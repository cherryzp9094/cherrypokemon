# 아키텍처

> 코드 예시는 방향을 보여주기 위한 것이며, 이 프로젝트에서 아직 빌드해 보지 않았다.

## 1. 모듈

### 1-1. 구성

| 모듈 | 종류 | 담는 것 |
|---|---|---|
| `:app` | Android application | `MainActivity`, 내비게이션, 화면(Route·Screen·ViewModel·UiState), 테마, 공용 컴포넌트 |
| `:domain` | **순수 Kotlin** (`java-library`) | 도메인 모델, Repository 인터페이스, UseCase |
| `:data` | Android library | Repository 구현, Retrofit API, DTO, 매퍼, PagingSource, Hilt 모듈 |
| `:core:common` | 순수 Kotlin | 어느 레이어에도 속하지 않는 확장 함수, 디스패처 qualifier |

### 1-2. 의존 방향 `필수`

```
:app ──▶ :domain ◀── :data
  │                    ▲
  └── runtimeOnly ─────┘      (Hilt가 구현을 찾도록 런타임에만 연결)

모든 모듈 ──▶ :core:common
```

- `:app`의 코드는 `com.cherryzp.data.*`를 import 하지 않는다. `:app`은 `:data`를 `runtimeOnly`로만 의존해서 컴파일 단계에서 막는다.
- `:domain`은 다른 프로젝트 모듈과 Android 프레임워크에 의존하지 않는다. 예외로 `paging-common`(JVM 아티팩트)은 허용한다.

```kotlin
// ❌ PokemonDetailViewModel.kt: UI가 data 모듈 코드를 직접 참조
import com.cherryzp.data.extend.default
```

근거: [Guide to app architecture](https://developer.android.com/topic/architecture), [Modularization patterns: Prefer Kotlin/Java modules](https://developer.android.com/topic/modularization/patterns)

### 1-3. 공개 범위 `권장`

- `:data`에서 밖으로 보이는 것은 Hilt 모듈뿐이다. Repository 구현, API, DTO, 매퍼, PagingSource는 `internal`로 둔다.
- 모듈 의존은 `implementation`으로 선언한다. `api`는 공개 타입에 그 모듈의 타입이 드러날 때만 쓴다. (예: `:domain`의 `api(libs.paging.common)`)

근거: [Modularization patterns: Expose minimal public interface](https://developer.android.com/topic/modularization/patterns)

### 1-4. feature 모듈 `선택`

화면이 두 개인 동안은 `:app` 안의 `feature/` 패키지로 나눈다. 다음 중 하나에 해당하면 `:feature:<이름>` 모듈로 분리한다.

- 화면이 5개를 넘는다
- 화면 하나를 빌드하는 데 `:app` 전체 컴파일을 기다리는 게 부담이 된다

분리할 때 feature 모듈끼리는 서로 의존하지 않는다. 화면 간 이동은 `:app`이 연결한다.

## 2. 레이어

| 레이어 | 위치 | 책임 | 노출 방식 |
|---|---|---|---|
| UI | `:app` | 상태를 그리고, 사용자 입력을 ViewModel에 전달 | ViewModel이 `UiState` 하나를 노출 |
| Domain | `:domain` | 여러 Repository를 조합하거나 재사용되는 비즈니스 로직 | `suspend operator fun invoke()` |
| Data | `:data` | 데이터 조회·저장, 원본 데이터를 도메인 모델로 변환 | 한 번 조회는 `suspend fun`, 계속 바뀌는 값은 `Flow` |

- 레이어 사이 통신은 코루틴과 Flow로 한다. 콜백이나 RxJava를 쓰지 않는다. `필수`
- UI 레이어는 Retrofit, 데이터베이스 같은 데이터 소스를 직접 쓰지 않고 항상 Repository를 거친다. `필수`
- UseCase를 언제 만드는지는 [data.md](data.md#2-usecase)를 따른다.

근거: [Recommendations: Layered architecture](https://developer.android.com/topic/architecture/recommendations#layered-architecture)

## 3. 상태 관리

### 3-1. 단방향 데이터 흐름 `필수`

```
사용자 입력 ─▶ Screen 콜백 ─▶ ViewModel 함수 ─▶ Repository
                                   │
Screen ◀─ UiState ◀────────────── reduce
```

- 상태는 ViewModel에서 UI로 내려가고, 입력은 UI에서 ViewModel로 올라간다.
- UI는 상태를 직접 바꾸지 않는다. 상태를 바꾸는 곳은 ViewModel의 `reduce`뿐이다.

근거: [UI layer: Unidirectional data flow](https://developer.android.com/topic/architecture/ui-layer#udf)

### 3-2. ViewModel `필수`

Orbit의 `OrbitContainerHost`를 구현한다. **SideEffect 타입은 `Nothing`으로 둔다.** (3-4 참고)

```kotlin
@HiltViewModel
class PokemonListViewModel @Inject constructor(
    pokemonRepository: PokemonRepository,
) : ViewModel(), OrbitContainerHost<PokemonListUiState, PokemonListUiState, Nothing> {

    override val container = orbitContainer<PokemonListUiState, Nothing>(PokemonListUiState())

    // PagingData는 UiState에 넣지 않는다 (3-5)
    val pokemons: Flow<PagingData<Pokemon>> =
        pokemonRepository.getPokemonsStream().cachedIn(viewModelScope)
}
```

- ViewModel은 화면 단위로만 만든다. 재사용 컴포넌트의 UI 로직은 일반 state holder 클래스(`rememberXxxState()`)에 둔다.
- `Context`, `Activity`, `Resources`, `Window`를 참조하지 않는다. 문자열은 리소스 ID(`@StringRes Int`)로 상태에 담고 UI에서 꺼낸다.
- `AndroidViewModel`을 쓰지 않는다. `권장`
- `init {}`에서 로딩을 시작하지 않고 `orbitContainer(onCreate = { })`를 쓴다. 테스트에서 시작 시점을 제어할 수 있다.
- `reduce {}` 안에서는 값만 계산한다. suspend 호출은 그 전에 끝낸다.

근거: [Recommendations: ViewModel](https://developer.android.com/topic/architecture/recommendations#viewmodel)

### 3-3. UiState `필수`

화면마다 하나의 불변 타입으로 정의하고 이름은 `<화면>UiState`로 한다.

```kotlin
// ✅ 동시에 존재할 수 없는 상태는 sealed interface
sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState
    data class Success(val pokemonDetail: PokemonDetail) : PokemonDetailUiState
    data object Error : PokemonDetailUiState
}

// ✅ 함께 존재하는 값은 data class
data class PokemonListUiState(
    @StringRes val userMessage: Int? = null,
)
```

```kotlin
// ❌ MainContract.kt: 가변 스트림이 상태 안에 있고, @Stable로 안정성을 우긴다
@Stable
data class MainUiState(
    val pokemons: Flow<PagingData<Pokemon>>? = null,
    ...
): UiState()
```

- 모든 프로퍼티는 `val`이다. 컬렉션은 `kotlinx.collections.immutable`의 `ImmutableList` / `ImmutableMap`을 쓴다. `권장`
- 로딩과 에러를 상태로 표현한다. 정상 흐름만 있는 상태를 만들지 않는다.
- 상태가 서로 배타적이면(`Loading` / `Success` / `Error`) `sealed interface`, 여러 값이 함께 바뀌면 `data class`. `선택`
- 공통 부모 클래스(`UiState()`)를 두지 않는다.

근거: [UI layer: Define UI state](https://developer.android.com/topic/architecture/ui-layer#define-ui-state)

### 3-4. ViewModel에서 UI로 이벤트를 보내지 않는다 `필수`

Orbit의 `postSideEffect`와 `collectSideEffect`를 쓰지 않는다. `Channel`이나 `SharedFlow`로 이벤트를 흘리는 것도 같다. 받는 쪽(Compose)보다 ViewModel이 오래 살아서, 이벤트가 전달되거나 처리된다는 보장이 없기 때문이다.

| 이벤트 | 처리 |
|---|---|
| 카드를 눌러 상세로 이동 | ViewModel을 거치지 않는다. Screen 콜백 → 내비게이션(4-4) |
| API 결과에 따라 이동 (예: 저장 후 닫기) | 상태에 `isSaved = true`를 두고, Route가 그 값을 보고 콜백을 호출 |
| 스낵바 메시지 | 상태에 `userMessage`를 두고, 보여준 뒤 `userMessageShown()`으로 지움 |

```kotlin
// ❌ MainViewModel.kt: 클릭을 이벤트로 바꿔 다시 UI로 돌려보냄
fun goPokemonDetail(pokeId: Int, pokemonBackgroundColor: Int) = postSideEffect {
    MainUiSideEffect.goPokemonDetail(pokeId = pokeId, pokemonBackgroundColor = pokemonBackgroundColor)
}

// ✅ Route가 클릭을 바로 내비게이션 콜백으로 연결
PokemonListScreen(onPokemonClick = onPokemonClick)
```

```kotlin
// ✅ 메시지는 상태로
fun refresh() = intent {
    try {
        ...
    } catch (e: IOException) {
        reduce { state.copy(userMessage = R.string.error_network) }
    }
}

fun userMessageShown() = intent { reduce { state.copy(userMessage = null) } }
```

근거: [UI events: Handle ViewModel events](https://developer.android.com/topic/architecture/ui-layer/events#handle-viewmodel-events)

### 3-5. PagingData는 UiState 밖에 둔다 `권장`

`Flow<PagingData<T>>`는 ViewModel의 별도 프로퍼티로 노출하고 `cachedIn(viewModelScope)`를 붙인다. 목록의 로딩·에러는 `LazyPagingItems.loadState`로 그린다.

근거: [UI layer: Consume UI state](https://developer.android.com/topic/architecture/ui-layer#consume-ui-state)

### 3-6. 상태 수집 `필수`

Compose에서 Orbit의 `collectAsState()`로 수집한다. 화면이 `STARTED` 이상일 때만 구독한다. 일반 `StateFlow`는 `collectAsStateWithLifecycle()`로 수집한다. `collectAsState()`(Compose 기본)나 `LaunchedEffect { flow.collect { } }`로 직접 수집하지 않는다.

근거: [Recommendations: Use lifecycle-aware UI state collection](https://developer.android.com/topic/architecture/recommendations#ui-layer)

## 4. 단일 Activity와 Navigation 3

### 4-1. 요구 사항

| 항목 | Navigation 3 1.1.x | Navigation 3 1.2.x |
|---|---|---|
| compileSdk | 36 이상 | 37 이상 |
| AGP | 8.9.1 이상 | 9.1.1 이상 |
| minSdk | 23 이상 | 23 이상 |
| 플러그인 | `org.jetbrains.kotlin.plugin.serialization` | 같음 |

지금 프로젝트는 compileSdk 34, AGP 8.5.2라서 툴체인을 먼저 올려야 한다.

```toml
[libraries]
androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "nav3Core" }
androidx-navigation3-ui = { module = "androidx.navigation3:navigation3-ui", version.ref = "nav3Core" }
androidx-lifecycle-viewmodel-navigation3 = { module = "androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "lifecycleViewmodelNav3" }
androidx-hilt-lifecycle-viewmodel-compose = { module = "androidx.hilt:hilt-lifecycle-viewmodel-compose", version.ref = "hiltLifecycleViewmodelCompose" }
kotlinx-serialization-core = { module = "org.jetbrains.kotlinx:kotlinx-serialization-core", version.ref = "kotlinxSerialization" }
```

근거: [Navigation 3 get started](https://developer.android.com/guide/navigation/navigation-3/get-started), [Navigation 3 releases](https://developer.android.com/jetpack/androidx/releases/navigation3), [AGP API level support](https://developer.android.com/build/releases/about-agp)

### 4-2. Activity는 하나 `필수`

- `MainActivity` 하나만 둔다. 화면을 추가할 때 Activity를 만들지 않는다.
- `MainActivity`는 `enableEdgeToEdge()`, 테마, `CherryPokemonApp()` 호출만 한다.
- Activity 생명주기 메서드(`onResume` 등)를 오버라이드하지 않는다. Compose에서 `LifecycleStartEffect`, `LifecycleResumeEffect`를 쓴다.

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

### 4-3. NavKey `필수`

- 화면마다 `@Serializable` 키를 하나 만든다. 이름은 `<화면>NavKey`, 파일은 그 화면 패키지에 둔다.
- 인자가 없으면 `data object`, 있으면 `data class`.
- **키에는 ID와 원시값만 담는다.** 도메인 모델이나 화면에 그릴 데이터를 담지 않는다. 대상 화면이 ID로 직접 불러온다.

```kotlin
@Serializable
data object PokemonListNavKey : NavKey

@Serializable
data class PokemonDetailNavKey(val pokeId: Int) : NavKey
```

```kotlin
// ❌ PokemonDetailActivity.kt: 앞 화면에서 계산한 표시용 값을 넘기고, 키 문자열 상수로 꺼낸다
fun create(pokeId: Int, pokemonBackgroundColor: Int) = bundleOf(
    POKE_NO to pokeId,
    POKEMON_BACKGROUND_COLOR to pokemonBackgroundColor
)
```

근거: [Navigation 3 basics](https://developer.android.com/guide/navigation/navigation-3/basics), [Save state](https://developer.android.com/guide/navigation/navigation-3/save-state)

### 4-4. back stack과 NavDisplay `필수`

- back stack은 `CherryPokemonApp()`에서 `rememberNavBackStack()`으로 한 번만 만든다. 설정 변경과 프로세스 종료 후에도 복원된다.
- back stack을 Route, Screen, ViewModel에 넘기지 않는다. 화면은 `onPokemonClick`, `onBackClick` 같은 콜백만 받고, 실제 `add` / `removeLastOrNull`은 `CherryPokemonApp()`이 한다.
- `entryDecorators`에 `rememberSaveableStateHolderNavEntryDecorator()`와 `rememberViewModelStoreNavEntryDecorator()`를 넣는다. ViewModel이 화면(NavEntry)마다 만들어지고, 화면이 back stack에서 빠지면 정리된다.

```kotlin
@Composable
fun CherryPokemonApp() {
    val backStack = rememberNavBackStack(PokemonListNavKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<PokemonListNavKey> {
                PokemonListRoute(
                    onPokemonClick = { pokeId -> backStack.add(PokemonDetailNavKey(pokeId)) },
                )
            }
            entry<PokemonDetailNavKey> { key ->
                PokemonDetailRoute(
                    viewModel = hiltViewModel<PokemonDetailViewModel, PokemonDetailViewModel.Factory>(
                        creationCallback = { factory -> factory.create(key) },
                    ),
                    onBackClick = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
```

근거: [Navigation 3 basics](https://developer.android.com/guide/navigation/navigation-3/basics), [Save state: Scope ViewModels to NavEntry](https://developer.android.com/guide/navigation/navigation-3/save-state)

### 4-5. ViewModel에 화면 인자 전달 `필수`

NavKey를 assisted injection으로 받는다. `SavedStateHandle`에서 문자열 키로 꺼내지 않는다. `KeyConsts` 같은 인자 이름 상수도 만들지 않는다.

```kotlin
@HiltViewModel(assistedFactory = PokemonDetailViewModel.Factory::class)
class PokemonDetailViewModel @AssistedInject constructor(
    @Assisted private val navKey: PokemonDetailNavKey,
    private val pokemonRepository: PokemonRepository,
) : ViewModel(), OrbitContainerHost<PokemonDetailUiState, PokemonDetailUiState, Nothing> {

    override val container = orbitContainer<PokemonDetailUiState, Nothing>(
        initialState = PokemonDetailUiState.Loading,
        onCreate = { loadDetail() },
    )

    fun retry() = intent {
        reduce { PokemonDetailUiState.Loading }
        loadDetail()
    }

    private suspend fun loadDetail() = subIntent {
        try {
            val detail = pokemonRepository.getPokemonDetail(navKey.pokeId)
            reduce { PokemonDetailUiState.Success(detail) }
        } catch (e: IOException) {
            reduce { PokemonDetailUiState.Error }
        } catch (e: HttpException) {
            reduce { PokemonDetailUiState.Error }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: PokemonDetailNavKey): PokemonDetailViewModel
    }
}
```

```kotlin
// ❌ PokemonDetailViewModel.kt: 키가 없으면 0번 포켓몬을 조용히 요청한다
savedStateHandle.get<Int>(POKE_NO).default()
```

근거: [Hilt with Jetpack: assisted injection](https://developer.android.com/training/dependency-injection/hilt-jetpack), [nav3-recipes: passing arguments (Hilt)](https://github.com/android/nav3-recipes/tree/main/app/src/main/java/com/example/nav3recipes/passingarguments/viewmodels/hilt)

## 5. `:app` 패키지 구조 `권장`

```
com.cherryzp.cherrypokemon
├── CherryPokemonApplication.kt
├── MainActivity.kt
├── CherryPokemonApp.kt              NavDisplay, back stack
├── feature
│   ├── pokemonlist
│   │   ├── PokemonListNavKey.kt
│   │   ├── PokemonListRoute.kt      ViewModel 연결
│   │   ├── PokemonListScreen.kt     상태만 받는 UI + Preview
│   │   ├── PokemonListUiState.kt
│   │   └── PokemonListViewModel.kt
│   └── pokemondetail
│       └── ...
└── ui
    ├── component                    두 화면 이상에서 쓰는 컴포넌트
    └── theme
```

- 화면 이름은 역할로 짓는다. 지금 `main`은 포켓몬 목록이므로 `pokemonlist`다.
- 화면 하나에서만 쓰는 컴포넌트는 그 화면 패키지에 둔다. 두 화면 이상에서 쓰게 되면 `ui/component`로 옮긴다.
- 패키지 이름 규칙은 [kotlin.md](kotlin.md#1-2-패키지)를 따른다.
