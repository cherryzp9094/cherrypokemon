# 데이터와 도메인

> 코드 예시는 방향을 보여주기 위한 것이며, 이 프로젝트에서 아직 빌드해 보지 않았다.

## 1. Repository

### 1-1. 인터페이스는 `:domain`, 구현은 `:data` `필수`

```kotlin
// :domain
interface PokemonRepository {
    fun getPokemonsStream(): Flow<PagingData<Pokemon>>
    suspend fun getPokemonDetail(pokeId: Int): PokemonDetail
}

// :data
internal class DefaultPokemonRepository @Inject constructor(
    private val pokemonApi: PokemonApi,
) : PokemonRepository { ... }
```

- 구현 클래스는 `internal`이고 `RepositoryModule`에서 `@Binds`로 연결한다.
- 데이터 소스가 하나뿐이어도 UI는 Repository를 거친다.
- 공개 함수는 **도메인 모델만** 주고받는다. DTO를 밖으로 내보내지 않는다.
- 한 번 조회는 `suspend fun get<데이터>()`, 계속 바뀌는 값은 `fun get<데이터>Stream(): Flow`. ([kotlin.md 3-5](kotlin.md#3-5-한-번-조회는-suspend-계속-바뀌는-값은-flow-필수))
- 모든 함수는 메인 스레드에서 불러도 안전해야 한다.

```kotlin
// ❌ PokemonRepositoryImpl.kt: Impl 접미사, public, 스트림을 만드는 함수가 suspend
class PokemonRepositoryImpl @Inject constructor(...): PokemonRepository {
    override suspend fun fetchPokemonList(): Flow<PagingData<Pokemon>> = ...
```

근거: [Data layer](https://developer.android.com/topic/architecture/data-layer), [Recommendations: Expose application data from the data layer using a repository](https://developer.android.com/topic/architecture/recommendations#layered-architecture)

### 1-2. 데이터 소스 클래스 `선택`

원격 API 하나뿐인 지금은 Repository가 `PokemonApi`를 직접 쓴다. 로컬 저장소(Room, DataStore)가 생기면 공식 이름 규칙대로 나눈다.

| 클래스 | 역할 |
|---|---|
| `PokemonRemoteDataSource` | `PokemonApi` 호출 |
| `PokemonLocalDataSource` | Room DAO 호출 |
| `DefaultPokemonRepository` | 둘을 조합하고, 어느 쪽이 기준(source of truth)인지 정한다 |

이름에 구현 기술(`Retrofit`, `SharedPreferences`)을 넣지 않고 `Remote` / `Local`을 쓴다.

근거: [Data layer: Naming conventions](https://developer.android.com/topic/architecture/data-layer#naming-conventions)

### 1-3. PagingSource `필수`

- `internal`이고, Repository의 `pagingSourceFactory`에서 직접 만든다. `@Inject`를 붙이지 않는다.
- 페이지 크기는 상수 하나로 두고 `PagingConfig`와 PagingSource가 같이 쓴다.
- `load()` 전체를 `try`로 감싸고, 네트워크 예외를 `LoadResult.Error`로 돌려준다.
- `getRefreshKey()`는 `anchorPosition` 기준으로 계산한다. `null`을 돌려주면 새로고침할 때 스크롤 위치가 사라진다.

```kotlin
// ❌ PokemonPagingSource.kt: API 호출이 try 밖이라 네트워크 에러가 크래시가 된다
val response = pokemonApi.fetchPokemonList(limit, page)...
return try { ... } catch (e: Exception) { LoadResult.Error(e) }
```

구현 예시는 `docs/improvement-plan.md` Phase 0-1에 있다.

## 2. UseCase

### 2-1. 로직이 있을 때만 만든다 `권장`

다음 중 하나에 해당할 때만 UseCase를 만든다. 그 외에는 ViewModel이 Repository를 직접 쓴다.

- 여러 Repository를 조합한다
- 같은 로직을 두 개 이상의 ViewModel이 쓴다
- ViewModel에 두기에는 복잡한 비즈니스 로직이다

```kotlin
// ❌ PokemonListUseCase.kt: 호출을 그대로 전달만 한다
class PokemonListUseCase @Inject constructor(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke() = pokemonRepository.fetchPokemonList()
}
```

전달만 하는 UseCase는 파일과 테스트만 늘리고, "UseCase를 꼭 거쳐야 하나"라는 규칙 혼란을 만든다.

근거: [Domain layer](https://developer.android.com/topic/architecture/domain-layer), [Recommendations: Use a domain layer (Recommended in big apps)](https://developer.android.com/topic/architecture/recommendations#layered-architecture)

### 2-2. 만들 때의 형태 `필수`

- 이름: 현재형 동사 + 명사 + `UseCase` (`GetPokemonWithSpeciesUseCase`)
- 공개 함수는 `operator fun invoke` 하나. 한 번 조회는 `suspend`, 스트림은 `Flow`를 돌려준다.
- 가변 상태를 갖지 않는다. `@Singleton`으로 만들지 않는다.
- 막히는 계산이 있으면 주입받은 디스패처로 `withContext` 한다.

근거: [Domain layer: Naming conventions](https://developer.android.com/topic/architecture/domain-layer#conventions), [Domain layer: Threading](https://developer.android.com/topic/architecture/domain-layer#threading)

## 3. 모델

### 3-1. 레이어마다 모델을 나눈다 `권장`

| 모델 | 위치 | 이름 | 공개 범위 |
|---|---|---|---|
| 응답 DTO | `:data` `model` 패키지 | `<데이터>Response` | `internal` |
| 도메인 모델 | `:domain` `model` 패키지 | `<데이터>` | public |
| UI 모델 | 화면 패키지 | `<데이터>UiModel` | 필요할 때만 (`선택`) |

- UI 모델은 표시용 가공(문자열 포맷, 색 계산)이 여러 곳에서 반복될 때만 만든다.
- 도메인 모델에는 Android·Compose 타입(`Color`, `Parcelable`, `@StringRes`)을 넣지 않는다. `필수`

근거: [Recommendations: Create a model per layer in complex apps](https://developer.android.com/topic/architecture/recommendations#models), [Data layer: Multiple levels of models](https://developer.android.com/topic/architecture/data-layer)

### 3-2. DTO는 kotlinx.serialization `필수`

- `@Serializable`을 붙이고, 프로퍼티는 camelCase로 짓고 JSON 이름은 `@SerialName`으로 적는다.
- 서버가 빠뜨릴 수 있는 필드는 nullable이거나 기본값을 둔다.
- `Json { ignoreUnknownKeys = true }`로 설정한다.

```kotlin
// ❌ PokemonDetailResponse.kt: JSON 이름을 필드명으로 그대로 씀. R8이 필드명을 바꾸면 파싱 결과가 전부 null
val base_experience: Int?,
val is_default: Boolean?,

// ✅
@Serializable
internal data class PokemonDetailResponse(
    @SerialName("id") val id: Int,
    @SerialName("base_experience") val baseExperience: Int? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
)
```

- 구조가 같은 DTO를 여러 번 정의하지 않는다. PokeAPI의 `{ name, url }`은 `NamedApiResourceResponse` 하나로 쓴다. `권장`

```kotlin
// ❌ PokemonSpeciesResponse.kt와 PokemonDetailResponse.kt에 같은 구조의 NamedResource가 각각 있다
```

### 3-3. 매퍼 `필수`

- `:data`의 `mapper` 패키지에 `internal fun <Dto>.toDomain(): <Model>` 확장 함수로 둔다.
- 빠진 값을 채우는 곳은 매퍼 하나다. 도메인 모델의 필드는 가능한 한 non-null이다.
- **URL 파싱처럼 계산이 필요한 값은 매퍼에서 한 번만 계산해서 필드로 담는다.** 도메인 모델의 getter에서 매번 계산하지 않는다.
- 이미지 서버 주소 같은 인프라 정보는 `:data`에 둔다. 도메인 모델이 호스트 주소를 알지 않게 한다. `권장`

```kotlin
// ❌ Pokemon.kt: 접근할 때마다 문자열을 파싱하고, 도메인 모델이 이미지 서버 주소를 안다
data class Pokemon(val name: String, val url: String) {
    val id: Int
        get() = url.split("/").lastOrNull { it.isNotEmpty() }?.toInt() ?: 0
    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/.../$id.png"
}

// ✅
data class Pokemon(val id: Int, val name: String, val imageUrl: String)

// id를 읽을 수 없는 항목은 id 0으로 만들지 않고 목록에서 뺀다 (results.mapNotNull { it.toDomainOrNull() })
internal fun PokemonResponse.toDomainOrNull(): Pokemon? {
    val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return null
    return Pokemon(id = id, name = name, imageUrl = PokemonImageUrl.officialArtwork(id))
}
```

## 4. 에러 처리 `필수`

| 레이어 | 할 일 |
|---|---|
| Data | 예외를 삼키지 않고 던진다. 실패를 빈 목록이나 `0` 같은 기본값으로 바꾸지 않는다 |
| PagingSource | `IOException` / `HttpException`을 `LoadResult.Error`로 바꾼다 |
| ViewModel | 구체적인 예외를 잡아 UiState의 에러 상태로 바꾼다 |
| UI | 에러 상태를 그리고 재시도 콜백을 연결한다 |

- 공식 가이드는 예외와 `Result` 래퍼를 모두 허용한다. 이 프로젝트는 **예외를 던지고 ViewModel에서 잡는 방식**으로 통일한다.
- 잡는 타입과 `CancellationException` 처리는 [kotlin.md 3-6](kotlin.md#3-6-예외는-구체적인-타입으로-잡는다-필수)을 따른다.

```kotlin
// ❌ PokemonDetailViewModel.kt: 인자가 없으면 0번 포켓몬을 요청하고, 실패는 처리하지 않는다
val pokemonDetail = pokemonDetailUseCase(savedStateHandle.get<Int>(POKE_NO).default()).single()
```

근거: [Data layer: Expose errors](https://developer.android.com/topic/architecture/data-layer), [Paging: Handle errors](https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data)

## 5. 의존성 주입

### 5-1. 생성자 주입과 Hilt `필수`

- 모든 의존성은 생성자로 받는다. 필드 주입은 Android 진입점(`Application`, `Activity`)에서만 쓴다.
- Hilt 모듈은 구현을 가진 모듈의 `di` 패키지에 둔다.

| Hilt 모듈 | 위치 | 형태 | 내용 |
|---|---|---|---|
| `NetworkModule` | `:data` | `internal object` + `@Provides` | `Json`, `OkHttpClient`, `Retrofit`, `PokemonApi` |
| `RepositoryModule` | `:data` | `internal interface` + `@Binds` | Repository 인터페이스 → 구현 |
| `DispatchersModule` | `:app` | `object` + `@Provides` | `@Dispatcher(IO)` → `Dispatchers.IO` |

근거: [Recommendations: Handle dependencies](https://developer.android.com/topic/architecture/recommendations#handle-dependencies)

### 5-2. 스코프는 필요할 때만 `필수`

`@Singleton`은 공유해야 하는 가변 상태가 있거나 만들기 비싼 객체(`OkHttpClient`, `Retrofit`)에만 붙인다. 가변 상태가 없는 Repository·UseCase에는 붙이지 않아도 된다.

근거: [Recommendations: Scope to a component when necessary](https://developer.android.com/topic/architecture/recommendations#handle-dependencies)

### 5-3. 설정 값 `권장`

API 기본 주소, 이미지 서버 주소는 코드에 흩어 두지 않고 `:data`의 `BuildConfig` 필드나 상수 한 곳에 둔다.

```kotlin
// ❌ ApiModule.kt와 Pokemon.kt에 서버 주소가 각각 박혀 있다
.baseUrl("https://pokeapi.co/api/v2/")
```
