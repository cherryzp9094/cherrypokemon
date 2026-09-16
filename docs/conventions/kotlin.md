# Kotlin

기본은 [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide)(이하 스타일 가이드)와 [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)(이하 코딩 컨벤션)이다. 둘이 다르면 스타일 가이드를 따른다. 포맷 규칙은 도구로 검사한다. ([build.md 5](build.md#5-포맷-검사-필수))

## 1. 이름

### 1-1. 기본 규칙 `필수`

| 대상 | 규칙 | 예 | 근거 |
|---|---|---|---|
| 식별자 | ASCII 글자·숫자만(`\w+`). `mName`, `kName` 같은 접두사·접미사 금지 | | 스타일 가이드 |
| 클래스, 인터페이스, object | PascalCase 명사 | `PokemonDetail` | 스타일 가이드 |
| 함수 | camelCase 동사구 | `refreshPokemonDetail()` | 스타일 가이드 |
| 프로퍼티, 변수, 파라미터 | camelCase 명사구 | `refreshState` | 스타일 가이드 |
| 상수 | UPPER_SNAKE_CASE. 커스텀 getter 없고 내용이 깊이 불변인 `val`이며 `object`나 top-level에 있을 때만. 스칼라 값이면 `const` | `const val POKEMON_PAGE_SIZE = 20` | 스타일 가이드 |
| backing property | `_` + 실제 프로퍼티 이름 | `_uiState` | 스타일 가이드 |
| 타입 변수 | 대문자 한 글자(+숫자) 또는 클래스 이름 + `T` | `T`, `RequestT` | 스타일 가이드 |
| 약어 | 단어로 취급해 camel case | `XmlHttpRequest`, `newCustomerId`, `supportsIpv6OnIos` | 스타일 가이드 |
| `@Composable` (Unit 반환) | PascalCase 명사. 동사 금지 | `PokemonCard()` | 스타일 가이드, [Compose API guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md) |
| `@Composable` (값 반환) | camelCase. 내부에서 `remember`한 가변 객체를 돌려주면 `remember` 접두사 | `rememberNavigationState()` | Compose API guidelines |
| `CompositionLocal` | `Local` 접두사, 접미사로 쓰지 않음 | `LocalExtendedColors` | Compose API guidelines |

```kotlin
// ❌ 클래스가 camelCase
data class goPokemonDetail(...)

// ❌ 약어를 대문자로 이어 씀
fun provideOKHttpClient(...)

// ❌ 동사로 시작하는 Unit 반환 Composable
@Composable fun RenderPokemonCard(...)
```

- 이름에 의미 없는 단어(`Util`, `Manager`, `Wrapper`)를 쓰지 않는다. `권장` — 코딩 컨벤션

### 1-2. 패키지 `필수`

- 전부 소문자이고, 단어는 밑줄 없이 이어 붙인다.
- `com.cherryzp.cherrypokemon.` + 모듈 경로. 모듈의 `namespace`와 같다.

```kotlin
// ❌ camelCase, 같은 단어 반복, 모듈 namespace 밖
package com.cherryzp.cherrypokemon.ui.view.pokemonDetail
package com.cherryzp.cherrypokemon.ui.view.base.base
package com.cherryzp.consts
```

근거: [스타일 가이드: Package names](https://developer.android.com/kotlin/style-guide#package_names)

### 1-3. 역할별 이름 `선택`

공식 [Recommendations: Naming conventions](https://developer.android.com/topic/architecture/recommendations#naming-conventions)의 등급은 Optional이다. 이 프로젝트는 아래 이름으로 통일한다.

| 역할 | 이름 | 예 | 근거 |
|---|---|---|---|
| 화면 Composable | `<화면>Screen`. ViewModel을 받는 것과 상태를 받는 것을 같은 이름으로 오버로드 | `PokemonDetailScreen` | [UI layer](https://developer.android.com/topic/architecture/ui-layer), [Previews](https://developer.android.com/develop/ui/compose/tooling/previews) 예시, NiA |
| ViewModel | `<화면>ViewModel` | `PokemonDetailViewModel` | 공식 예시 전반 |
| UI 상태 | `<기능>UiState` | `PokemonDetailUiState` | [UI layer](https://developer.android.com/topic/architecture/ui-layer) |
| SideEffect | `<화면>SideEffect` | `PokemonDetailSideEffect` | Orbit |
| 내비게이션 키 | `<화면>NavKey` | `PokemonDetailNavKey` | NiA |
| 이동 함수 | `Navigator.navigateTo<화면>()` | `navigateToPokemonDetail()` | NiA |
| entry builder | `<화면>EntryBuilder()` / 파일 `<화면>EntryBuilder.kt` | `pokemonDetailEntryBuilder()` | [Navigation 3: Modularize](https://developer.android.com/guide/navigation/navigation-3/modularize) |
| Repository | `<데이터>Repository` / 구현은 방식을 드러내는 이름, 없으면 `Default` 접두사 | `PokemonRepository` / `OfflineFirstPokemonRepository` | [Data layer](https://developer.android.com/topic/architecture/data-layer), Recommendations |
| 데이터 소스 | `<데이터><소스>DataSource` (Remote/Local 또는 Network/Disk) | `PokemonNetworkDataSource` | [Data layer](https://developer.android.com/topic/architecture/data-layer) |
| 데이터 소스 구현 | 구현 기술 + 대상 | `RetrofitPokemonNetwork` | NiA |
| 네트워크 모델 / 엔티티 / 외부 모델 | `Network<데이터>` / `<데이터>Entity` / `<데이터>` | `NetworkPokemon` / `PokemonEntity` / `Pokemon` | [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first) |
| 모델 변환 | 네트워크 → 엔티티 `asEntity()`, 엔티티 → 외부 모델 `asExternalModel()` | | 같음 |
| DAO / RemoteMediator | `<데이터>Dao` / `<데이터>RemoteMediator` | `PokemonDao` | [Room](https://developer.android.com/training/data-storage/room), [Paging](https://developer.android.com/topic/libraries/architecture/paging/v3-network-db) |
| UseCase | 현재형 동사 + 명사 + `UseCase` | `GetPokemonWithSpeciesUseCase` | [Domain layer](https://developer.android.com/topic/architecture/domain-layer) |
| 테스트 대역 | `Fake` 접두사 | `FakePokemonRepository` | Recommendations |
| 테스트 클래스 | `<대상>Test`, 통합 테스트는 `<대상>IntegrationTest` | `PokemonDetailViewModelTest` | 스타일 가이드 |
| Hilt 모듈 | 제공 대상 + `Module` | `NetworkModule` | NiA |

| 함수 | 이름 | 예 | 근거 |
|---|---|---|---|
| Flow를 돌려주는 조회 | `get<모델>Stream()`, 목록이면 복수형 | `getPokemonsStream()` | Recommendations |
| 한 번 조회 | `get<모델>()` | `getPokemonSpecies(pokeId)` | 스타일 가이드(동사구) |
| 네트워크 → 로컬 갱신 | `refresh<모델>()` | `refreshPokemonDetail(pokeId)` | 이 프로젝트 |
| ViewModel 공개 함수 | 처리하는 동작의 동사 | `retry()` | [UI events](https://developer.android.com/topic/architecture/ui-layer/events): `validateInput()`, `login()` |
| Composable 이벤트 파라미터 | `on` + 대상 + 동작 | `onPokemonClick`, `onBackClick` | UI events: `onExpandClicked`, `onValueChange` |

## 2. 파일 `필수`

| 규칙 | 근거 |
|---|---|
| 최상위 클래스·인터페이스 하나면 그 이름.kt | 스타일 가이드, 코딩 컨벤션 |
| 최상위 선언이 여러 개면 내용을 설명하는 PascalCase 이름 | 같음 |
| 한 파일은 한 주제에 집중한다. 의미적으로 밀접한 선언은 같은 파일에 둬도 되지만 수백 줄을 넘기지 않는다 | 같음 |
| 확장 함수는 그 클래스의 모든 사용자에게 필요하면 클래스와 같은 파일, 특정 사용처에만 필요하면 그 코드 옆에 둔다. 한 클래스의 확장 함수만 모으는 파일을 만들지 않는다 | 코딩 컨벤션: "Avoid creating files just to hold all extensions of some class." |
| 구성 순서: 라이선스(여러 줄 `/* */`) → 파일 어노테이션 → `package` → `import` → 최상위 선언, 사이는 빈 줄 하나 | 스타일 가이드 |
| 클래스 멤버 순서: 프로퍼티·초기화 블록 → 보조 생성자 → 메서드 → companion object. 알파벳·가시성 순이 아니라 관련 있는 것끼리, 오버로드는 붙여서 | 코딩 컨벤션 |

## 3. 포맷 `필수`

| 규칙 | 근거 |
|---|---|
| 들여쓰기 4칸, 탭 금지 | 스타일 가이드 |
| 한 줄 100자 (예외: `package`·`import`, 줄일 수 없는 URL, 셸 명령) | 스타일 가이드 |
| `import`는 하나의 목록으로 ASCII 정렬, 와일드카드 import 금지 | 스타일 가이드 |
| `if`/`for`/`when` 분기/`do`/`while`은 중괄호 필수 (한 줄에 들어가는 `if` 식·`when` 분기 제외), K&R 스타일 | 스타일 가이드 |
| 줄바꿈은 높은 구문 단위에서. 연산자 뒤, `.` `?.` `::` 앞 | 스타일 가이드 |
| 함수 시그니처가 넘치면 파라미터마다 한 줄(+4), `)`와 반환 타입은 다음 줄 | 스타일 가이드 |
| `:` 앞 공백은 상위 타입 선언과 `where` 제약에서만 (`class A : B`) | 스타일 가이드 |
| 여러 줄로 나눈 선언부 끝에 trailing comma | 코딩 컨벤션 |
| 불필요한 구문 생략: `: Unit`, 세미콜론, 단순 문자열 템플릿의 중괄호 | 코딩 컨벤션 |

```kotlin
// ❌
class PokemonDetailActivity: BaseActivity<...>()

// ✅
class PokemonDetailActivity : BaseActivity<...>()
```

## 4. KDoc `필수`

- `public` 타입과 그 `public` / `protected` 멤버에는 KDoc을 쓴다. 이름만으로 더 할 말이 없는 단순 멤버(`getFoo`, `foo`)와 override는 생략할 수 있다.
- 첫 줄은 요약 조각(명사구·동사구)이다. "이 함수는 ~를 반환한다" 식으로 쓰지 않는다.
- 파라미터와 반환값은 `@param`, `@return` 대신 본문에 `[파라미터]` 링크로 설명한다. 길어질 때만 태그를 쓴다.
- 짧으면 한 줄 형식(`/** ... */`)을 쓴다.

```kotlin
/** 포켓몬 데이터를 로컬 데이터베이스 기준으로 제공하고 네트워크에서 받아 로컬을 갱신하는 저장소. */
interface PokemonRepository {
    /** [pokeId] 포켓몬의 상세 정보. 로컬에 아직 없으면 `null`을 내보낸다. */
    fun getPokemonDetailStream(pokeId: Int): Flow<PokemonDetail?>
}
```

근거: [스타일 가이드: Documentation](https://developer.android.com/kotlin/style-guide#documentation), [코딩 컨벤션: Documentation comments](https://kotlinlang.org/docs/coding-conventions.html#documentation-comments)

## 5. 관용 표현 `권장`

| 규칙 | 근거 |
|---|---|
| 초기화 후 바꾸지 않으면 `val`. 바꾸지 않는 컬렉션은 불변 인터페이스(`List`, `Set`, `Map`)와 `listOf()` | 코딩 컨벤션: Immutability |
| 오버로드보다 기본 인자 | 코딩 컨벤션: Default parameter values |
| 같은 원시 타입 인자가 여럿이거나 Boolean 인자면 이름 붙인 인자로 호출 | 코딩 컨벤션: Named arguments |
| `if`, `when`, `try`는 식 형태 우선 | 코딩 컨벤션: Conditional statements |
| 예외를 던지지 않고, 계산이 싸고, 상태가 같으면 같은 값을 돌려주면 함수 대신 프로퍼티 | 코딩 컨벤션: Functions vs properties |
| 한 객체를 주로 다루는 함수는 확장 함수로, 가시성은 최대한 좁게 | 코딩 컨벤션: Extension functions |
| 특별한 의미가 있는 팩토리 함수는 클래스와 다른 이름 (`fromPolar`) | 코딩 컨벤션: Factory functions |
| 짧고 중첩되지 않은 람다는 `it`, 중첩 람다는 파라미터 이름을 쓴다 | 코딩 컨벤션: Lambda parameters |
| 표현식 하나로 끝나는 함수는 expression body | 코딩 컨벤션: Functions |
| 안정적으로 다룰 수 없는 상태를 가진 싱글턴이나 companion object 상태를 만들지 않는다. 공유 상태는 DI 스코프로 관리한다 | Compose API guidelines, [Recommendations: Handle dependencies](https://developer.android.com/topic/architecture/recommendations#handle-dependencies) |

## 6. 코루틴과 Flow

### 6-1. 디스패처는 주입한다 `필수`

`Dispatchers.IO` / `Dispatchers.Default`를 코루틴 생성이나 `withContext`에 직접 쓰지 않는다. `:core:common`의 qualifier로 주입받는다.

```kotlin
// :core:common
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Dispatcher(val cherryDispatcher: CherryDispatchers)

enum class CherryDispatchers { Default, IO }
```

```kotlin
// ❌
suspend fun fetchDominantColor(...) = withContext(Dispatchers.IO) { ... }
```

근거: [Coroutines best practices: Inject Dispatchers](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#inject-dispatchers) — "Note: While you might have seen hardcoded dispatchers in code snippets across this site ... In your application, you should inject dispatchers.", [Hilt: qualifier](https://developer.android.com/training/dependency-injection/hilt-android) `AnnotationRetention.BINARY`

### 6-2. 메인 스레드 안전 `필수`

- suspend 함수는 메인 스레드에서 불러도 안전해야 한다. 막히는 작업은 그 작업을 하는 클래스가 `withContext`로 옮긴다.
- Retrofit·Room의 suspend·Flow API는 이미 안전하므로 감싸지 않는다.
- Flow 생산자의 스레드를 바꿀 때는 `flowOn(주입받은 디스패처)`을 쓴다.
- 코루틴 안의 막히는 반복 작업은 `ensureActive()`로 취소를 확인한다.

근거: [Coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices): "This applies to all classes in your app", "Make your coroutine cancellable", [Data layer: Threading](https://developer.android.com/topic/architecture/data-layer), [Flow: flowOn](https://developer.android.com/kotlin/flow)

### 6-3. 코루틴을 시작하는 곳 `필수`

| 작업의 수명 | 시작하는 곳 |
|---|---|
| 화면이 떠 있는 동안 | ViewModel (Orbit `intent` / `onCreate`) |
| 앱이 켜져 있는 동안 (화면을 벗어나도 끝까지) | `@ApplicationScope` `CoroutineScope`를 주입받아 시작 |
| 앱이 종료돼도 끝까지 | WorkManager |
| 호출자 수명 안에서 병렬 처리 | `coroutineScope` / `supervisorScope` |

- ViewModel은 비즈니스 로직용 `suspend fun`을 공개하지 않는다.
- `GlobalScope`를 직접 쓰지 않는다.
- Composable에서 비즈니스 로직용 코루틴을 시작하지 않는다. UI 관련 작업(이미지 불러오기, 문자열 포맷)만 한다.

근거: [Coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices): "The ViewModel should create coroutines", "Views shouldn't directly trigger any coroutines to perform business logic", "Avoid GlobalScope", [Data layer: Types of data operations](https://developer.android.com/topic/architecture/data-layer)

### 6-4. 가변 타입을 공개하지 않는다 `필수`

`MutableStateFlow`, `MutableList` 등은 `private`으로 두고 읽기 전용 타입으로 공개한다.

근거: [Coroutines best practices: Don't expose mutable types](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#mutable-types)

### 6-5. 한 번 실행은 suspend, 계속 바뀌는 값은 Flow `필수`

- 데이터·비즈니스 레이어는 한 번 실행에 `suspend fun`, 변경 알림에 `Flow`를 노출한다.
- 값을 한 번만 내보내는 `flow { emit(...) }`로 한 번 실행을 감싸지 않는다. `Flow`를 만드는 함수는 `suspend`가 아니다.

```kotlin
// ❌
operator fun invoke(pokeNo: Int): Flow<PokemonDetail> = flow { emit(repository.fetch(pokeNo)) }
suspend fun fetchPokemonList(): Flow<PagingData<Pokemon>>
```

근거: [Coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#coroutines-data-layer), [Data layer: Expose APIs](https://developer.android.com/topic/architecture/data-layer)

### 6-6. 예외 `필수`

- 예외가 날 수 있으면 `viewModelScope` 등에서 시작한 코루틴 본문에서 잡는다.
- `IOException`처럼 구체적인 타입을 잡는다. `Exception` / `Throwable`은 피한다.
- `CancellationException`은 잡지 않거나, 잡았다면 다시 던진다.
- Flow의 예외는 `catch` 연산자로 처리한다. `catch` 후에는 흐름이 끝나므로 계속 받아야 하면 `retry`를 고려한다.

```kotlin
// ❌ 모든 예외를 잡고 로그만 남긴다
} catch (e: Exception) {
    e.printStackTrace()
    Color.White
}
```

근거: [Coroutines best practices: Watch out for exceptions](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#exceptions), [Flow: Catching unexpected exceptions](https://developer.android.com/kotlin/flow), [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first) Note(catch 후 흐름 종료)
