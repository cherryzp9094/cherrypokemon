# Kotlin

기본은 [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide)와 [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)다. 여기에는 자주 어기는 것과 이 프로젝트에서 정한 이름 규칙만 적는다.

## 1. 이름

### 1-1. 기본 규칙 `필수`

| 대상 | 규칙 | 예 |
|---|---|---|
| 클래스, 인터페이스, object | PascalCase 명사 | `PokemonDetail` |
| 함수 | camelCase 동사구 | `getPokemonDetail()`, `retry()` |
| 프로퍼티, 변수 | camelCase 명사구 | `backgroundColor` |
| 상수 | UPPER_SNAKE_CASE. `const val`이거나, top-level·`object` 안의 커스텀 getter 없는 불변 `val`일 때만 | `const val PAGE_SIZE = 20` |
| backing property | `_` + 공개 프로퍼티 이름 | `_uiState` / `uiState` |
| `@Composable` (Unit 반환) | PascalCase 명사 | `PokemonCard()` |
| 약어 | 단어처럼 취급 | `OkHttpClient`, `provideOkHttpClient()`, `XmlHttpRequest` |

```kotlin
// ❌ MainContract.kt: 클래스가 camelCase라 함수 호출처럼 보인다
data class goPokemonDetail(...)

// ❌ ApiModule.kt: 약어를 대문자로 이어 씀
fun provideOKHttpClient(...)
```

- 타입 이름에 종류를 붙이지 않는다. `PokemonTypeEnum` → `PokemonType`. `권장`
- 의미 없는 단어(`Util`, `Manager`, `Helper`, `Extend`)로 이름을 짓지 않는다. `권장`

근거: [Android Kotlin style guide: Naming](https://developer.android.com/kotlin/style-guide#naming), [Kotlin coding conventions: Naming rules](https://kotlinlang.org/docs/coding-conventions.html#naming-rules)

### 1-2. 패키지 `필수`

- 전부 소문자이고, 단어는 밑줄 없이 이어 붙인다.
- 모듈의 namespace로 시작한다: `com.cherryzp.cherrypokemon`, `com.cherryzp.domain`, `com.cherryzp.data`, `com.cherryzp.core.common`

| 지금 | 문제 | 바꿀 이름 |
|---|---|---|
| `ui/view/pokemonDetail` | camelCase | `feature/pokemondetail` |
| `ui/view/base/base` | 같은 단어 반복 | 삭제 (Orbit 전환) |
| `extend` | 관용 표현이 아님 | `extension` |
| `com.cherryzp.consts` (`:domain`) | 모듈 namespace 밖 | 삭제 (NavKey로 대체) |

`:app`의 패키지 구조는 [architecture.md](architecture.md#5-app-패키지-구조-권장)를 따른다.

근거: [Android Kotlin style guide: Package names](https://developer.android.com/kotlin/style-guide#package_names)

### 1-3. 파일 이름 `필수`

- 클래스 하나만 있으면 클래스 이름과 같게 한다.
- 최상위 선언이 여러 개면 내용을 설명하는 PascalCase 이름을 짓는다. 확장 함수 파일은 `<대상>Extensions.kt`. (예: `NullableExtensions.kt`)
- sealed 계층은 부모와 한 파일에 둔다.

근거: [Android Kotlin style guide: Source files](https://developer.android.com/kotlin/style-guide#source_files)

### 1-4. 이 프로젝트의 이름 규칙

| 종류 | 이름 | 예 | 근거 |
|---|---|---|---|
| 화면 상태 | `<화면>UiState` | `PokemonDetailUiState` | [UI layer](https://developer.android.com/topic/architecture/ui-layer#define-ui-state) |
| ViewModel | `<화면>ViewModel` | `PokemonDetailViewModel` | |
| ViewModel을 연결하는 Composable | `<화면>Route` | `PokemonDetailRoute` | |
| 상태만 받는 Composable | `<화면>Screen` | `PokemonDetailScreen` | |
| 내비게이션 키 | `<화면>NavKey` | `PokemonDetailNavKey` | |
| Repository 인터페이스 | `<데이터>Repository` | `PokemonRepository` | [Data layer](https://developer.android.com/topic/architecture/data-layer#naming-conventions) |
| Repository 구현 | 구현 방식을 드러내는 이름, 없으면 `Default` 접두사. `Impl` 접미사는 쓰지 않는다 | `DefaultPokemonRepository` | [Recommendations: Naming](https://developer.android.com/topic/architecture/recommendations#naming-conventions) |
| UseCase | 현재형 동사 + 명사 + `UseCase` | `GetPokemonDetailUseCase` | [Domain layer](https://developer.android.com/topic/architecture/domain-layer#conventions) |
| Retrofit 인터페이스 | `<데이터>Api` | `PokemonApi` | |
| 응답 DTO | `<데이터>Response` | `PokemonDetailResponse` | |
| 테스트 대역 | `Fake` 접두사 | `FakePokemonRepository` | [Recommendations: Naming](https://developer.android.com/topic/architecture/recommendations#naming-conventions) |
| Hilt 모듈 | `<대상>Module` | `NetworkModule` | |

| 함수 종류 | 이름 | 예 |
|---|---|---|
| 계속 바뀌는 값을 주는 함수 (`Flow`) | `get<데이터>Stream()` | `getPokemonsStream()` |
| 한 번 조회 (`suspend`) | `get<데이터>()` | `getPokemonDetail(pokeId)` |
| Retrofit 호출 | `fetch<데이터>()` | `fetchPokemonDetail(pokeId)` |
| ViewModel 공개 함수 | 하는 일을 나타내는 동사구 | `retry()`, `userMessageShown()` |
| Composable 이벤트 파라미터 | `on` + 대상 + 동작 | `onPokemonClick`, `onBackClick` |

```kotlin
// ❌ MainScreen.kt: 이벤트 파라미터가 "무엇을 할지"를 정해버린다
goPokemonDetail: (Int, Int) -> Unit,
updateBackgroundColor: (Int, Color) -> Unit,

// ✅ 무슨 일이 일어났는지만 알린다
onPokemonClick: (pokeId: Int) -> Unit,
```

## 2. 포맷

| 규칙 | 강도 |
|---|---|
| 들여쓰기 4칸, 탭 금지 | `필수` |
| 한 줄 100자 (import, URL 제외) | `필수` |
| 와일드카드 import 금지 | `필수` |
| 클래스 헤더의 상위 타입 앞 `:`에 공백 (`class A : B`) | `필수` |
| 여러 줄로 나눈 파라미터·인자·컬렉션에 trailing comma | `권장` |
| 같은 타입 인자가 둘 이상이면 이름 붙인 인자 | `권장` |

```kotlin
// ❌ PokemonDetailActivity.kt, MainViewModel.kt
class PokemonDetailActivity: BaseActivity<...>()
): BaseViewModel<MainUiState>() {

// ✅
class PokemonDetailActivity : BaseActivity<...>()
) : BaseViewModel<MainUiState>() {
```

지금은 포맷 검사 도구가 없다. 개선 계획 Phase 6-2에서 ktlint를 붙이면 이 표는 도구 설정으로 옮긴다.

근거: [Android Kotlin style guide: Formatting](https://developer.android.com/kotlin/style-guide#formatting), [Kotlin coding conventions: Trailing commas](https://kotlinlang.org/docs/coding-conventions.html#trailing-commas)

## 3. 코루틴과 Flow

### 3-1. 디스패처는 주입한다 `필수`

`Dispatchers.IO` / `Dispatchers.Default`를 코드에 직접 쓰지 않는다. `:core:common`의 qualifier로 주입받는다. 테스트에서 `TestDispatcher`로 바꾸기 위해서다.

```kotlin
// :core:common
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: CherryDispatchers)

enum class CherryDispatchers { Default, IO }
```

```kotlin
// ❌ ColorExtend.kt
return withContext(Dispatchers.IO) { ... }

// ✅
class DefaultPokemonRepository @Inject constructor(
    private val pokemonApi: PokemonApi,
    @Dispatcher(CherryDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : PokemonRepository
```

Retrofit의 `suspend` 함수는 자체 스레드에서 실행되므로 `withContext`로 감싸지 않아도 된다. 파일 읽기, 비트맵 처리처럼 막히는 작업에만 쓴다.

근거: [Coroutines best practices: Inject Dispatchers](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#inject-dispatchers)

### 3-2. suspend 함수는 메인 스레드에서 불러도 안전해야 한다 `필수`

막히는 작업은 그 함수 안에서 `withContext`로 옮긴다. 호출하는 쪽이 스레드를 신경 쓰지 않게 한다. 코루틴 안에서 `Future.get()`, `Thread.sleep()` 같은 막히는 호출을 하지 않는다.

```kotlin
// ❌ ColorExtend.kt: IO 스레드 하나를 다운로드가 끝날 때까지 붙잡는다
Glide.with(context).asBitmap().load(imageUrl).submit().get()
```

근거: [Coroutines best practices: Suspend functions should be safe to call from the main thread](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#main-safe)

### 3-3. 코루틴은 ViewModel이 시작한다 `필수`

- ViewModel은 `suspend fun`을 공개하지 않는다. Orbit의 `intent { }`나 `viewModelScope`에서 시작한다.
- `GlobalScope`를 쓰지 않는다. 화면보다 오래 살아야 하는 작업은 Application 범위 `CoroutineScope`를 주입받는다.
- Composable에서 비즈니스 로직용 코루틴을 시작하지 않는다. `rememberCoroutineScope`는 스크롤·애니메이션 같은 UI 동작에만 쓴다.

```kotlin
// ❌ MainScreen.kt: 색상 계산(데이터 작업)을 Composable의 LaunchedEffect가 시작한다
LaunchedEffect(pokemon.id) {
    val color = fetchDominantColor(context, pokemon.imageUrl)
    updateBackgroundColor(pokemon.id, color)
}
```

근거: [Coroutines best practices: The ViewModel should create coroutines](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#viewmodel-coroutines), [Avoid GlobalScope](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#global-scope)

### 3-4. 가변 타입을 공개하지 않는다 `필수`

`MutableStateFlow`, `MutableList`, `SnapshotStateMap`은 `private`으로 두고 읽기 전용 타입으로 공개한다.

근거: [Coroutines best practices: Don't expose mutable types](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#mutable-types)

### 3-5. 한 번 조회는 suspend, 계속 바뀌는 값은 Flow `필수`

값을 한 번만 내보내는 `flow { emit(...) }`를 만들지 않는다.

```kotlin
// ❌ PokemonDetailUseCase.kt: 한 번 조회를 Flow로 감싸고, ViewModel은 다시 single()로 푼다
operator fun invoke(pokeNo: Int): Flow<PokemonDetail> =
    flow { emit(pokemonRepository.fetchPokemonDetail(pokeNo)) }

// ❌ PokemonRepository.kt: Pager를 만드는 일은 suspend가 아니다
suspend fun fetchPokemonList(): Flow<PagingData<Pokemon>>

// ✅
suspend fun getPokemonDetail(pokeId: Int): PokemonDetail
fun getPokemonsStream(): Flow<PagingData<Pokemon>>
```

근거: [Coroutines best practices: Data and business layer should expose suspend functions and Flows](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#coroutines-data-layer)

### 3-6. 예외는 구체적인 타입으로 잡는다 `필수`

- `IOException`, `HttpException`, `SerializationException`처럼 실제로 날 수 있는 타입만 잡는다.
- `Exception`이나 `Throwable`을 잡아야 한다면 `CancellationException`을 먼저 다시 던진다. 삼키면 코루틴 취소가 깨진다.
- suspend 호출을 `runCatching`으로 감싸지 않는다. `CancellationException`까지 잡는다.
- `e.printStackTrace()`로 끝내지 않는다. 에러를 상태로 바꾸거나 위로 던진다.

```kotlin
// ❌ ColorExtend.kt
} catch (e: Exception) {
    e.printStackTrace()
    Color.White
}
```

근거: [Coroutines best practices: Watch out for exceptions](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#exceptions)

## 4. 기타

| 규칙 | 강도 | 이유 |
|---|---|---|
| `!!`를 쓰지 않는다. 없으면 안 되는 값은 `checkNotNull(value) { "이유" }` | `권장` | 실패 원인이 메시지로 남는다 |
| sealed 계층의 인자 없는 타입은 `data object` | `권장` | `toString()`이 이름을 보여준다 |
| 공용 부모 클래스(`BaseActivity`, `BaseViewModel`)를 만들지 않는다 | `권장` | 상속보다 조합. 필요한 동작은 함수나 Composable로 나눈다 |
| 주석은 코드로 알 수 없는 **이유**만 적는다 | `선택` | 공식 스타일 가이드는 public 멤버에 KDoc을 요구하지만, 라이브러리가 아닌 앱 코드라 이름으로 드러나지 않을 때만 쓴다 |
