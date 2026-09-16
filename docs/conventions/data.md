# 데이터와 도메인

> 코드 예시는 규칙을 보여주기 위한 것이다. 라이브러리 버전에 따라 API 이름이 다를 수 있으니 작성할 때 해당 버전 문서를 확인한다.

## 1. 구조 `필수`

오프라인 우선이다. **읽기는 항상 로컬 데이터베이스에서** 하고, 네트워크는 로컬을 갱신하는 데만 쓴다.

```
                 ┌──────────── :core:data ────────────┐
ViewModel ◀─Flow─┤ OfflineFirstPokemonRepository      │
                 │   읽기: PokemonDao ─▶ asExternalModel()
                 │   갱신: PokemonNetworkDataSource ─▶ asEntity() ─▶ PokemonDao
                 │   목록: Pager(PokemonRemoteMediator, PokemonDao.pagingSource)
                 └──────┬───────────────────────┬──────┘
                        ▼                       ▼
                 :core:database           :core:network
                 PokemonEntity            NetworkPokemon
                 PokemonDao               PokemonNetworkDataSource
```

| 모델 | 모듈 | 이름 | 누가 만드나 |
|---|---|---|---|
| 네트워크 모델 | `:core:network` | `Network<데이터>` | kotlinx.serialization |
| 엔티티 | `:core:database` | `<데이터>Entity` | `Network<데이터>.asEntity()` (`:core:data`) |
| 도메인 모델 | `:core:model` | `<데이터>` | `<데이터>Entity.asExternalModel()` (`:core:database`) |

- ViewModel과 UI에는 도메인 모델만 나간다. 네트워크 모델과 엔티티는 `:core:data` 밖으로 나가지 않는다.
- 동기화는 pull 방식이다. 화면에 필요할 때 받아와 로컬에 저장한다. 앱이 꺼져 있을 때도 동기화해야 하는 데이터가 생기면 `:sync:work` 모듈에서 WorkManager로 한다.

근거: [Build an offline-first app](https://developer.android.com/topic/architecture/data-layer/offline-first), [Data layer](https://developer.android.com/topic/architecture/data-layer), NiA `OfflineFirstTopicsRepository`

## 2. 네트워크 (`:core:network`)

### 2-1. 데이터 소스 `필수`

```kotlin
/** PokeAPI 호출을 담당한다. */
interface PokemonNetworkDataSource {
    suspend fun getPokemons(offset: Int, limit: Int): NetworkPokemonPage
    suspend fun getPokemonDetail(pokeId: Int): NetworkPokemonDetail
}

private interface RetrofitPokemonNetworkApi {
    @GET("pokemon")
    suspend fun getPokemons(@Query("offset") offset: Int, @Query("limit") limit: Int): NetworkPokemonPage

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") pokeId: Int): NetworkPokemonDetail
}

@Singleton
internal class RetrofitPokemonNetwork @Inject constructor(
    networkJson: Json,
    okhttpCallFactory: dagger.Lazy<Call.Factory>,
) : PokemonNetworkDataSource {
    private val api = Retrofit.Builder()
        .baseUrl(BuildConfig.POKEAPI_BASE_URL)
        .callFactory { okhttpCallFactory.get().newCall(it) }
        .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(RetrofitPokemonNetworkApi::class.java)
    ...
}
```

- 모듈 밖에 공개하는 것은 데이터 소스 인터페이스와 네트워크 모델뿐이다. Retrofit 인터페이스는 `private`, 구현은 `internal`이다.
- 실패하면 `IOException` / `HttpException` / `SerializationException`을 그대로 던진다. 빈 값이나 기본값으로 바꾸지 않는다.
- 기본 주소는 `BuildConfig` 필드로 둔다. 코드에 URL 문자열을 쓰지 않는다.
- OkHttp는 `dagger.Lazy<Call.Factory>`로 받아 메인 스레드에서 초기화되지 않게 한다.
- 로그 인터셉터는 디버그 빌드에서만 붙인다.

근거: [Data layer: Data sources](https://developer.android.com/topic/architecture/data-layer), NiA `RetrofitNiaNetwork`

### 2-2. 네트워크 모델 `필수`

```kotlin
@Serializable
data class NetworkPokemonDetail(
    val id: Int,
    val name: String,
    @SerialName("base_experience") val baseExperience: Int? = null,
    val height: Int = 0,
    val weight: Int = 0,
    val types: List<NetworkPokemonTypeSlot> = emptyList(),
)
```

- kotlinx.serialization을 쓴다. 프로퍼티는 camelCase이고 JSON 이름이 다르면 `@SerialName`으로 적는다.
- 서버가 빠뜨릴 수 있는 필드는 기본값이나 nullable로 둔다. 필수 필드(`id`)는 기본값을 두지 않아 누락되면 파싱이 실패하게 한다.
- `Json { ignoreUnknownKeys = true }`
- 같은 구조를 여러 번 정의하지 않는다. PokeAPI의 `{ name, url }`은 `NetworkNamedApiResource` 하나로 쓴다.

```kotlin
// ❌ JSON 이름을 프로퍼티 이름으로 그대로 씀. R8이 이름을 바꾸면 Gson 파싱 결과가 전부 null
data class PokemonDetailResponse(val base_experience: Int?, val is_default: Boolean?)
```

근거: [Offline-first: model naming](https://developer.android.com/topic/architecture/data-layer/offline-first), [kotlinx.serialization](https://kotlinlang.org/docs/serialization.html)

## 3. 데이터베이스 (`:core:database`)

### 3-1. 엔티티와 DAO `필수`

```kotlin
@Entity(tableName = "pokemons")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
)

fun PokemonEntity.asExternalModel() = Pokemon(id = id, name = name, imageUrl = imageUrl)

@Dao
interface PokemonDao {
    @Query("SELECT * FROM pokemons ORDER BY id")
    fun pagingSource(): PagingSource<Int, PokemonEntity>

    @Transaction
    @Query("SELECT * FROM pokemon_details WHERE id = :pokeId")
    fun getPokemonDetailEntity(pokeId: Int): Flow<PopulatedPokemonDetail?>

    @Upsert
    suspend fun upsertPokemons(entities: List<PokemonEntity>)
}
```

| 규칙 | 근거 |
|---|---|
| 테이블 이름은 복수형 snake_case, 컬럼 이름은 snake_case | NiA |
| 기본 키는 서버 ID를 그대로 쓴다 | 네트워크 데이터와 1:1로 갱신 |
| 계속 관찰하는 조회는 `Flow`, 한 번 조회와 쓰기는 `suspend` | [Room: Asynchronous queries](https://developer.android.com/training/data-storage/room/async-queries) |
| 삽입·갱신은 `@Upsert` | NiA |
| 관계는 `@Relation`을 가진 `Populated<데이터>` 클래스와 `@Transaction` 조회로 가져온다 | NiA `PopulatedNewsResource` |
| 엔티티 → 도메인 변환 `asExternalModel()`은 엔티티 파일에 둔다 | [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first), NiA |
| `RoomDatabase` 클래스는 `internal`, 엔티티와 DAO는 `:core:data`가 쓰므로 `public` | NiA `NiaDatabase` |

### 3-2. 스키마와 마이그레이션 `필수`

- `exportSchema = true`이고 스키마 JSON(`schemas/`)을 git에 커밋한다.
- 스키마를 바꾸면 버전을 올리고 `AutoMigration`이나 `Migration`을 추가한다. `fallbackToDestructiveMigration()`을 쓰지 않는다.
- 마이그레이션마다 `MigrationTestHelper` 테스트를 추가한다.

근거: [Migrate your Room database](https://developer.android.com/training/data-storage/room/migrating-db-versions), [Test migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions#test)

## 4. Repository (`:core:data`)

### 4-1. 형태 `필수`

```kotlin
/** 포켓몬 데이터를 로컬 데이터베이스 기준으로 제공하고, 네트워크에서 받아 로컬을 갱신한다. */
interface PokemonRepository {
    fun getPokemonsStream(): Flow<PagingData<Pokemon>>
    fun getPokemonDetailStream(pokeId: Int): Flow<PokemonDetail?>
    suspend fun refreshPokemonDetail(pokeId: Int)
}

internal class OfflineFirstPokemonRepository @Inject constructor(
    private val database: CherryPokemonDatabase,
    private val pokemonDao: PokemonDao,
    private val network: PokemonNetworkDataSource,
) : PokemonRepository {

    override fun getPokemonDetailStream(pokeId: Int): Flow<PokemonDetail?> =
        pokemonDao.getPokemonDetailEntity(pokeId).map { it?.asExternalModel() }

    override suspend fun refreshPokemonDetail(pokeId: Int) {
        val networkDetail = network.getPokemonDetail(pokeId)
        pokemonDao.upsertPokemonDetail(networkDetail.asEntity())
    }
    ...
}
```

| 규칙 | 근거 |
|---|---|
| 인터페이스와 구현을 `:core:data`에 두고, 구현은 `internal`, `DataModule`에서 `@Binds`로 연결한다 | NiA `DataModule` |
| **조회 함수는 로컬(DAO)의 `Flow`를 도메인 모델로 바꿔 돌려준다.** 네트워크 응답을 직접 돌려주지 않는다 | [Offline-first: Reads](https://developer.android.com/topic/architecture/data-layer/offline-first) |
| 갱신 함수(`refresh<데이터>`)는 네트워크에서 받아 로컬에 저장만 한다. 새 값은 조회 `Flow`로 흘러간다 | 같음 |
| 갱신이 실패하면 예외를 던진다. 로컬 데이터는 건드리지 않는다 | 같음 |
| `Network<데이터>.asEntity()`는 `:core:data`의 `model` 패키지에 둔다 | NiA `core/data/model` |
| 모든 함수는 메인 스레드에서 불러도 안전하다 | [Data layer: Threading](https://developer.android.com/topic/architecture/data-layer) |

### 4-2. 목록은 RemoteMediator `필수`

```kotlin
override fun getPokemonsStream(): Flow<PagingData<Pokemon>> =
    Pager(
        config = PagingConfig(pageSize = POKEMON_PAGE_SIZE),
        remoteMediator = PokemonRemoteMediator(database, network),
        pagingSourceFactory = pokemonDao::pagingSource,
    ).flow.map { pagingData -> pagingData.map(PokemonEntity::asExternalModel) }
```

- Room의 `PagingSource`가 화면에 보여줄 데이터를 제공하고, `RemoteMediator`는 로컬 데이터가 떨어지면 네트워크에서 받아 저장만 한다.
- `load()`에서 `REFRESH`는 처음부터, `APPEND`는 다음 페이지를 받는다. PokeAPI는 앞쪽 페이지를 따로 받을 일이 없으므로 `PREPEND`는 `endOfPaginationReached = true`로 끝낸다.
- 다음 offset은 원격 키 테이블(`PokemonRemoteKeyEntity`)에 저장한다.
- 삭제와 삽입은 `database.withTransaction {}` 안에서 함께 한다.
- `IOException` / `HttpException`은 `MediatorResult.Error`로 돌려준다.
- `initialize()`에서 마지막 갱신 시각을 보고 캐시가 유효하면 `SKIP_INITIAL_REFRESH`를 돌려준다.
- 페이지 크기는 상수 하나(`POKEMON_PAGE_SIZE`)로 둔다.

근거: [Page from network and database](https://developer.android.com/topic/libraries/architecture/paging/v3-network-db)

## 5. 도메인 모델 (`:core:model`) `필수`

- 순수 Kotlin `data class` / `enum class`다. Android, Compose, Room, 직렬화 어노테이션을 넣지 않는다.
- 모든 프로퍼티는 `val`이고, 가능한 한 non-null이다.
- 계산이 필요한 값(URL에서 파싱한 ID, 이미지 주소)은 데이터 레이어가 계산해서 필드로 담는다. getter에서 매번 계산하지 않는다.
- 이미지 서버 주소 같은 인프라 정보는 데이터 레이어(`:core:network`)에 둔다.

```kotlin
// ❌ 접근할 때마다 문자열을 파싱하고, 도메인 모델이 이미지 서버 주소를 안다
data class Pokemon(val name: String, val url: String) {
    val id: Int get() = url.split("/").lastOrNull { it.isNotEmpty() }?.toInt() ?: 0
    val imageUrl: String get() = "https://raw.githubusercontent.com/PokeAPI/sprites/.../$id.png"
}

// ✅
data class Pokemon(val id: Int, val name: String, val imageUrl: String)
```

근거: [Recommendations: Models](https://developer.android.com/topic/architecture/recommendations#models), NiA `core/model`

## 6. UseCase

### 6-1. 로직이 있을 때만 만든다 `필수`

다음 중 하나에 해당할 때만 `:core:domain`에 UseCase를 만든다. 그 외에는 ViewModel이 Repository를 직접 쓴다.

- 여러 Repository를 조합한다
- 같은 로직을 두 개 이상의 ViewModel이 쓴다
- ViewModel에 두기에는 복잡한 비즈니스 로직이다

```kotlin
// ❌ 호출을 그대로 전달만 한다
class PokemonListUseCase @Inject constructor(private val repository: PokemonRepository) {
    suspend operator fun invoke() = repository.fetchPokemonList()
}
```

근거: [Domain layer](https://developer.android.com/topic/architecture/domain-layer), NiA `core/domain`

### 6-2. 형태 `필수`

- 이름: 현재형 동사 + 명사 + `UseCase` (`GetPokemonWithSpeciesUseCase`)
- 공개 함수는 `operator fun invoke` 하나. 계속 바뀌는 값은 `Flow`, 한 번 실행은 `suspend`.
- 가변 상태를 갖지 않는다. 스코프 어노테이션을 붙이지 않는다.
- 막히는 계산이 있으면 주입받은 디스패처로 `withContext` 한다.

근거: [Domain layer: Conventions](https://developer.android.com/topic/architecture/domain-layer#conventions), [Domain layer: Threading](https://developer.android.com/topic/architecture/domain-layer#threading)

## 7. 에러 처리 `필수`

| 위치 | 할 일 |
|---|---|
| `:core:network` | 예외를 그대로 던진다 |
| `:core:database` | 예외를 그대로 던진다 |
| Repository 갱신 함수 | 예외를 그대로 던진다. 실패를 기본값으로 바꾸지 않는다 |
| RemoteMediator | `IOException` / `HttpException` → `MediatorResult.Error` |
| ViewModel | 갱신 실패를 잡아 상태나 SideEffect로 바꾼다. 캐시된 데이터는 계속 보여준다 |
| UI | `loadState`와 UiState로 에러와 재시도를 그린다 |

- 잡는 타입과 `CancellationException` 처리는 [kotlin.md 4-6](kotlin.md#4-6-예외-필수)을 따른다.
- `Flow`를 성공·실패·로딩 상태로 바꿔야 하면 `:core:common`의 `Result`와 `asResult()`를 쓴다.

근거: [Data layer: Expose errors](https://developer.android.com/topic/architecture/data-layer), [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first), NiA `core/common/result/Result.kt`

## 8. 의존성 주입

### 8-1. Hilt 모듈 `필수`

모든 의존성은 생성자로 받는다. 필드 주입은 Android 진입점(`Application`, `Activity`)에서만 쓴다.

| Hilt 모듈 | 위치 | 제공 |
|---|---|---|
| `DispatchersModule` | `:core:common` | `@Dispatcher(IO)`, `@Dispatcher(Default)` |
| `CoroutineScopesModule` | `:core:common` | `@ApplicationScope CoroutineScope` |
| `NetworkModule` | `:core:network` | `Json`, `Call.Factory`, `PokemonNetworkDataSource` 바인딩 |
| `DatabaseModule` | `:core:database` | `CherryPokemonDatabase` |
| `DaosModule` | `:core:database` | 각 DAO |
| `DataModule` | `:core:data` | Repository 인터페이스 → 구현 (`@Binds`) |

- Hilt 모듈은 제공하는 구현이 있는 모듈의 `di` 패키지에 둔다.
- 테스트에서 `@TestInstallIn(replaces = …)`로 교체하는 모듈(`DataModule`, `DispatchersModule`)은 `public`이고, 그 안의 바인딩 함수는 `internal`이다. 나머지 Hilt 모듈은 `internal`이다.
- `@Binds`는 `abstract class` / `interface` 모듈, `@Provides`는 `object` 모듈에 둔다.

근거: [Recommendations: Use dependency injection](https://developer.android.com/topic/architecture/recommendations#handle-dependencies), NiA

### 8-2. 스코프 `필수`

`@Singleton`은 공유해야 하는 가변 상태가 있거나 만들기 비싼 객체(데이터베이스, `Json`, `Call.Factory`, Retrofit 구현)에만 붙인다.

근거: [Recommendations: Scope to a component when necessary](https://developer.android.com/topic/architecture/recommendations#handle-dependencies)
