# Kotlin

기본은 [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide)와 [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)다. 둘이 다르면 Android 스타일 가이드를 따른다. 포맷 규칙은 Spotless + ktlint로 검사한다. ([build.md 4](build.md#4-포맷-검사-필수))

## 1. 이름

### 1-1. 기본 규칙 `필수`

| 대상 | 규칙 | 예 |
|---|---|---|
| 클래스, 인터페이스, object | PascalCase 명사 | `PokemonDetail` |
| 함수 | camelCase 동사구 | `getPokemonDetailStream()`, `retry()` |
| 프로퍼티, 변수 | camelCase 명사구 | `isRefreshing` |
| 상수 | UPPER_SNAKE_CASE. `const val`이거나, top-level·`object` 안의 커스텀 getter 없는 깊은 불변 `val`일 때만 | `const val POKEMON_PAGE_SIZE = 20` |
| backing property | `_` + 공개 프로퍼티 이름 | `_uiState` / `uiState` |
| `@Composable` (Unit 반환) | PascalCase 명사 | `PokemonCard()` |
| `@Composable` (값 반환) | camelCase. 내부에서 `remember`한 객체를 돌려주면 `remember` 접두사 | `rememberNavigationState()` |
| 약어 | 단어처럼 취급 | `OkHttpClient`, `XmlHttpRequest`, `supportsIpv6OnIos` |
| 타입 파라미터 | 대문자 한 글자, 또는 클래스 이름 + `T` | `T`, `RequestT` |

```kotlin
// ❌ 클래스가 camelCase라 함수 호출처럼 보인다
data class goPokemonDetail(...)

// ❌ 약어를 대문자로 이어 씀
fun provideOKHttpClient(...)

// ❌ 타입 이름에 종류를 붙임
enum class PokemonTypeEnum
```

- 타입 이름에 종류(`Enum`, `Class`, `Interface`)를 붙이지 않는다.
- 의미 없는 단어(`Util`, `Manager`, `Helper`, `Extend`, `Info`, `Data`)로 이름을 짓지 않는다.

근거: [Android Kotlin style guide: Naming](https://developer.android.com/kotlin/style-guide#naming), [Kotlin coding conventions: Naming rules](https://kotlinlang.org/docs/coding-conventions.html#naming-rules), [Compose API guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md)

### 1-2. 패키지 `필수`

- 전부 소문자이고, 단어는 밑줄 없이 이어 붙인다.
- `com.cherryzp.cherrypokemon.` + 모듈 경로로 시작한다. 모듈의 `namespace`와 같다.

| 모듈 | 패키지 |
|---|---|
| `:app` | `com.cherryzp.cherrypokemon` |
| `:core:data` | `com.cherryzp.cherrypokemon.core.data` |
| `:feature:pokemondetail:impl` | `com.cherryzp.cherrypokemon.feature.pokemondetail.impl` |

```kotlin
// ❌ camelCase 패키지, 같은 단어 반복, 모듈 namespace 밖의 패키지
package com.cherryzp.cherrypokemon.ui.view.pokemonDetail
package com.cherryzp.cherrypokemon.ui.view.base.base
package com.cherryzp.consts
```

근거: [Android Kotlin style guide: Package names](https://developer.android.com/kotlin/style-guide#package_names)

### 1-3. 파일 `필수`

- 클래스 하나만 있으면 클래스 이름과 같게 한다.
- 최상위 선언이 여러 개면 내용을 설명하는 PascalCase 이름을 짓는다.
- 확장 함수만 모은 파일은 `<대상>Extensions.kt`로 짓는다.
- sealed 계층은 부모와 한 파일에 둔다.
- 파일 구성 순서: 라이선스 주석(있으면) → 파일 어노테이션 → `package` → `import` → 최상위 선언. 각 부분 사이는 빈 줄 하나.

근거: [Android Kotlin style guide: Source files](https://developer.android.com/kotlin/style-guide#source_files)

### 1-4. 역할별 이름 `필수`

| 역할 | 이름 | 예 | 근거 |
|---|---|---|---|
| 화면 Composable | `<화면>Screen`. ViewModel을 받는 것과 상태를 받는 것을 같은 이름으로 오버로드 | `PokemonDetailScreen` | NiA |
| ViewModel | `<화면>ViewModel` | `PokemonDetailViewModel` | |
| 화면 상태 | `<화면>UiState` | `PokemonDetailUiState` | [UI layer](https://developer.android.com/topic/architecture/ui-layer#define-ui-state) |
| 화면 SideEffect | `<화면>SideEffect` | `PokemonDetailSideEffect` | |
| 내비게이션 키 | `<화면>NavKey` | `PokemonDetailNavKey` | NiA |
| 이동 함수 | `Navigator.navigateTo<화면>()` | `navigateToPokemonDetail()` | NiA |
| entry 함수 / 파일 | `<화면>Entry()` / `<화면>EntryProvider.kt` | `pokemonDetailEntry()` | NiA |
| Repository 인터페이스 | `<데이터>Repository` | `PokemonRepository` | [Data layer](https://developer.android.com/topic/architecture/data-layer#naming-conventions) |
| Repository 구현 | 구현 방식 + `<데이터>Repository`. `Impl` 접미사 금지 | `OfflineFirstPokemonRepository` | [Recommendations: Naming](https://developer.android.com/topic/architecture/recommendations#naming-conventions) |
| 네트워크 데이터 소스 | 인터페이스 `<데이터>NetworkDataSource`, 구현 `Retrofit<데이터>Network` | `PokemonNetworkDataSource`, `RetrofitPokemonNetwork` | [Data layer](https://developer.android.com/topic/architecture/data-layer#naming-conventions), NiA |
| 네트워크 모델 | `Network<데이터>` | `NetworkPokemonDetail` | [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first) |
| DB 엔티티 / DAO | `<데이터>Entity` / `<데이터>Dao` | `PokemonEntity` / `PokemonDao` | 같음 |
| 모델 변환 | 네트워크 → 엔티티 `asEntity()`, 엔티티 → 도메인 `asExternalModel()` | `NetworkPokemon.asEntity()` | 같음 |
| RemoteMediator | `<데이터>RemoteMediator` | `PokemonRemoteMediator` | [Paging](https://developer.android.com/topic/libraries/architecture/paging/v3-network-db) |
| UseCase | 현재형 동사 + 명사 + `UseCase` | `GetPokemonWithSpeciesUseCase` | [Domain layer](https://developer.android.com/topic/architecture/domain-layer#conventions) |
| 테스트 대역 | `Fake` 접두사 | `FakePokemonRepository` | [Recommendations: Naming](https://developer.android.com/topic/architecture/recommendations#naming-conventions) |
| Hilt 모듈 | 제공 대상 + `Module` | `NetworkModule`, `DaosModule` | NiA |

| 함수 | 이름 | 예 |
|---|---|---|
| `Flow`를 돌려주는 조회 | `get<데이터>Stream()` | `getPokemonsStream()`, `getPokemonDetailStream(pokeId)` |
| 한 번 실행하는 조회 (`suspend`) | `get<데이터>()` | `getPokemonSpecies(pokeId)` |
| 네트워크 → 로컬 갱신 (`suspend`) | `refresh<데이터>()` | `refreshPokemonDetail(pokeId)` |
| DAO 조회 | `get<데이터>Entity()` / `get<데이터>Entities()` | `getPokemonDetailEntity(pokeId)` |
| DAO 쓰기 | `upsert<데이터>()` / `delete<데이터>()` | `upsertPokemons(entities)` |
| ViewModel 공개 함수 | 하는 일을 나타내는 동사구 | `retry()`, `toggleFavorite()` |
| Composable 이벤트 파라미터 | `on` + 대상 + 동작 | `onPokemonClick`, `onBackClick` |

```kotlin
// ❌ 이벤트 파라미터가 "무엇을 할지"를 정해버린다
goPokemonDetail: (Int, Int) -> Unit,
updateBackgroundColor: (Int, Color) -> Unit,

// ✅ 무슨 일이 일어났는지만 알린다
onPokemonClick: (pokeId: Int) -> Unit,
```

근거: [Recommendations: Naming conventions](https://developer.android.com/topic/architecture/recommendations#naming-conventions)

## 2. 포맷 `필수`

| 규칙 | 근거 |
|---|---|
| 들여쓰기 4칸, 탭 금지 | Android 스타일 가이드 |
| 한 줄 100자 (`package`, `import`, URL 제외) | Android 스타일 가이드 |
| 와일드카드 import 금지 | Android 스타일 가이드 |
| 여러 줄 블록에는 중괄호 필수, K&R 스타일 | Android 스타일 가이드 |
| 클래스 헤더의 상위 타입 앞 `:`에 공백 (`class A : B`) | Kotlin 코딩 컨벤션 |
| 여러 줄로 나눈 파라미터·인자·컬렉션 끝에 trailing comma | Kotlin 코딩 컨벤션 |
| 같은 타입 인자가 둘 이상이거나 Boolean 인자는 이름 붙인 인자로 호출 | Kotlin 코딩 컨벤션 |
| 표현식 하나로 끝나는 함수는 expression body | Kotlin 코딩 컨벤션 |

```kotlin
// ❌
class PokemonDetailActivity: BaseActivity<...>()

// ✅
class PokemonDetailActivity : BaseActivity<...>()
```

근거: [Android Kotlin style guide: Formatting](https://developer.android.com/kotlin/style-guide#formatting), [Kotlin coding conventions: Trailing commas](https://kotlinlang.org/docs/coding-conventions.html#trailing-commas)

## 3. KDoc `필수`

- `public` 타입과 `public` / `protected` 멤버에는 KDoc을 쓴다. 이름만으로 뜻이 분명한 단순 멤버(예: `getFoo()`)는 생략할 수 있다.
- 첫 줄은 요약 문장이다. `@param`, `@return`은 설명이 더 필요할 때만 쓴다.
- 모듈 밖에서 쓸 필요가 없는 선언은 처음부터 `internal`이다. ([architecture.md 1-3](architecture.md#1-3-공개-범위-필수))
- 구현 안의 주석은 코드로 알 수 없는 **이유**만 적는다.

```kotlin
/**
 * 포켓몬 데이터를 로컬 데이터베이스 기준으로 제공하고, 네트워크에서 받아 로컬을 갱신한다.
 */
interface PokemonRepository {
    /** 목록을 페이지 단위로 제공한다. 로컬에 없는 페이지는 네트워크에서 받아 저장한 뒤 내보낸다. */
    fun getPokemonsStream(): Flow<PagingData<Pokemon>>
}
```

근거: [Android Kotlin style guide: Documentation](https://developer.android.com/kotlin/style-guide#documentation)

## 4. 코루틴과 Flow

### 4-1. 디스패처는 주입한다 `필수`

`Dispatchers.IO` / `Dispatchers.Default`를 코드에 직접 쓰지 않는다. `:core:common`의 qualifier로 주입받는다.

```kotlin
// :core:common
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val cherryDispatcher: CherryDispatchers)

enum class CherryDispatchers { Default, IO }
```

```kotlin
// ❌
suspend fun fetchDominantColor(...) = withContext(Dispatchers.IO) { ... }

// ✅
internal class OfflineFirstPokemonRepository @Inject constructor(
    ...
    @Dispatcher(CherryDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : PokemonRepository
```

근거: [Coroutines best practices: Inject Dispatchers](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#inject-dispatchers), NiA `NiaDispatchers`

### 4-2. suspend 함수는 메인 스레드에서 불러도 안전해야 한다 `필수`

- 막히는 작업은 그 함수 안에서 `withContext`로 옮긴다. 호출하는 쪽이 스레드를 신경 쓰지 않게 한다.
- Retrofit의 `suspend` 함수와 Room의 `suspend` / `Flow` DAO는 이미 메인 스레드에서 안전하므로 감싸지 않는다.
- 코루틴 안에서 `Future.get()`, `Thread.sleep()`, `runBlocking` 같은 막히는 호출을 하지 않는다.

```kotlin
// ❌ IO 스레드 하나를 다운로드가 끝날 때까지 붙잡는다
Glide.with(context).asBitmap().load(imageUrl).submit().get()
```

근거: [Coroutines best practices: Main-safe](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#main-safe)

### 4-3. 코루틴을 시작하는 곳 `필수`

| 작업의 수명 | 시작하는 곳 |
|---|---|
| 화면이 살아 있는 동안 | ViewModel (Orbit `intent` / `onCreate`) |
| 화면을 벗어나도 끝까지 해야 함 | `:core:common`의 `@ApplicationScope` `CoroutineScope`를 주입받아 시작 |
| 앱 프로세스가 종료돼도 끝까지 해야 함 | WorkManager |

- ViewModel은 `suspend fun`을 공개하지 않는다.
- `GlobalScope`를 쓰지 않는다.
- Composable에서 데이터 작업용 코루틴을 시작하지 않는다. `rememberCoroutineScope`는 스크롤·애니메이션 같은 UI 동작에만 쓴다.

근거: [Coroutines best practices: ViewModel should create coroutines](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#viewmodel-coroutines), [Avoid GlobalScope](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#global-scope), [Data layer](https://developer.android.com/topic/architecture/data-layer)

### 4-4. 가변 타입을 공개하지 않는다 `필수`

`MutableStateFlow`, `MutableList`, `SnapshotStateList`는 `private`으로 두고 읽기 전용 타입으로 공개한다.

근거: [Coroutines best practices: Don't expose mutable types](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#mutable-types)

### 4-5. 한 번 실행은 suspend, 계속 바뀌는 값은 Flow `필수`

- 값을 한 번만 내보내는 `flow { emit(...) }`를 만들지 않는다.
- `Flow`를 돌려주는 함수는 `suspend`가 아니다.

```kotlin
// ❌ 한 번 조회를 Flow로 감싸고, 받는 쪽은 다시 single()로 푼다
operator fun invoke(pokeNo: Int): Flow<PokemonDetail> = flow { emit(repository.fetch(pokeNo)) }

// ❌ Flow를 만드는 일은 suspend가 아니다
suspend fun fetchPokemonList(): Flow<PagingData<Pokemon>>
```

근거: [Coroutines best practices: Data and business layer should expose suspend functions and Flows](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#coroutines-data-layer)

### 4-6. 예외 `필수`

- 실제로 날 수 있는 구체적인 타입만 잡는다. (`IOException`, `HttpException`, `SerializationException`, `SQLiteException`)
- `Exception` / `Throwable`을 잡아야 하는 경계(예: `Result`로 감싸는 곳)에서는 `CancellationException`을 먼저 다시 던진다.
- suspend 호출을 `runCatching`으로 감싸지 않는다. `CancellationException`까지 잡는다.
- 잡은 예외를 로그 출력만 하고 버리지 않는다. 상태나 SideEffect로 바꾸거나 다시 던진다.

```kotlin
// ❌
} catch (e: Exception) {
    e.printStackTrace()
    Color.White
}
```

근거: [Coroutines best practices: Watch out for exceptions](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#exceptions)

## 5. 기타

| 규칙 | 강도 | 근거 |
|---|---|---|
| `!!`를 쓰지 않는다. 없으면 안 되는 값은 `checkNotNull(value) { "이유" }` / `requireNotNull` | `필수` | 실패 원인이 메시지로 남는다 |
| sealed 계층의 인자 없는 타입은 `data object` | `필수` | `toString()`이 이름을 보여준다 |
| 공용 부모 클래스(`BaseActivity`, `BaseViewModel`)를 만들지 않는다. 동작은 함수·Composable·위임으로 나눈다 | `필수` | NiA. Compose는 상속이 아닌 조합으로 UI를 만든다 |
| 상태를 가진 싱글턴 `object`를 만들지 않는다. Hilt 스코프로 관리한다 | `필수` | [Recommendations: Handle dependencies](https://developer.android.com/topic/architecture/recommendations#handle-dependencies) |
| 스코프 함수(`let`, `apply` 등)는 null 처리와 객체 설정에 쓰고 중첩하지 않는다 | `권장` | [Kotlin coding conventions: Scope functions](https://kotlinlang.org/docs/coding-conventions.html#scope-functions-apply-with-run-also-let) |
