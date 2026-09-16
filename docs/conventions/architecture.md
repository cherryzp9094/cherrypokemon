# 아키텍처

> 코드 예시는 규칙을 보여주기 위한 것이다. 라이브러리 버전에 따라 API 이름이 다를 수 있으니 작성할 때 해당 버전 문서를 확인한다.

## 1. 모듈

### 1-1. 구성 `필수`

```
:app
:feature:pokemonlist:api        :feature:pokemondetail:api
:feature:pokemonlist:impl       :feature:pokemondetail:impl
:core:navigation
:core:designsystem
:core:ui
:core:domain
:core:data
:core:database
:core:network
:core:model
:core:common
:core:testing
:core:data-test
```

| 모듈 | 종류 | 담는 것 |
|---|---|---|
| `:app` | Android application | `Application`, `MainActivity`, 앱 전체 내비게이션(`NavDisplay`), 앱 수준 상태 |
| `:feature:<화면>:api` | Android library | 그 화면의 `NavKey`, `Navigator.navigateTo<화면>()` |
| `:feature:<화면>:impl` | Android library | Screen, ViewModel, UiState, 화면 전용 컴포넌트, entry provider |
| `:core:navigation` | Android library | `NavigationState`, `Navigator` |
| `:core:designsystem` | Android library | 테마(색·타이포·모양), 도메인을 모르는 기본 컴포넌트, 아이콘 |
| `:core:ui` | Android library | 도메인 모델을 받는 공용 컴포넌트(`PokemonCard`), Preview 도구 |
| `:core:domain` | Android library | UseCase |
| `:core:data` | Android library | Repository 인터페이스와 구현, RemoteMediator, 네트워크 모델 → 엔티티 변환 |
| `:core:database` | Android library | Room 데이터베이스, 엔티티, DAO |
| `:core:network` | Android library | 네트워크 데이터 소스 인터페이스와 Retrofit 구현, 네트워크 모델 |
| `:core:model` | **JVM library** | 앱 전체가 쓰는 도메인 모델 |
| `:core:common` | **JVM library** | 디스패처 qualifier, 애플리케이션 `CoroutineScope`, `Result` |
| `:core:testing` | Android library | 테스트 규칙, 테스트 데이터, 공용 Fake |
| `:core:data-test` | Android library | 계측 테스트에서 Repository 바인딩을 Fake로 바꾸는 Hilt 모듈 |

- 새 화면(사용자 흐름 하나)을 만들면 `:feature:<화면>:api`와 `:impl`을 함께 만든다.
- Android 프레임워크가 필요 없는 모듈은 JVM library로 만든다.

근거: [Modularization patterns: Types of modules](https://developer.android.com/topic/modularization/patterns), NiA `settings.gradle.kts`

### 1-2. 의존 방향 `필수`

```
:app ──▶ :feature:*:impl ──▶ :feature:*:api ──▶ :core:navigation
  │            │
  │            ├──▶ :core:domain ──▶ :core:data ──┬──▶ :core:network  ──┐
  │            │                                   ├──▶ :core:database ─┼──▶ :core:model
  │            ├──▶ :core:data                     └──▶ :core:common    │
  │            └──▶ :core:ui ──▶ :core:designsystem                     │
  │                     └───────────────────────────────────────────────┘
  └──▶ :core:designsystem, :core:navigation
```

| 규칙 | 이유 |
|---|---|
| `impl`은 다른 `impl`에 의존하지 않는다. 다른 화면으로 가려면 그 화면의 `api`에 의존한다 | feature끼리 결합하면 한 화면을 고칠 때 다른 화면까지 다시 빌드된다 |
| `api`는 `:core:navigation` 말고는 의존하지 않는다 | `api`는 다른 feature가 가져다 쓰므로 가벼워야 한다 |
| 레이어 방향은 UI → domain → data 한쪽뿐이다. data는 UI나 domain을 모른다 | [Guide to app architecture](https://developer.android.com/topic/architecture) |
| `:core:model`과 `:core:common`은 어떤 프로젝트 모듈에도 의존하지 않는다 | 가장 아래 모듈 |
| `:core:network`와 `:core:database`는 서로 의존하지 않는다 | 두 데이터 소스를 조합하는 곳은 `:core:data`다 |

근거: [Modularization patterns: Feature modules](https://developer.android.com/topic/modularization/patterns), [Domain layer: Dependencies](https://developer.android.com/topic/architecture/domain-layer)

### 1-3. 공개 범위 `필수`

- 다른 모듈이 쓸 필요가 없는 선언은 `internal`이다. Repository 구현, 데이터 소스 구현, DAO 구현 세부, ViewModel, 화면 내부 Composable이 여기에 해당한다.
- `:feature:*:impl`에서 `public`인 것은 entry provider 함수뿐이다.
- 의존은 `implementation`으로 선언한다. `api`는 공개 시그니처에 그 모듈의 타입이 드러날 때만 쓴다. (예: `:core:domain`의 `api(projects.core.model)`)

근거: [Modularization patterns: Expose minimal public interface](https://developer.android.com/topic/modularization/patterns)

## 2. 레이어

| 레이어 | 모듈 | 책임 | 노출 방식 |
|---|---|---|---|
| UI | `:feature:*:impl`, `:core:ui`, `:core:designsystem` | 상태를 그리고, 사용자 입력을 ViewModel에 전달 | ViewModel이 UiState를 노출 |
| Domain | `:core:domain` | 여러 Repository를 조합하거나 재사용되는 비즈니스 로직 | `operator fun invoke()` |
| Data | `:core:data`, `:core:network`, `:core:database` | 데이터를 저장·동기화하고 도메인 모델로 노출 | 계속 바뀌는 값은 `Flow`, 한 번 실행은 `suspend fun` |

- 레이어 사이 통신은 코루틴과 Flow로 한다. `필수`
- UI 레이어는 Retrofit, Room 같은 데이터 소스를 직접 쓰지 않고 항상 Repository나 UseCase를 거친다. `필수`
- ViewModel은 UseCase가 있으면 UseCase를, 없으면 Repository를 쓴다. UseCase를 만드는 기준은 [data.md 6](data.md#6-usecase)을 따른다.

근거: [Recommendations: Layered architecture](https://developer.android.com/topic/architecture/recommendations#layered-architecture)

## 3. 상태 관리

### 3-1. 단방향 데이터 흐름 `필수`

```
사용자 입력 ─▶ Screen 콜백 ─▶ ViewModel intent ─▶ Repository / UseCase
                                     │
Screen ◀── UiState ◀────────────── reduce
Screen ◀── SideEffect ◀────────── postSideEffect
```

- 상태는 ViewModel에서 UI로 내려가고, 입력은 UI에서 ViewModel로 올라간다.
- UI는 상태를 직접 바꾸지 않는다. 상태를 바꾸는 곳은 ViewModel의 `reduce`뿐이다.

근거: [UI layer: Unidirectional data flow](https://developer.android.com/topic/architecture/ui-layer#udf)

### 3-2. ViewModel은 Orbit으로 만든다 `필수`

[라이브러리 예외](README.md#라이브러리-예외)에 따라 ViewModel의 상태와 SideEffect는 Orbit이 관리한다. Orbit이 맡는 부분은 [Orbit 문서](https://orbit-mvi.org/)를 따르고, 그 밖의 ViewModel 규칙은 공식 가이드를 따른다.

```kotlin
@HiltViewModel(assistedFactory = PokemonDetailViewModel.Factory::class)
internal class PokemonDetailViewModel @AssistedInject constructor(
    @Assisted private val pokeId: Int,
    private val pokemonRepository: PokemonRepository,
) : ViewModel(),
    OrbitContainerHost<PokemonDetailUiState, PokemonDetailUiState, PokemonDetailSideEffect> {

    override val container = orbitContainer<PokemonDetailUiState, PokemonDetailSideEffect>(
        initialState = PokemonDetailUiState(),
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
        reduce { state.copy(isRefreshing = true) }
        try {
            pokemonRepository.refreshPokemonDetail(pokeId)
        } catch (e: IOException) {
            postSideEffect(PokemonDetailSideEffect.ShowMessage(R.string.feature_pokemondetail_impl_error_network))
        } finally {
            reduce { state.copy(isRefreshing = false) }
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
| ViewModel은 화면 단위로만 만든다. 재사용 컴포넌트의 UI 로직은 일반 state holder 클래스에 둔다 | `필수` | [Recommendations: ViewModel](https://developer.android.com/topic/architecture/recommendations#viewmodel) |
| `Context`, `Activity`, `Resources`, `Window`를 참조하지 않는다. 문자열은 `@StringRes Int`로 넘긴다 | `필수` | 같음 |
| `AndroidViewModel`을 쓰지 않는다 | `권장` | 같음 |
| 로딩 시작과 스트림 구독은 `init {}`이 아니라 `orbitContainer`의 `onCreate` 블록에서 한다 | `필수` | Orbit: 구독이 시작될 때 실행되고 테스트에서 시점을 제어할 수 있다 |
| `reduce {}` 안에서는 새 상태만 계산한다. suspend 호출은 그 전에 끝낸다 | `필수` | Orbit |
| 끝나지 않는 Flow는 `repeatOnSubscription {}` 안에서 수집한다. 화면이 보이지 않을 때 구독을 멈춘다 | `필수` | [Orbit: Operators](https://orbit-mvi.org/Core/) |
| 화면 인자는 NavKey 값을 assisted injection으로 받는다. `SavedStateHandle`에서 문자열 키로 꺼내지 않는다 | `필수` | [Hilt with Jetpack](https://developer.android.com/training/dependency-injection/hilt-jetpack), NiA `TopicViewModel` |

### 3-3. UiState `필수`

화면마다 불변 타입 하나로 정의하고 이름은 `<화면>UiState`로 한다.

```kotlin
// 오프라인 우선 화면: 캐시된 데이터, 새로고침 여부가 함께 존재한다 → data class
internal data class PokemonDetailUiState(
    val pokemonDetail: PokemonDetail? = null,
    val isRefreshing: Boolean = false,
)

// 서로 배타적인 상태 → sealed interface
internal sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(val results: ImmutableList<Pokemon>) : SearchUiState
    data object EmptyQuery : SearchUiState
}
```

- 모든 프로퍼티는 `val`이다. 컬렉션은 `ImmutableList` / `ImmutableMap`이다.
- 로딩과 에러를 표현한다. 정상 흐름만 있는 상태를 만들지 않는다.
- 여러 값이 함께 바뀌면 `data class`, 서로 배타적이면 `sealed interface`로 만든다.
- 공통 부모 클래스를 두지 않는다.
- `Flow`, `PagingData`, `MutableState`를 상태에 넣지 않는다.

근거: [UI layer: Define UI state](https://developer.android.com/topic/architecture/ui-layer#define-ui-state)

### 3-4. SideEffect `필수`

Orbit의 SideEffect로 한 번만 처리할 이벤트(메시지 표시, 비즈니스 결과에 따른 이동)를 UI에 보낸다.

- 타입은 화면마다 `internal sealed interface <화면>SideEffect`로 만든다. 없으면 `Nothing`으로 둔다.
- 비즈니스 로직 없이 클릭만으로 끝나는 이동은 SideEffect를 거치지 않고 Screen 콜백에서 바로 `Navigator`를 부른다. (4-4)
- UI는 `collectSideEffect`로 한 곳에서만 수집한다.

```kotlin
internal sealed interface PokemonDetailSideEffect {
    data class ShowMessage(@StringRes val messageId: Int) : PokemonDetailSideEffect
}
```

근거: [Orbit: Side effects](https://orbit-mvi.org/Core/), [UI events: UI behavior logic](https://developer.android.com/topic/architecture/ui-layer/events)

### 3-5. PagingData는 UiState 밖에 둔다 `필수`

`Flow<PagingData<T>>`는 ViewModel의 별도 프로퍼티로 노출하고 `cachedIn(viewModelScope)`를 붙인다. 목록의 로딩·에러는 `LazyPagingItems.loadState`로 그린다.

```kotlin
val pokemons: Flow<PagingData<Pokemon>> =
    pokemonRepository.getPokemonsStream().cachedIn(viewModelScope)
```

근거: [UI layer](https://developer.android.com/topic/architecture/ui-layer) — Paging의 `PagingData`는 불변 UI 상태와 따로 다룬다

### 3-6. 상태 수집 `필수`

Orbit 컨테이너는 `collectAsState()` / `collectSideEffect()`로, 그 외 `StateFlow`는 `collectAsStateWithLifecycle()`로 수집한다. 둘 다 화면이 `STARTED` 이상일 때만 구독한다. `LaunchedEffect { flow.collect { } }`로 직접 수집하지 않는다.

근거: [Recommendations: Use lifecycle-aware UI state collection](https://developer.android.com/topic/architecture/recommendations#ui-layer), [Orbit Compose](https://orbit-mvi.org/Compose/)

## 4. 단일 Activity와 Navigation 3

### 4-1. 요구 사항 `필수`

| 항목 | 값 |
|---|---|
| Navigation 3 | 최신 안정 버전 |
| compileSdk | Navigation 3가 요구하는 값 이상 (1.1.x: 36, 1.2.x: 37) |
| AGP | 그 compileSdk를 지원하는 버전 이상 (API 36: 8.9.1, API 37: 9.1.1) |
| 플러그인 | `org.jetbrains.kotlin.plugin.serialization` |
| 의존성 | `navigation3-runtime`, `navigation3-ui`, `lifecycle-viewmodel-navigation3`, `hilt-lifecycle-viewmodel-compose` |

근거: [Navigation 3 get started](https://developer.android.com/guide/navigation/navigation-3/get-started), [Navigation 3 releases](https://developer.android.com/jetpack/androidx/releases/navigation3), [AGP API level support](https://developer.android.com/build/releases/about-agp)

### 4-2. Activity는 하나 `필수`

- `MainActivity` 하나만 둔다. 화면은 Activity가 아니라 NavKey와 entry로 추가한다.
- `MainActivity`는 `enableEdgeToEdge()`, 테마 적용, `CherryPokemonApp()` 호출만 한다.
- Activity 생명주기 메서드(`onResume` 등)를 오버라이드하지 않는다. Compose에서 `LifecycleStartEffect` / `LifecycleResumeEffect`를 쓴다.

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

근거: [Recommendations: Use a single-activity application](https://developer.android.com/topic/architecture/recommendations#ui-layer), [Recommendations: Lifecycle](https://developer.android.com/topic/architecture/recommendations#lifecycle)

### 4-3. NavKey와 이동 함수는 `:feature:*:api` `필수`

```kotlin
// :feature:pokemondetail:api  navigation/PokemonDetailNavKey.kt
@Serializable
data class PokemonDetailNavKey(val pokeId: Int) : NavKey

fun Navigator.navigateToPokemonDetail(pokeId: Int) {
    navigate(PokemonDetailNavKey(pokeId))
}
```

- 이름은 `<화면>NavKey`. 인자가 없으면 `data object`, 있으면 `data class`.
- **키에는 ID와 원시값만 담는다.** 도메인 모델이나 앞 화면에서 계산한 표시용 값(예: 배경색)을 담지 않는다. 대상 화면이 ID로 직접 불러온다.

근거: [Navigation 3 basics](https://developer.android.com/guide/navigation/navigation-3/basics), NiA `TopicNavKey`

### 4-4. entry provider는 `:feature:*:impl` `필수`

```kotlin
// :feature:pokemondetail:impl  navigation/PokemonDetailEntryProvider.kt
fun EntryProviderScope<NavKey>.pokemonDetailEntry(
    navigator: Navigator,
    onShowSnackbar: suspend (message: String) -> Unit,
) {
    entry<PokemonDetailNavKey> { key ->
        PokemonDetailScreen(
            viewModel = hiltViewModel<PokemonDetailViewModel, PokemonDetailViewModel.Factory>(
                key = key.pokeId.toString(),
            ) { factory ->
                factory.create(key.pokeId)
            },
            onBackClick = navigator::goBack,
            onShowSnackbar = onShowSnackbar,
        )
    }
}

// :feature:pokemonlist:impl  navigation/PokemonListEntryProvider.kt
fun EntryProviderScope<NavKey>.pokemonListEntry(
    navigator: Navigator,
    onShowSnackbar: suspend (message: String) -> Unit,
) {
    entry<PokemonListNavKey> {
        PokemonListScreen(
            onPokemonClick = navigator::navigateToPokemonDetail,
            onShowSnackbar = onShowSnackbar,
        )
    }
}
```

- 함수 이름은 `<화면>Entry`, 파일은 `<화면>EntryProvider.kt`.
- Screen은 `Navigator`나 back stack을 받지 않고 콜백만 받는다. 콜백과 `Navigator`를 잇는 곳은 entry provider다.

근거: NiA `TopicEntryProvider`, [Hilt with Jetpack: assisted injection](https://developer.android.com/training/dependency-injection/hilt-jetpack)

### 4-5. 내비게이션 상태는 `:core:navigation`, 조립은 `:app` `필수`

- `:core:navigation`에 `NavigationState`(top-level 스택과 화면별 back stack, `rememberNavBackStack`으로 복원)와 `Navigator`(`navigate`, `goBack`)를 둔다. 구현은 NiA의 `core/navigation`을 따른다.
- 각 스택의 entry에는 `rememberSaveableStateHolderNavEntryDecorator()`와 `rememberViewModelStoreNavEntryDecorator()`를 적용한다. ViewModel이 화면(NavEntry)마다 만들어지고, 화면이 스택에서 빠지면 정리된다.
- `:app`의 `CherryPokemonApp()`이 각 feature의 entry를 모아 `NavDisplay`를 만든다.

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
    val onShowSnackbar: suspend (String) -> Unit = { message ->
        snackbarHostState.showSnackbar(message)
    }

    val entryProvider = entryProvider {
        pokemonListEntry(navigator, onShowSnackbar)
        pokemonDetailEntry(navigator, onShowSnackbar)
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

근거: [Save state: Scope ViewModels to NavEntry](https://developer.android.com/guide/navigation/navigation-3/save-state), NiA `NavigationState` / `Navigator` / `NiaApp`

## 5. 모듈 안의 패키지 `필수`

패키지는 `com.cherryzp.cherrypokemon.` + 모듈 경로다. (`:feature:pokemondetail:impl` → `com.cherryzp.cherrypokemon.feature.pokemondetail.impl`)

```
feature/pokemondetail/impl/src/main/kotlin/com/cherryzp/cherrypokemon/feature/pokemondetail/impl/
├── PokemonDetailScreen.kt
├── PokemonDetailViewModel.kt
├── PokemonDetailUiState.kt
├── PokemonDetailSideEffect.kt
├── component/                       이 화면에서만 쓰는 Composable
└── navigation/
    └── PokemonDetailEntryProvider.kt

core/data/src/main/kotlin/com/cherryzp/cherrypokemon/core/data/
├── di/                              Hilt 모듈
├── model/                           네트워크 모델 → 엔티티 변환 (asEntity)
├── paging/                          RemoteMediator
└── repository/                      인터페이스와 구현
```

- 소스는 `src/main/kotlin`에 둔다.
- 한 화면에서만 쓰는 컴포넌트는 그 feature의 `component` 패키지, 두 feature 이상에서 쓰면 `:core:ui`로 옮긴다. 도메인 모델을 모르는 컴포넌트는 `:core:designsystem`이다.
- 패키지 이름 규칙은 [kotlin.md 1-2](kotlin.md#1-2-패키지-필수)를 따른다.
