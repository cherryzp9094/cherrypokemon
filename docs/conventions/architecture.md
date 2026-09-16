# 아키텍처

> 코드 예시는 규칙을 보여주기 위한 것이다. 라이브러리 버전에 따라 API 이름이 다를 수 있으니 작성할 때 해당 버전 문서를 확인한다.

## 1. 모듈

### 1-1. 구성 `필수`

```
:app
:feature:pokemonlist:api        :feature:pokemondetail:api
:feature:pokemonlist:impl       :feature:pokemondetail:impl
:core:navigation   :core:designsystem   :core:ui
:core:domain       :core:data           :core:database   :core:network
:core:model        :core:common
:core:testing      :core:data-test
```

| 모듈 | 종류 | 담는 것 |
|---|---|---|
| `:app` | Android application | `Application`, `MainActivity`, 앱 전체 내비게이션(`NavDisplay`), 앱 수준 UI 상태 |
| `:feature:<화면>:api` | Android library | 그 화면의 `NavKey`, `Navigator.navigateTo<화면>()` |
| `:feature:<화면>:impl` | Android library | Screen, ViewModel, UiState, 화면 전용 컴포넌트, entry builder |
| `:core:navigation` | Android library | `NavigationState`, `Navigator` |
| `:core:designsystem` | Android library | 테마(색·타이포·모양), 도메인을 모르는 기본 컴포넌트, 아이콘 |
| `:core:ui` | Android library | 도메인 모델을 쓰는 공용 컴포넌트, Preview 데이터 |
| `:core:domain` | Android library | UseCase |
| `:core:data` | Android library | Repository 인터페이스와 구현, RemoteMediator, 네트워크 모델 → 엔티티 변환 |
| `:core:database` | Android library | Room 데이터베이스, 엔티티, DAO |
| `:core:network` | Android library | 네트워크 데이터 소스 인터페이스와 Retrofit 구현, 네트워크 모델 |
| `:core:model` | **JVM library** | 앱 전체가 쓰는 도메인 모델 |
| `:core:common` | **JVM library** | 디스패처 qualifier, 애플리케이션 `CoroutineScope`, `Result` |
| `:core:testing` | Android library | 테스트 규칙, 테스트 데이터, 공용 Fake |
| `:core:data-test` | Android library | 계측 테스트에서 Repository 바인딩을 Fake로 바꾸는 Hilt 모듈 |

- 새 화면(사용자 흐름 하나)을 만들면 `:feature:<화면>:api`와 `:impl`을 함께 만든다.
- Android 프레임워크가 필요 없는 모듈은 JVM library로 만든다. ("Prefer Kotlin & Java modules")

근거: [Modularization patterns](https://developer.android.com/topic/modularization/patterns) — data·feature·app·common·test 모듈 유형, [Navigation 3: Modularize](https://developer.android.com/guide/navigation/navigation-3/modularize) — "Create two submodules: api and impl for each feature", NiA `settings.gradle.kts`

### 1-2. 의존 방향 `필수`

```
:app ──┬──▶ :feature:*:impl ──┬──▶ :feature:*:api ──▶ :core:navigation
       ├──▶ :feature:*:api    ├──▶ :core:domain ──▶ :core:data ──┬──▶ :core:network  ──┐
       ├──▶ :core:navigation  ├──▶ :core:data                    ├──▶ :core:database ─┼──▶ :core:model
       └──▶ :core:designsystem└──▶ :core:ui ──▶ :core:designsystem└──▶ :core:common    │
                                        └────────────────────────────────────────────────┘
```

| 규칙 | 근거 |
|---|---|
| 레이어 방향은 UI → domain → data 한쪽뿐이다. data는 UI나 domain을 모른다 | [Guide to app architecture](https://developer.android.com/topic/architecture): "the domain layer depends on data layer classes" |
| `impl`은 다른 `impl`에 의존하지 않는다. 다른 화면으로 가려면 그 화면의 `api`에 의존한다 | [Navigation 3: Modularize](https://developer.android.com/guide/navigation/navigation-3/modularize): impl이 다른 feature의 api에 있는 키에 의존해 이동. [Modularization patterns](https://developer.android.com/topic/modularization/patterns): feature 사이는 app 모듈이 중재 |
| `api`는 `:core:navigation` 말고는 의존하지 않는다 | NiA `AndroidFeatureApiConventionPlugin` |
| `:app`은 시작 화면의 `api`를 포함해 필요한 `api`에 직접 의존한다. `implementation`은 전이되지 않는다 | [Dependencies](https://developer.android.com/build/dependencies) |
| `:core:model`과 `:core:common`은 프로젝트 모듈에 의존하지 않는다 | NiA |
| `:core:network`와 `:core:database`는 서로 의존하지 않는다. 둘을 조합하는 곳은 `:core:data`다 | [Data layer](https://developer.android.com/topic/architecture/data-layer): "Each data source class should have the responsibility of working with only one source of data" |

### 1-3. 공개 범위 `필수`

- 모듈 밖에서 쓸 필요가 없는 선언은 `internal` 또는 `private`이다. 데이터 소스 구현, Repository 구현, Retrofit 인터페이스, 화면 Composable이 여기에 해당한다.
- `:feature:*:impl`이 밖에 공개하는 것은 entry builder 함수다.
- 의존은 `implementation`으로 선언한다. `api`는 공개 시그니처에 그 모듈의 타입이 드러날 때만 쓴다.

근거: [Guide to app architecture](https://developer.android.com/topic/architecture) — "Expose as little as possible from each module", [Modularization patterns](https://developer.android.com/topic/modularization/patterns) — "Data sources should only be accessible by repositories from the same module ... private or internal", "prefer implementation over api"

## 2. 레이어

| 레이어 | 모듈 | 책임 | 노출 방식 |
|---|---|---|---|
| UI | `:feature:*:impl`, `:core:ui`, `:core:designsystem` | 데이터를 화면에 표시하고 사용자 입력을 받는다 | ViewModel이 UI 상태를 노출 |
| Domain (선택) | `:core:domain` | 복잡하거나 여러 ViewModel이 재사용하는 비즈니스 로직 | `operator fun invoke()` |
| Data | `:core:data`, `:core:network`, `:core:database` | 앱 데이터와 비즈니스 로직 | 한 번 실행은 `suspend fun`, 계속 바뀌는 값은 `Flow` |

| 규칙 | 강도 | 근거 |
|---|---|---|
| 데이터 레이어를 명확히 두고, 데이터 소스가 하나여도 Repository를 만든다 | `필수` | [Recommendations](https://developer.android.com/topic/architecture/recommendations#layered-architecture) Strongly recommended |
| UI 레이어(Composable, ViewModel)는 DB·네트워크 같은 데이터 소스와 직접 상호작용하지 않는다 | `필수` | 같음 |
| 레이어 사이 통신은 코루틴과 Flow로 한다 | `필수` | 같음 |
| UseCase는 필요할 때만 만든다 | `필수` | [Domain layer](https://developer.android.com/topic/architecture/domain-layer): "You should only use it when needed". 기준은 [data.md 6](data.md#6-usecase) |
| 비즈니스 로직은 UI 레이어에 두지 않는다. UI 로직(문자열 리소스 선택, 화면 이동, 스낵바 표시)은 UI에 둔다 | `필수` | [UI layer](https://developer.android.com/topic/architecture/ui-layer): "Business logic is usually placed in the domain or data layers, but never in the UI layer", "Keep UI logic in the UI, not in the ViewModel" |
| 앱 컴포넌트(Activity 등)에 데이터나 상태를 저장하지 않는다 | `필수` | [Guide to app architecture](https://developer.android.com/topic/architecture): "don't store any application data or state in your app components" |

## 3. 상태 관리

### 3-1. 단방향 데이터 흐름 `필수`

```
사용자 입력 ─▶ Screen 콜백 ─▶ ViewModel 함수 ─▶ Repository / UseCase
                                     │
Screen ◀── UI 상태 ◀────────────── reduce
Screen ◀── SideEffect ◀────────── postSideEffect   (Orbit, 라이브러리 예외)
```

- 상태는 ViewModel에서 UI로 내려가고, 입력은 UI에서 ViewModel로 올라간다.
- UI는 상태를 직접 바꾸지 않는다. ("Never modify the UI state in the UI directly unless the UI itself is the sole source of its data")

근거: [Recommendations](https://developer.android.com/topic/architecture/recommendations#ui-layer) — Follow UDF(Strongly recommended), [UI layer](https://developer.android.com/topic/architecture/ui-layer)

### 3-2. ViewModel은 Orbit으로 만든다 `필수`

[라이브러리 예외](README.md#라이브러리-예외)에 따라 ViewModel의 상태와 SideEffect는 Orbit이 관리한다. Orbit이 맡는 부분은 [Orbit 문서](https://orbit-mvi.org/)를 따르고, 그 밖의 ViewModel 규칙은 공식 문서를 따른다.

```kotlin
@HiltViewModel(assistedFactory = PokemonDetailViewModel.Factory::class)
class PokemonDetailViewModel @AssistedInject constructor(
    @Assisted private val pokeId: Int,
    private val pokemonRepository: PokemonRepository,
) : ViewModel(),
    OrbitContainerHost<PokemonDetailUiState, PokemonDetailUiState, PokemonDetailSideEffect> {

    override val container = orbitContainer<PokemonDetailUiState, PokemonDetailSideEffect>(
        initialState = PokemonDetailUiState(refreshState = RefreshState.Refreshing),
    ) {
        coroutineScope {
            launch { observePokemonDetail() }
            launch { refresh() }
        }
    }

    fun retry() = intent { refresh() }

    private suspend fun observePokemonDetail() = subIntent {
        repeatOnSubscription {
            pokemonRepository.getPokemonDetailStream(pokeId).collect { detail ->
                reduce { state.copy(pokemonDetail = detail) }
            }
        }
    }

    private suspend fun refresh() = subIntent {
        reduce { state.copy(refreshState = RefreshState.Refreshing) }
        try {
            pokemonRepository.refreshPokemonDetail(pokeId)
            reduce { state.copy(refreshState = RefreshState.Idle) }
        } catch (e: IOException) {
            reduce { state.copy(refreshState = RefreshState.Failed) }
            if (state.pokemonDetail != null) {
                postSideEffect(PokemonDetailSideEffect.ShowMessage(R.string.feature_pokemondetail_impl_error_refresh))
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(pokeId: Int): PokemonDetailViewModel
    }
}
```

| 규칙 | 강도 | 근거 |
|---|---|---|
| ViewModel은 화면(내비게이션 목적지) 단위로만 쓴다. 재사용 UI 컴포넌트의 상태는 일반 state holder 클래스에 둔다 | `필수` | [Recommendations](https://developer.android.com/topic/architecture/recommendations#viewmodel) Strongly recommended |
| `Activity`, `Context`, `Resources` 등 생명주기 관련 타입을 참조하지 않는다. 문자열은 `@StringRes Int`로 넘긴다 | `필수` | 같음 |
| `AndroidViewModel`을 쓰지 않는다 | `권장` | 같음 Recommended |
| ViewModel 함수·상태 필드 이름은 UI 구현을 드러내지 않게 일반적으로 짓는다 | `필수` | [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel): "ViewModels shouldn't know about the UI implementation details" |
| ViewModel을 다른 클래스·함수·UI 컴포넌트에 넘기지 않는다 | `필수` | 같음: "Don't pass ViewModels to other classes, functions or other UI components." |
| `init {}`이나 생성자에서 비동기 작업을 시작하지 않는다. 로딩과 스트림 구독은 `orbitContainer`의 `onCreate` 블록에서 한다 | `필수` | [State production](https://developer.android.com/topic/architecture/ui-layer/state-production): "Don't launch asynchronous operations in the init block or constructor of a ViewModel." / Orbit `LazyCreateContainerDecorator`: `onCreate`는 첫 구독이나 첫 intent 때 한 번 실행 |
| 끝나지 않는 Flow는 `repeatOnSubscription {}` 안에서 수집한다 | `필수` | [Orbit: Operators](https://orbit-mvi.org/Core/), [State production](https://developer.android.com/topic/architecture/ui-layer/state-production): 파이프라인은 "Lifecycle aware" |
| `reduce {}` 안에서는 새 상태만 계산한다 | `필수` | Orbit |
| 5초 이상 걸리는 작업은 `viewModelScope`에서 하지 않고 WorkManager로 보낸다 | `필수` | [State production](https://developer.android.com/topic/architecture/ui-layer/state-production): "Don't use the viewModelScope to run requests that last for 5 seconds or more." |
| 화면 인자는 NavKey 값을 assisted injection으로 받는다 | `필수` | [Hilt with Jetpack](https://developer.android.com/training/dependency-injection/hilt-jetpack), NiA `TopicViewModel` |

### 3-3. UI 상태

| 규칙 | 강도 | 근거 |
|---|---|---|
| 화면의 관련 상태는 하나의 불변 타입으로 노출하고 이름은 `<기능>UiState`로 짓는다 | `권장` | [Recommendations](https://developer.android.com/topic/architecture/recommendations#viewmodel) Expose a UI state(Recommended), [UI layer](https://developer.android.com/topic/architecture/ui-layer): "functionality + UiState" |
| 데이터·로딩·에러를 함께 담으면 `data class`, 서로 배타적이면 `sealed interface` | `권장` | 같음 |
| 모든 프로퍼티는 `val`이다 | `필수` | [UI layer](https://developer.android.com/topic/architecture/ui-layer) Immutability Key Point |
| 정상 흐름만 표현하지 않는다. 로딩과 에러를 상태로 표현한다 | `필수` | [UI layer](https://developer.android.com/topic/architecture/ui-layer): Show in-progress operations / Show errors |
| 초기 상태가 실제 화면과 어긋나지 않게 한다 (로딩을 곧 시작하면 초기값도 로딩) | `필수` | [UI layer](https://developer.android.com/topic/architecture/ui-layer): "the UI state is what the app says they should see" |
| 두 필드에서 계산되는 값은 필드로 중복 저장하지 않고 확장 프로퍼티로 계산한다 | `권장` | 같음: `val NewsUiState.canBookmarkNews` 예시 |
| `PagingData`는 UI 상태에 넣지 않는다 (3-5) | `필수` | 같음 |

```kotlin
internal data class PokemonDetailUiState(
    val pokemonDetail: PokemonDetail? = null,
    val refreshState: RefreshState,
)

internal enum class RefreshState { Refreshing, Idle, Failed }

// 캐시가 없고 새로고침이 실패했을 때만 에러 화면
internal val PokemonDetailUiState.showsError: Boolean
    get() = pokemonDetail == null && refreshState == RefreshState.Failed
```

### 3-4. SideEffect와 이벤트

[라이브러리 예외](README.md#라이브러리-예외)에 따라 Orbit의 SideEffect로 한 번만 처리할 이벤트(메시지 표시, 비즈니스 결과에 따른 이동)를 UI에 보낼 수 있다.

| 규칙 | 강도 | 근거 |
|---|---|---|
| SideEffect 타입은 화면마다 `sealed interface <화면>SideEffect`, 없으면 `Nothing` | `필수` | Orbit |
| UI에서 시작한 이동(버튼 클릭)은 ViewModel을 거치지 않고 Screen 콜백으로 처리한다 | `필수` | [UI events](https://developer.android.com/topic/architecture/ui-layer/events): "If the event is triggered in the UI because the user tapped on a button, the UI takes care of that by exposing the event to the caller composable." |
| 클릭으로 이동하는 콜백은 `dropUnlessResumed { }`로 감싼다 | `권장` | 같음 (전환 중 중복 이동 방지) |
| SideEffect는 한 곳(상태를 가진 Screen)에서만 수집한다 | `필수` | [UI events](https://developer.android.com/topic/architecture/ui-layer/events): 소비자가 여럿이면 구조 재검토 |

```kotlin
internal sealed interface PokemonDetailSideEffect {
    data class ShowMessage(@StringRes val messageId: Int) : PokemonDetailSideEffect
}
```

### 3-5. PagingData `필수`

`Flow<PagingData<T>>`는 ViewModel의 별도 프로퍼티로 노출하고 `cachedIn(viewModelScope)`를 붙인다.

```kotlin
val pokemons: Flow<PagingData<Pokemon>> =
    pokemonRepository.getPokemonsStream().cachedIn(viewModelScope)
```

근거: [UI layer](https://developer.android.com/topic/architecture/ui-layer): "do not represent it in an immutable UI state. Instead, expose it from the ViewModel independently in its own stream.", [Paging transform](https://developer.android.com/topic/libraries/architecture/paging/v3-transform): "Typically, you apply this operator [cachedIn] in your ViewModel"

### 3-6. 상태 수집 `필수`

- Orbit 컨테이너는 `collectAsState()` / `collectSideEffect()`로, 그 외 Flow는 `collectAsStateWithLifecycle()`로 수집한다.
- Flow 수집을 생명주기 이벤트에 맞춰 직접 시작·중지하거나, UI에서 `launch` / `launchIn`으로 직접 수집하지 않는다.

근거: [Recommendations](https://developer.android.com/topic/architecture/recommendations#ui-layer) Strongly recommended, [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle): "Do not manually start or stop Flow collection based on lifecycle events", [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) Warning, [Orbit Compose](https://orbit-mvi.org/Compose/)

### 3-7. 상태 보존 `필수`

| 데이터 | 보존 수단 |
|---|---|
| 앱 데이터 | 로컬 저장소(Room) |
| 화면 UI 상태 | ViewModel (프로세스가 종료되면 데이터 레이어에서 다시 만든다) |
| 비즈니스 로직에 쓰이는 작은 일시 상태(검색어 등) | `SavedStateHandle` |
| UI 로직에만 쓰이는 작은 일시 상태(펼침 여부 등) | `rememberSaveable` |

- saved state에는 ID 같은 최소한의 원시값만 담는다. 목록이나 큰 객체를 담지 않는다.
- 화면 UI 상태를 `SavedStateHandle`에 저장하지 않는다.
- 설정 변경을 피하려고 방향·크기 조절을 제한하거나 Activity 재생성을 끄지 않는다.

근거: [Save UI states](https://developer.android.com/topic/libraries/architecture/saving-states), [State saving in Compose](https://developer.android.com/develop/ui/compose/state-saving): "Screen UI state ... should not be stored in SavedStateHandle", [Configuration changes](https://developer.android.com/guide/topics/resources/runtime-changes): "Avoid opting out as a quick fix", "Don't put restrictions on orientation, aspect ratio, or resizability"

## 4. 단일 Activity와 Navigation 3

### 4-1. 요구 사항 `필수`

| 항목 | 값 |
|---|---|
| 라이브러리 | `navigation3-runtime`, `navigation3-ui`, `lifecycle-viewmodel-navigation3`, `hilt-lifecycle-viewmodel-compose`, (적응형) `adaptive-navigation3` |
| 플러그인 | `org.jetbrains.kotlin.plugin.serialization` |
| compileSdk | 36 이상 |

근거: [Navigation 3 get started](https://developer.android.com/guide/navigation/navigation-3/get-started), [Hilt with Jetpack](https://developer.android.com/training/dependency-injection/hilt-jetpack)

### 4-2. Activity는 하나 `필수`

- `MainActivity` 하나만 둔다. 화면은 Activity가 아니라 NavKey와 entry로 추가한다.
- `MainActivity`는 `enableEdgeToEdge()`, 테마 적용, `CherryPokemonApp()` 호출만 한다.
- Activity 생명주기 메서드(`onResume` 등)를 오버라이드하지 않는다. Compose의 `LifecycleStartEffect` / `LifecycleResumeEffect`를 쓴다.

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CherryPokemonTheme {
                CherryPokemonApp()
            }
        }
    }
}
```

근거: [Recommendations](https://developer.android.com/topic/architecture/recommendations#ui-layer) — Use a single-activity application(Strongly recommended), [Recommendations: Lifecycle](https://developer.android.com/topic/architecture/recommendations#lifecycle) Strongly recommended, [Guide to app architecture](https://developer.android.com/topic/architecture): "The primary role of an Activity is to host your app's UI"

### 4-3. NavKey와 이동 함수는 `:feature:*:api` `필수`

```kotlin
// :feature:pokemondetail:api  navigation/PokemonDetailNavKey.kt
@Serializable
data class PokemonDetailNavKey(val pokeId: Int) : NavKey

fun Navigator.navigateToPokemonDetail(pokeId: Int) {
    navigate(PokemonDetailNavKey(pokeId))
}
```

- 키는 `NavKey`를 구현하고 `@Serializable`을 붙인다. 인자가 없으면 `data object`, 있으면 `data class`.
- **키에는 ID 같은 원시값만 담는다.** 도메인 모델이나 앞 화면에서 계산한 표시용 값(예: 배경색)을 담지 않는다. 대상 화면이 ID로 데이터 레이어에서 불러온다.

근거: [Navigation 3: Save state](https://developer.android.com/guide/navigation/navigation-3/save-state) — `rememberNavBackStack`의 키 요구사항, [Modularization patterns](https://developer.android.com/topic/modularization/patterns): "You shouldn't pass objects as navigation arguments. Instead, use simple ids", [Navigation 3: Modularize](https://developer.android.com/guide/navigation/navigation-3/modularize), NiA `TopicNavKey`

### 4-4. entry builder는 `:feature:*:impl` `필수`

```kotlin
// :feature:pokemondetail:impl  navigation/PokemonDetailEntryBuilder.kt
fun EntryProviderScope<NavKey>.pokemonDetailEntryBuilder(
    navigator: Navigator,
    snackbarHostState: SnackbarHostState,
) {
    entry<PokemonDetailNavKey> { key ->
        PokemonDetailScreen(
            viewModel = hiltViewModel<PokemonDetailViewModel, PokemonDetailViewModel.Factory>(
                key = key.pokeId.toString(),
            ) { factory ->
                factory.create(key.pokeId)
            },
            onBackClick = navigator::goBack,
            snackbarHostState = snackbarHostState,
        )
    }
}
```

- entry builder는 `EntryProviderScope<NavKey>` 확장 함수로 impl 모듈에 둔다.
- Screen은 `Navigator`나 back stack을 받지 않고 콜백만 받는다. 콜백과 `Navigator`를 잇는 곳은 entry builder다.
- 화면 간 결과 전달이 필요하면 Screen은 콜백만 노출하고, entry builder에서 `ResultEventBus`로 보낸다.

근거: [Navigation 3: Modularize](https://developer.android.com/guide/navigation/navigation-3/modularize): "create extension functions on EntryProviderScope and move them into the impl module", "inject an object capable of modifying the app's navigation state into each builder function", [Return results](https://developer.android.com/guide/navigation/navigation-3/return-results): "don't access LocalResultEventBus directly inside your screen UI. Instead, expose callback lambdas", [Test Compose navigation](https://developer.android.com/guide/navigation/testing/compose): "Don't pass the NavController directly into any composable"

### 4-5. 내비게이션 상태는 `:core:navigation`, 조립은 `:app` `필수`

- `:core:navigation`에 `NavigationState`(top-level 스택과 스택별 back stack, `rememberNavBackStack`으로 복원)와 `Navigator`(`navigate`, `goBack`)를 둔다. 구현은 NiA `core/navigation`을 따른다.
- 각 back stack의 entry decorator는 `rememberSaveableStateHolderNavEntryDecorator()`를 **첫 번째로**, 그다음 `rememberViewModelStoreNavEntryDecorator()`를 둔다.
- `:app`의 `CherryPokemonApp()`이 entry builder들을 모아 `NavDisplay`를 만든다.
- 시작 화면은 고정이고 back stack의 바닥에 항상 있다.

```kotlin
@Composable
fun CherryPokemonApp(
    navigationState: NavigationState = rememberNavigationState(
        startKey = PokemonListNavKey,
        topLevelKeys = setOf(PokemonListNavKey),
    ),
) {
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val snackbarHostState = remember { SnackbarHostState() }

    val entryProvider = entryProvider {
        pokemonListEntryBuilder(navigator, snackbarHostState)
        pokemonDetailEntryBuilder(navigator, snackbarHostState)
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = navigator::goBack,
            modifier = Modifier.padding(padding),
        )
    }
}
```

근거: [Navigation 3: Save state](https://developer.android.com/guide/navigation/navigation-3/save-state), [Entry decorators](https://developer.android.com/guide/navigation/navigation-3/naventrydecorators): "you should include SaveableStateHolderNavEntryDecorator as the first decorator", [Principles of navigation](https://developer.android.com/guide/navigation/principles): 고정 시작 목적지, NiA `NavigationState` / `Navigator` / `NiaApp`

## 5. 모듈 안의 패키지 `필수`

패키지는 `com.cherryzp.cherrypokemon.` + 모듈 경로이고, 디렉터리는 패키지 구조를 따른다. (`:feature:pokemondetail:impl` → `com.cherryzp.cherrypokemon.feature.pokemondetail.impl`)

```
feature/pokemondetail/impl/src/main/kotlin/com/cherryzp/cherrypokemon/feature/pokemondetail/impl/
├── PokemonDetailScreen.kt
├── PokemonDetailViewModel.kt
├── PokemonDetailUiState.kt
├── PokemonDetailSideEffect.kt
├── component/                       이 화면에서만 쓰는 Composable
└── navigation/
    └── PokemonDetailEntryBuilder.kt

core/data/src/main/kotlin/com/cherryzp/cherrypokemon/core/data/
├── di/                              Hilt 모듈
├── model/                           네트워크 모델 → 엔티티 변환 (asEntity)
├── paging/                          RemoteMediator
└── repository/                      인터페이스와 구현
```

- 소스는 `src/main/kotlin`에 둔다.
- 한 화면에서만 쓰는 컴포넌트는 그 feature의 `component` 패키지, 두 feature 이상에서 쓰면 `:core:ui`로 옮긴다. 도메인 모델을 모르는 컴포넌트는 `:core:designsystem`이다.

근거: [Kotlin coding conventions: Directory structure](https://kotlinlang.org/docs/coding-conventions.html#directory-structure), NiA 모듈별 패키지 구성
