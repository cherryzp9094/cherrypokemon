# 데이터와 도메인

> 코드 예시는 규칙을 보여주기 위한 것이다. 라이브러리 버전에 따라 API 이름이 다를 수 있으니 작성할 때 해당 버전 문서를 확인한다.

## 1. 구조 `필수`

오프라인 우선이다. **읽기는 항상 로컬 데이터베이스에서** 하고, 네트워크는 로컬을 갱신하는 데만 쓴다.

```
                 ┌──────────── :core:data ─────────────┐
ViewModel ◀─Flow─┤ OfflineFirstPokemonRepository       │
                 │   읽기: PokemonDao ─▶ asExternalModel()
                 │   갱신: PokemonNetworkDataSource ─▶ asEntity() ─▶ PokemonDao
                 │   목록: Pager(PokemonRemoteMediator, PokemonDao::pagingSource)
                 └──────┬────────────────────────┬─────┘
                        ▼                        ▼
                 :core:database            :core:network
                 PokemonEntity             NetworkPokemon
                 PokemonDao                PokemonNetworkDataSource
```

| 규칙 | 근거 |
|---|---|
| 네트워크를 쓰는 Repository는 로컬 데이터 소스와 네트워크 데이터 소스를 모두 가진다 | [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first): "A repository with network access in an offline-first app must always have a local data source." |
| 로컬 데이터 소스가 상위 레이어가 읽는 유일한 출처다 | 같음: "It should be the exclusive source of any data that higher layers of the app read." |
| domain·UI 레이어는 네트워크 레이어와 직접 통신하지 않는다 | 같음: "The domain and UI layers of the app must never communicate directly with the network layer." |
| 네트워크 모델과 엔티티는 데이터 레이어 안에만 두고, 밖에는 세 번째 타입(`:core:model`)을 노출한다 | 같음: "keep both the AuthorEntity and the NetworkAuthor internal to the data layer and expose a third type" |
| 동기화는 pull 방식이다. 화면에 필요할 때 받아와 로컬에 저장한다 | 같음: Pull-based synchronization, [README 결정 사항](README.md#결정-사항) |

## 2. 네트워크 (`:core:network`)

### 2-1. 데이터 소스

```kotlin
/** PokeAPI 호출. */
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
) : PokemonNetworkDataSource { ... }
```

| 규칙 | 강도 | 근거 |
|---|---|---|
| 데이터 소스는 인터페이스로 노출하고 구현은 `internal`, Retrofit 인터페이스는 `private` | `필수` | [Data layer](https://developer.android.com/topic/architecture/data-layer) Key Point: "Relying on interfaces makes API implementations swappable ... inject fake data source implementations in tests", [Modularization patterns](https://developer.android.com/topic/modularization/patterns) |
| 데이터 소스 하나는 데이터 소스 하나(PokeAPI)만 다룬다 | `필수` | Data layer |
| 실패하면 예외를 그대로 던진다. 빈 값이나 기본값으로 바꾸지 않는다 | `필수` | Data layer: Expose errors |
| 모든 트래픽은 HTTPS이고 network security configuration을 선언한다 | `필수` | [Core app quality](https://developer.android.com/docs/quality-guidelines/core-app-quality): Network_Security_Traffic, Network_Security_Configuration |
| 로그 인터셉터는 디버그 빌드에서만 붙이고, 민감한 데이터를 로그에 남기지 않는다 | `필수` | Core app quality: Production_Build_Quality, Sensitive_Data_Logging |
| 기본 주소는 `BuildConfig` 필드로 둔다 | `권장` | NiA `core/network` |
| OkHttp는 `dagger.Lazy<Call.Factory>`로 받아 메인 스레드에서 초기화되지 않게 한다 | `권장` | NiA `RetrofitNiaNetwork` |

### 2-2. 네트워크 모델 `필수`

```kotlin
@Serializable
data class NetworkPokemonDetail(
    val id: Int,
    val name: String,
    @SerialName("base_experience") val baseExperience: Int? = null,
    val types: List<NetworkPokemonTypeSlot> = emptyList(),
)
```

- kotlinx.serialization을 쓴다. 프로퍼티는 camelCase, JSON 이름이 다르면 `@SerialName`.
- 서버가 빠뜨릴 수 있는 필드는 기본값이나 nullable로 둔다.
- `Json { ignoreUnknownKeys = true }`를 `@Singleton`으로 제공한다.
- 같은 JSON 구조(PokeAPI의 `{ name, url }`)는 모델 하나로 쓴다.

```kotlin
// ❌ JSON 이름을 프로퍼티 이름으로 그대로 쓰고, 리플렉션 기반 Gson에 기댄다
data class PokemonDetailResponse(val base_experience: Int?, val is_default: Boolean?)
```

근거: [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first) `@Serializable NetworkAuthor` 예시, NiA `NetworkModule`

## 3. 데이터베이스 (`:core:database`)

### 3-1. Room `필수`

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

    @Query("SELECT * FROM pokemon_details WHERE id = :pokeId")
    fun getPokemonDetailEntity(pokeId: Int): Flow<PokemonDetailEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemons(entities: List<PokemonEntity>)
}
```

| 규칙 | 강도 | 근거 |
|---|---|---|
| SQLite API를 직접 쓰지 않고 Room을 쓴다. 어노테이션 처리는 KSP | `필수` | [Room](https://developer.android.com/training/data-storage/room): "We recommend using Room instead of using the SQLite APIs directly", Room 3.0은 KSP 필요 |
| `RoomDatabase` 인스턴스는 하나(`@Singleton`) | `필수` | Room Note: 단일 프로세스면 싱글턴 |
| 계속 관찰하는 조회는 `Flow`, 한 번 읽기·쓰기는 `suspend` | `필수` | [Async queries](https://developer.android.com/training/data-storage/room/async-queries) |
| 관찰 쿼리는 테이블의 다른 행이 바뀌어도 다시 실행되므로, 필요하면 `distinctUntilChanged()` | `권장` | 같음 Note |
| 엔티티 간 관계는 multimap 반환 타입(`Map<A, List<B>>`)으로 조회한다. 중간 데이터 클래스는 이유가 있을 때만 | `권장` | [Relationships](https://developer.android.com/training/data-storage/room/relationships): "If you don't have a specific reason to use intermediate data classes, we recommend using the multimap return type approach" |
| 테이블 이름은 복수형 snake_case, 컬럼 이름은 snake_case(`@ColumnInfo(name = ...)`) | `권장` | [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first) `AuthorEntity` 예시 |
| 엔티티 → 외부 모델 변환 `asExternalModel()`은 엔티티 파일에 둔다 | `권장` | Offline-first 예시, NiA |
| `RoomDatabase` 클래스는 `internal`, 엔티티와 DAO는 `:core:data`가 쓰므로 `public` | `필수` | NiA `NiaDatabase` |

### 3-2. 스키마와 마이그레이션 `필수`

- 스키마를 내보내고(Room Gradle Plugin `schemaDirectory("$projectDir/schemas")`) 그 JSON을 git에 커밋한다.
- 스키마를 바꾸면 버전을 올리고 `AutoMigration`이나 `Migration`을 추가한다. 수동 마이그레이션 SQL은 상수 참조 없이 전체 쿼리 문자열로 쓴다.
- `fallbackToDestructiveMigration()`은 데이터 유실이 허용될 때만 쓴다. **사용자가 만든 데이터(즐겨찾기 등)가 있는 테이블이 생기면 쓰지 않는다.** 네트워크에서 다시 받을 수 있는 캐시만 있는 동안은 허용한다.
- 마이그레이션마다 테스트를 추가한다.

근거: [Migrate your Room database](https://developer.android.com/training/data-storage/room/migrating-db-versions): "Store these files in your version control system", "If it's acceptable to lose existing data when a migration path is missing, call the fallbackToDestructiveMigration"

## 4. Repository (`:core:data`)

### 4-1. 형태 `필수`

```kotlin
/** 포켓몬 데이터를 로컬 데이터베이스 기준으로 제공하고 네트워크에서 받아 로컬을 갱신하는 저장소. */
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
        pokemonDao.insertPokemonDetail(network.getPokemonDetail(pokeId).asEntity())
    }
    ...
}
```

| 규칙 | 근거 |
|---|---|
| 데이터 종류마다 Repository를 만든다. 데이터 소스가 하나여도 만든다 | [Recommendations](https://developer.android.com/topic/architecture/recommendations#layered-architecture) Strongly recommended, [Data layer](https://developer.android.com/topic/architecture/data-layer) |
| 다른 레이어는 데이터 소스에 직접 접근하지 않는다. ViewModel·UseCase는 데이터 소스를 의존성으로 갖지 않는다 | Data layer: "Other layers in the hierarchy should never access data sources directly" |
| Repository는 생성자로 데이터 소스를 받는다. 추가 로직이 없으면 DAO를 직접 받아도 된다 | Data layer: Room as a data source |
| 노출하는 데이터는 불변이다 | Data layer |
| **조회 함수는 로컬의 `Flow`를 외부 모델로 바꿔 돌려준다.** 네트워크 응답을 직접 돌려주지 않는다 | [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first): "read operations from repositories read directly from the local data source" |
| 갱신 함수(`refresh<모델>`)는 네트워크에서 받아 로컬에 쓴다. 새 값은 조회 `Flow`로 흘러간다. 실패하면 예외를 던진다 | 같음: "Write any updates to the local data source first" |
| 모든 함수는 메인 스레드에서 불러도 안전하다 | Data layer: Threading |
| 구현은 `internal`, `DataModule`에서 `@Binds`로 연결한다 | [Modularization patterns](https://developer.android.com/topic/modularization/patterns): 데이터 모듈의 공개 API는 Repository, NiA `DataModule` |
| `Network<모델>.asEntity()`는 `:core:data`의 `model` 패키지에 둔다 | Offline-first Note: 매퍼는 사용하는 모듈에 정의, NiA `core/data/model` |
| 화면보다 오래 살아야 하는 작업은 주입받은 `@ApplicationScope` `CoroutineScope`로 한다. Repository가 직접 `CoroutineScope`를 만들지 않는다 | Data layer: "should receive a scope as a parameter in its constructor instead of creating its own CoroutineScope" |

### 4-2. 목록은 RemoteMediator `필수`

```kotlin
override fun getPokemonsStream(): Flow<PagingData<Pokemon>> =
    Pager(
        config = PagingConfig(pageSize = POKEMON_PAGE_SIZE),
        remoteMediator = PokemonRemoteMediator(database, network),
        pagingSourceFactory = pokemonDao::pagingSource,
    ).flow.map { pagingData -> pagingData.map(PokemonEntity::asExternalModel) }
```

- Room이 만든 `PagingSource`가 DB를 기준으로 화면에 데이터를 주고, `RemoteMediator`는 로컬 데이터가 떨어지면 네트워크에서 받아 저장만 한다.
- `load()`: `REFRESH`는 처음부터, `APPEND`는 다음 페이지. 앞쪽 페이지를 따로 받지 않으면 `PREPEND`는 `MediatorResult.Success(endOfPaginationReached = true)`.
- 원격 키가 아이템에 대응하지 않으면(PokeAPI의 offset) 별도 원격 키 테이블에 저장한다.
- 삭제·삽입·원격 키 저장은 `database.withTransaction {}` 안에서 함께 한다.
- `IOException` / `HttpException`은 `MediatorResult.Error`로 돌려준다. Retrofit suspend 호출은 `withContext`로 감싸지 않는다.
- `initialize()`에서 캐시 유효 시간을 보고 `SKIP_INITIAL_REFRESH` / `LAUNCH_INITIAL_REFRESH`를 돌려준다.
- `Pager`를 Repository에서 만드는 이유는 [README 결정 사항](README.md#결정-사항)을 본다.

근거: [Page from network and database](https://developer.android.com/topic/libraries/architecture/paging/v3-network-db)

## 5. 외부 모델 (`:core:model`) `필수`

- 순수 Kotlin `data class` / `enum class`다. Android, Compose, Room, 직렬화 어노테이션을 넣지 않는다.
- 데이터 소스가 주는 정보 중 앱에 필요한 것만 담는다.
- 모든 프로퍼티는 `val`이다.
- 계산이 필요한 값(URL에서 파싱한 ID, 이미지 주소)은 데이터 레이어가 계산해 필드로 담는다. 프로퍼티 getter는 싸고 예외를 던지지 않아야 한다.

```kotlin
// ❌ 접근할 때마다 문자열을 파싱하고 실패 시 예외, 외부 모델이 이미지 서버 주소를 안다
data class Pokemon(val name: String, val url: String) {
    val id: Int get() = url.split("/").lastOrNull { it.isNotEmpty() }?.toInt() ?: 0
    val imageUrl: String get() = "https://raw.githubusercontent.com/PokeAPI/sprites/.../$id.png"
}

// ✅
data class Pokemon(val id: Int, val name: String, val imageUrl: String)
```

근거: [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first): 외부 레이어용 세 번째 타입, [Data layer: Represent business models](https://developer.android.com/topic/architecture/data-layer), [Guide to app architecture](https://developer.android.com/topic/architecture): "Reduce dependencies on Android classes", [Kotlin coding conventions: Functions vs properties](https://kotlinlang.org/docs/coding-conventions.html#functions-vs-properties)

## 6. UseCase

### 6-1. 필요할 때만 만든다 `필수`

다음 중 하나에 해당할 때만 `:core:domain`에 UseCase를 만든다. 그 외에는 ViewModel이 Repository를 직접 쓴다.

- 여러 ViewModel이 같은 비즈니스 로직을 쓴다
- 특정 ViewModel의 비즈니스 로직이 복잡하다
- 여러 Repository를 조합한다 (Room 관계 쿼리로 해결되면 UseCase 대신 Repository에서 조회한다)

```kotlin
// ❌ 호출을 그대로 전달만 한다
class PokemonListUseCase @Inject constructor(private val repository: PokemonRepository) {
    suspend operator fun invoke() = repository.fetchPokemonList()
}
```

근거: [Domain layer](https://developer.android.com/topic/architecture/domain-layer): "You should only use it when needed", "A good approach is to add use cases only when required", Room 관계 Note, [Recommendations](https://developer.android.com/topic/architecture/recommendations#layered-architecture)

### 6-2. 형태 `필수`

- 이름: 현재형 동사 + 명사 + `UseCase`. 하나의 기능만 맡는다.
- `operator fun invoke()`로 호출한다.
- 가변 데이터를 갖지 않는다. 자체 수명이 없으므로 스코프 어노테이션을 붙이지 않는다.
- 메인 스레드에서 불러도 안전해야 한다. 막히는 계산은 주입받은 디스패처로 옮기되, 캐시·재사용이 필요한 무거운 계산이면 데이터 레이어에 둔다.
- 유틸 클래스의 정적 함수 대신 UseCase로 둔다.

근거: [Domain layer](https://developer.android.com/topic/architecture/domain-layer)

## 7. 에러 처리 `필수`

| 위치 | 할 일 | 근거 |
|---|---|---|
| 데이터 소스, Repository | 예외를 던진다. 실패를 기본값으로 바꾸지 않는다. 의미 있는 실패는 커스텀 예외로 | [Data layer: Expose errors](https://developer.android.com/topic/architecture/data-layer) |
| RemoteMediator | `IOException` / `HttpException` → `MediatorResult.Error` | [Page from network and database](https://developer.android.com/topic/libraries/architecture/paging/v3-network-db) |
| ViewModel | 한 번 실행의 실패는 `try/catch`, 로컬 읽기 Flow의 실패는 `catch`로 잡아 상태나 SideEffect로 바꾼다. 캐시된 데이터는 계속 보여준다 | Data layer, [Offline-first: Error handling](https://developer.android.com/topic/architecture/data-layer/offline-first) |
| UI | 로딩·에러와 재시도를 그린다 | [UI layer](https://developer.android.com/topic/architecture/ui-layer) |

- 네트워크 읽기 재시도는 오류 종류를 보고 판단한다(연결 없음은 재시도, 인증 실패는 재시도하지 않음). `권장` — Offline-first: Exponential backoff
- 잡는 타입과 `CancellationException` 처리는 [kotlin.md 6-6](kotlin.md#6-6-예외-필수)을 따른다.
- Flow를 성공·실패·로딩으로 바꿔야 하면 `:core:common`의 `Result`와 `asResult()`를 쓴다. `선택` — Data layer Note, NiA

## 8. 의존성 주입

### 8-1. Hilt `필수`

| 규칙 | 근거 |
|---|---|
| 생성자 주입을 쓴다. 필드 주입은 Android 진입점에서만 | [Recommendations](https://developer.android.com/topic/architecture/recommendations#handle-dependencies) Strongly recommended |
| Hilt 모듈은 생성자 주입이 불가능한 타입(인터페이스, 외부 라이브러리 타입, 빌더)에만 만든다 | [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) |
| 인터페이스 바인딩은 `abstract class` 모듈의 `@Binds`, 외부 타입 생성은 `object` 모듈의 `@Provides` | 같음 |
| 같은 타입의 바인딩이 여럿이면 `@Qualifier @Retention(AnnotationRetention.BINARY)`를 만들고, 그 타입을 제공하는 모든 곳에 qualifier를 붙인다 | 같음: "add qualifiers to all the possible ways to provide that dependency" |
| Hilt 모듈은 구현이 있는 Gradle 모듈의 `di` 패키지에 둔다. 테스트에서 `@TestInstallIn(replaces = …)`로 교체하는 모듈(`DataModule`, `DispatchersModule`)은 `public`, 나머지는 `internal` | [Hilt testing](https://developer.android.com/training/dependency-injection/hilt-testing), NiA |
| 스코프는 최소한으로. `@Singleton`은 공유 가변 상태가 있거나, 동기화가 필요하거나, 생성 비용을 측정해 비싼 객체에만 | [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) Note: "Minimize the use of scoped bindings", Recommendations |

| Hilt 모듈 | 위치 | 제공 |
|---|---|---|
| `DispatchersModule` | `:core:common` | `@Dispatcher(IO)`, `@Dispatcher(Default)` |
| `CoroutineScopesModule` | `:core:common` | `@ApplicationScope CoroutineScope` |
| `NetworkModule` | `:core:network` | `Json`, `Call.Factory`, 데이터 소스 바인딩 |
| `DatabaseModule`, `DaosModule` | `:core:database` | 데이터베이스, DAO |
| `DataModule` | `:core:data` | Repository 바인딩 |

### 8-2. WorkManager `필수`

앱이 종료돼도 끝나야 하는 작업이 생기면 `:sync:work` 모듈에서 WorkManager로 한다. Worker는 `@HiltWorker` + `@AssistedInject`, 앱 시작 시 등록은 App Startup `Initializer`에서 한다.

근거: [Data layer: Business-oriented operations](https://developer.android.com/topic/architecture/data-layer), [Hilt with Jetpack: WorkManager](https://developer.android.com/training/dependency-injection/hilt-jetpack), [Offline-first: WorkManager](https://developer.android.com/topic/architecture/data-layer/offline-first)
