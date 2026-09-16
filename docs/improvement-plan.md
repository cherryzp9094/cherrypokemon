# CherryPokemon 개선 계획

- 작성일: 2026-09-16
- 기준 커밋: `c4a3cf5`
- 출발점: `claude/android-architecture-analysis-rfck0g` 브랜치의 `docs/android-architecture-analysis.md` (원격 컨테이너에서 한 정적 분석)
- 이 문서는 그 리포트의 주장을 **실제 코드로 다시 검증**하고, Orbit MVI 전환을 더해 실행 순서대로 정리한 것이다.

> 코드 예시는 방향을 보여주기 위한 것이며, 이 프로젝트에서 아직 빌드해 보지 않았다. 각 PR에서 로컬 빌드와 실기기로 확인한다.

---

## 검증 결과

### 리포트 주장

| 주장 | 판정 |
|---|---|
| Paging 네트워크 에러 시 크래시 | ✅ `PokemonPagingSource.load()`의 API 호출이 `try` **밖**에 있음 |
| 상세 화면 배경색 폴백 오류 | ✅ `Color.White.value.toInt()`는 ULong 패킹 값을 잘라낸 값 |
| Palette 폴백이 투명색 | ✅ `getDominantColor(0xFFFFFF)`는 alpha가 0 |
| 상세 API 실패 시 크래시 | ✅ `fetchData()`에 예외 처리 없음 |
| `Pokemon.id`의 `NumberFormatException` | ✅ `toInt()` 사용 |
| Gson 논-널 필드 NPE | ⚠️ 잠재적. R8을 켜면 바로 문제가 됨 |
| `observe()`가 State를 수집하지 않음 | ❌ 과장. State는 Compose가 `collectAsStateWithLifecycle`로 수집함. 이름이 헷갈릴 뿐 |
| SideEffect 유실 | ✅ `MutableSharedFlow()`는 버퍼가 0이라 구독자가 없으면 `emit`한 값이 사라짐 |
| 그 밖의 구조 지적 | ✅ 대부분 정확 |

### 리포트에 없던 항목

1. `targetSdk 34`: Google Play는 2025-08-31부터 신규 앱과 업데이트에 API 35 타깃을 요구한다.
2. `fetchDominantColor`가 Glide `.submit().get()`으로 IO 스레드를 막고, Landscapist가 이미 받은 이미지를 **한 번 더 다운로드**한다.
3. `Pokemon.id`가 접근할 때마다 `url.split()`을 실행해서 리컴포지션마다 문자열을 파싱한다.
4. `getRefreshKey()`가 `null`을 반환해서 새로고침하면 스크롤 위치가 사라진다.
5. `PokemonSpeciesResponse` / `fetchPokemonSpecies`는 쓰는 곳이 없다.
6. Room, Parcelize, Material2는 전 모듈에서 사용처가 0건인데 의존성만 선언되어 있다.

---

## 실행 순서

```
Week 1  ├─ Phase 0  크래시·오동작 수정
        └─ Phase 1  툴체인 올리기 (코드 변경 없음, 단독 PR)
Week 2  ├─ Phase 2  Orbit MVI 전환 (PokemonDetail → Main → base 패키지 삭제)
        └─           CLAUDE.md MVI 섹션 갱신
Week 3  ├─ Phase 3  Compose UI·성능, edge-to-edge + targetSdk 35
        └─ Phase 6-3 CI
Week 4  ├─ Phase 4  모듈 경계
        └─ Phase 5  빌드 인프라
```

**테스트(Phase 6-1)는 한꺼번에 몰아서 쓰지 않고, 각 Phase의 PR에 해당 회귀 테스트를 같이 넣는다.**

---

## Phase 0 — 크래시·오동작 (반나절)

리팩터링 없이 동작만 고친다. 버그 하나당 커밋 하나.

### 0-1. PagingSource 에러 처리

`data/src/main/java/com/cherryzp/data/paging/PokemonPagingSource.kt`

```kotlin
class PokemonPagingSource(
    private val pokemonApi: PokemonApi,
    private val pageSize: Int,
) : PagingSource<Int, Pokemon>() {

    override fun getRefreshKey(state: PagingState<Int, Pokemon>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(pageSize)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(pageSize)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> = try {
        val offset = params.key ?: 0
        val items = pokemonApi.fetchPokemonList(params.loadSize, offset)
            .results.orEmpty().map { it.toDomain() }

        LoadResult.Page(
            data = items,
            prevKey = if (offset == 0) null else offset - pageSize,
            nextKey = if (items.size < pageSize) null else offset + pageSize,
        )
    } catch (e: IOException) {
        LoadResult.Error(e)
    } catch (e: HttpException) {
        LoadResult.Error(e)
    }
}
```

- `Exception` 전체를 잡으면 `CancellationException`까지 삼켜서 코루틴 취소가 깨진다. `IOException`과 `HttpException`만 잡는다.
- 페이지 크기를 PagingSource와 `PagingConfig`에 따로 적지 않고 한 곳에서 주입한다.
- 지금 `page`라는 변수는 실제로 offset이다.

### 0-2. 상세 화면 API 실패 (임시 수정)

`PokemonDetailViewModel.fetchData()`에 try/catch를 넣고 에러 상태를 둔다. Phase 2에서 다시 작성하므로 최소한으로만 고친다.

### 0-3. 색상·파싱 버그

```kotlin
// PokemonDetailViewModel.kt
- savedStateHandle.get<Int>(POKEMON_BACKGROUND_COLOR) ?: Color.White.value.toInt()
+ savedStateHandle.get<Int>(POKEMON_BACKGROUND_COLOR) ?: Color.White.toArgb()

// ColorExtend.kt
- palette.getDominantColor(0xFFFFFF)
+ palette.getDominantColor(Color.White.toArgb())

// domain/.../Pokemon.kt
- get() = url.split("/").lastOrNull { it.isNotEmpty() }?.toInt() ?: 0
+ get() = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0
```

### 0-4. 정리

- 쓰지 않는 `PokemonSpeciesResponse`, `PokemonApi.fetchPokemonSpecies` 삭제
- 쓰지 않는 import 삭제: `PokemonPagingSource`의 `PokemonRepository`, `PokemonListUseCase`의 `Pager` / `PagingConfig`

**완료 기준:** 비행기 모드로 앱을 실행해도 크래시가 나지 않는다.

---

## Phase 1 — 툴체인 올리기 (단독 PR)

Orbit 10 이상이 요구하는 버전이다 (Maven Central 메타데이터로 확인).

| | Orbit 9.0.0 | Orbit 10 ~ 12.0.1 | 현재 |
|---|---|---|---|
| kotlin-stdlib | 1.9.24 | **2.1.21** | 2.0.0 |
| kotlinx-coroutines | 1.8.1 | **1.10.2** | (간접 의존) |
| lifecycle | 2.7.0 | **2.9.0** | 2.8.7 |

```toml
kotlin = "2.1.21"
ksp = "2.1.21-2.0.2"
lifecycleRuntimeKtx = "2.9.x"
hilt = "최신 2.5x"   # 2.51.1은 Kotlin 2.0 시절 버전. 메타데이터를 못 읽는 에러가 나면 올린다
```

- lifecycle 2.9.0의 AAR 메타데이터는 `minCompileSdk=34`, `minAndroidGradlePluginVersion=8.1.1`이다. **compileSdk 35로 올리지 않아도 된다.**
- 확인 순서: `./gradlew :app:kspDebugKotlin` → `assembleDebug` → 실기기 실행
- **이 PR에는 다른 변경을 섞지 않는다.** Kotlin, KSP, Hilt를 동시에 올리는 단계라 빌드가 깨질 가능성이 이 계획에서 가장 크다.

---

## Phase 2 — Orbit MVI 전환

`ui/view/base/base/`의 커스텀 컨테이너는 이름까지 Orbit을 따라 만든 구조라 1:1로 옮길 수 있다. 기준 버전은 **Orbit 12.0.1** (2026-08-28).

### 2-1. 왜 바꾸는가

| 현재 결함 | Orbit 12 기본 동작 |
|---|---|
| SideEffect가 구독자가 없으면 사라짐 | `sideEffectBufferSize = Channel.BUFFERED`, `SideEffectMode.FAN_OUT`: 수집자가 붙을 때까지 보관 |
| SideEffect 타입이 `UiSideEffect` 인터페이스라 `when`이 모든 경우를 검사하지 못함 | `OrbitContainerHost<S, S, SIDE_EFFECT>`로 타입 지정 → `sealed interface`에 대한 `when`이 모든 경우를 검사 |
| `init { fetchData() }`를 테스트에서 막을 수 없음 | `onCreate`는 처음 구독할 때 실행되고, 테스트에서는 `runOnCreate()`로 직접 실행 |
| 테스트 도구 없음 | `orbit-test` (Turbine 기반) |
| `@Immutable abstract class UiState` 남용, 깨진 `sealed` 계층 | base 패키지를 통째로 삭제 |

### 2-2. 매핑

| 현재 | Orbit 12.0.1 |
|---|---|
| `BaseViewModel<S>` | `ViewModel()` + `OrbitContainerHost<S, S, SE>` |
| `RealContainer(initialState)` | `orbitContainer<S, SE>(initialState, onCreate = { ... })` |
| `reduceState { it.copy() }` | `intent { reduce { state.copy() } }` |
| `postSideEffect { SE }` | `intent { postSideEffect(SE) }` |
| `init { fetchData() }` | `onCreate = { load() }` |
| `container.uiState.collectAsStateWithLifecycle()` | `viewModel.collectAsState()` |
| `BaseActivity.observe()` + `handleSideEffect()` | Compose에서 `viewModel.collectSideEffect { }` |

Orbit 12에서 이름이 바뀌었다: `ContainerHost` → `OrbitContainerHost`, `container()` → `orbitContainer()`, `test()` → `testWithInternalState()`. 인터넷 예제는 대부분 옛날 이름이므로 새 이름으로 작성한다.

### 2-3. 의존성

```toml
[versions]
orbit = "12.0.1"

[libraries]
orbit-viewmodel = { module = "org.orbit-mvi:orbit-viewmodel", version.ref = "orbit" }
orbit-compose   = { module = "org.orbit-mvi:orbit-compose",   version.ref = "orbit" }
orbit-test      = { module = "org.orbit-mvi:orbit-test",      version.ref = "orbit" }

[bundles]
orbit = ["orbit-viewmodel", "orbit-compose"]
```

### 2-4. PokemonDetail (첫 번째 PR)

화면이 작고 에러 경로가 있어서 먼저 옮긴다.

```kotlin
data class PokemonDetailState(
    val backgroundColor: Int,
    val pokemonDetail: PokemonDetail? = null,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
)

sealed interface PokemonDetailSideEffect
```

```kotlin
@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pokemonDetailUseCase: PokemonDetailUseCase,
) : ViewModel(), OrbitContainerHost<PokemonDetailState, PokemonDetailState, PokemonDetailSideEffect> {

    // 인자가 없으면 0번 포켓몬을 요청하지 말고 바로 실패시킨다
    private val pokeNo: Int = checkNotNull(savedStateHandle[POKE_NO])

    override val container = orbitContainer<PokemonDetailState, PokemonDetailSideEffect>(
        initialState = PokemonDetailState(
            backgroundColor = savedStateHandle[POKEMON_BACKGROUND_COLOR] ?: Color.White.toArgb(),
        ),
        onCreate = { loadDetail() },
    )

    fun retry() = intent {
        reduce { state.copy(isLoading = true, isError = false) }
        loadDetail()
    }

    private suspend fun loadDetail() = subIntent {
        try {
            val detail = pokemonDetailUseCase(pokeNo)
            reduce { state.copy(pokemonDetail = detail, isLoading = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, isError = true) }
        }
    }
}
```

`pokemonDetailUseCase(pokeNo)`가 값을 바로 돌려주려면 Phase 4-4가 필요하다. 그 전이라면 `.first()`를 붙인다.

### 2-5. Main (두 번째 PR)

```kotlin
sealed interface MainSideEffect {
    data class NavigateToDetail(val pokeId: Int, val backgroundColor: Int) : MainSideEffect
}
```

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    pokemonListUseCase: PokemonListUseCase,
) : ViewModel(), OrbitContainerHost<MainState, MainState, MainSideEffect> {

    override val container = orbitContainer<MainState, MainSideEffect>(MainState())

    // PagingData는 State에 넣지 않는다
    val pokemons: Flow<PagingData<Pokemon>> = pokemonListUseCase().cachedIn(viewModelScope)

    fun onPokemonClick(pokeId: Int, backgroundColor: Int) = intent {
        postSideEffect(MainSideEffect.NavigateToDetail(pokeId, backgroundColor))
    }
}
```

`MainUiState.pokemons: Flow<PagingData>`를 State 밖으로 빼는 이유:
- `@Stable`로 선언했지만 `Flow`는 Compose가 변화를 추적할 수 없는 타입이다.
- 첫 프레임에 `null`이라 빈 화면이 한 번 그려진다.
- `MainViewModel`의 `.catch { }`는 아무것도 잡지 못한다. PagingSource 예외는 스트림 예외가 아니라 `LoadState.Error`로 전달된다.

Main 화면은 상태를 대부분 Paging이 갖고 있어서, 여기서 Orbit 컨테이너는 주로 SideEffect 전달을 맡는다.

### 2-6. Route / Screen 분리, BaseActivity 삭제

```kotlin
@Composable
fun MainRoute(
    onNavigateToDetail: (pokeId: Int, backgroundColor: Int) -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val pokemons = viewModel.pokemons.collectAsLazyPagingItems()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is MainSideEffect.NavigateToDetail ->
                onNavigateToDetail(effect.pokeId, effect.backgroundColor)
        }
    }

    MainScreen(pokemons = pokemons, onPokemonClick = viewModel::onPokemonClick)
}
```

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CherryPokemonTheme {
                MainRoute(onNavigateToDetail = { id, color ->
                    startActivity(Intent(this, PokemonDetailActivity::class.java)
                        .putExtras(PokemonDetailActivity.create(id, color)))
                })
            }
        }
    }
}
```

- `Screen`은 상태만 받으므로 Preview와 UI 테스트에 ViewModel이 필요 없다.
- 나중에 Navigation Compose로 옮길 때 `Route`를 그대로 `composable {}` 안에 넣으면 된다.
- `hiltViewModel()`을 쓰므로 `hilt-navigation-compose`는 **남겨 둔다**.

**삭제:** `ui/view/base/base/` 패키지 전체 (`BaseActivity`, `BaseContainer`, `BaseContract`, `BaseViewModel`, `RealContainer`)

### 2-7. 테스트

```kotlin
@Test
fun `상세 조회에 실패하면 에러 상태가 된다`() = runTest {
    val viewModel = PokemonDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(POKE_NO to 25, POKEMON_BACKGROUND_COLOR to 0)),
        pokemonDetailUseCase = PokemonDetailUseCase(FailingPokemonRepository()),
    )

    viewModel.testWithInternalState(this) {
        runOnCreate()
        expectInternalState { copy(isLoading = false, isError = true) }
    }
}
```

- 초기 상태는 자동으로 검증되고, 확인하지 않은 상태나 SideEffect가 남아 있으면 테스트가 실패한다.
- mockk 대신 `PokemonRepository`를 구현한 Fake를 쓴다.

### 2-8. 주의할 점

| 함정 | 설명 |
|---|---|
| `reduce {}` 안에서 suspend 호출 | `reduce`는 값만 계산하는 순수 함수로 둔다. API 호출은 그 전에 끝낸다 |
| `intent {}`의 시작 스레드 | `intentLaunchingDispatcher` 기본값이 `Unconfined`라서 처음 suspend 되기 전까지는 호출한 스레드(메인)에서 실행된다 |
| `onCreate`는 `init`이 아님 | 처음 구독할 때 실행된다. 테스트에서 `runOnCreate()`를 빼먹으면 로딩이 일어나지 않는다 |
| `SavedStateHandle`을 받는 `orbitContainer` 오버로드 | `KSerializer<STATE>`가 필요하다. 상태 저장은 당분간 쓰지 않고, 화면 인자를 읽을 때만 `SavedStateHandle`을 쓴다 |
| 전역 `exceptionHandler` | 설정하면 intent 예외가 조용히 삼켜져서 화면이 로딩 상태에서 멈춘다. 크래시 리포팅용 안전망으로만 쓴다 |
| SideEffect를 두 곳에서 수집 | 디버그 빌드에서 `SideEffectMode.FAN_OUT_STRICT`로 두면 설정 실수가 바로 드러난다 |

### 2-9. 트레이드오프

- Orbit은 10 → 11 → 12가 15개월 사이에 나왔고 매번 큰 API 변화가 있었다. 버전을 올릴 때마다 비용이 든다.
- 라이브러리 없이 `StateFlow` + `Channel`로 만드는 방법(Google 공식 가이드)도 있다. 다만 지금 코드와 용어가 이미 Orbit 쪽이라 옮기는 비용은 Orbit이 가장 작고, 테스트 라이브러리도 같이 얻는다.

---

## Phase 3 — Compose UI·성능

### 3-1. Paging LoadState UI와 key

```kotlin
val refresh = pokemons.loadState.refresh
when {
    refresh is LoadState.Loading -> FullScreenLoading()
    refresh is LoadState.Error   -> FullScreenError(onRetry = pokemons::retry)
    pokemons.itemCount == 0      -> EmptyState()
    else -> LazyVerticalGrid(...) {
        items(count = pokemons.itemCount, key = pokemons.itemKey { it.id }) { index -> ... }
        when (pokemons.loadState.append) {
            is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) { AppendLoading() }
            is LoadState.Error   -> item(span = { GridItemSpan(maxLineSpan) }) { AppendRetry(pokemons::retry) }
            else -> Unit
        }
    }
}
```

지금은 `items(pokemons.itemCount)`에 key가 없어서 인덱스 기준으로 아이템을 재사용한다. 목록 앞에 아이템이 끼어들면 색상이 다른 카드에 붙는다.

### 3-2. 대표 색상 파이프라인

지금 구조의 문제:
- `fetchDominantColor`가 Glide `.submit().get()`으로 스레드를 막고, 같은 이미지를 한 번 더 다운로드한다.
- 색상이 `ImmutableMap`으로 State에 들어 있어서 하나가 바뀌면 맵 전체가 교체되고, 보이는 카드가 전부 리컴포즈된다. 처음 로딩할 때 O(n²).

**A (권장): Landscapist palette 플러그인.** 이미 받은 비트맵을 재사용해서 중복 다운로드가 없다.

```kotlin
GlideImage(
    imageModel = { image },
    component = rememberImageComponent {
        +PalettePlugin { palette ->
            onColorResolved(Color(palette.dominantSwatch?.rgb ?: Color.White.toArgb()))
        }
    },
)
```

**B (최소 변경):** State에서 빼서 `mutableStateMapOf<Int, Color>()`로 둔다. `SnapshotStateMap`은 읽은 키 단위로 구독하므로 해당 카드만 리컴포즈된다.

어느 쪽이든 색상은 화면 상태가 아니라 캐시이므로 State에서 뺀다.

### 3-3. Window 주입 제거, 상태바 충돌 해소

- `PokemonDetailScreen(window = window)`를 없앤다. Composable이 프레임워크 객체에 묶이면 Preview와 테스트를 할 수 없다.
- 지금은 `CherryPokemonTheme`이 `SideEffect`로 상태바를 `PrimaryFire`로 칠하고, `PokemonDetailScreen`이 포켓몬 색으로 다시 칠해서 서로 덮어쓴다.
- Theme에서 상태바 코드를 지우고, Activity에서 `enableEdgeToEdge()`를 호출한 뒤, 화면마다 `WindowInsets.statusBars` 영역에 배경을 그린다.

### 3-4. targetSdk 35 (3-3과 같은 PR)

- AGP 8.5.2 → 8.7 이상, compileSdk / targetSdk 34 → 35
- API 35에서는 edge-to-edge가 강제되고 `window.statusBarColor`는 동작하지 않는다. 그래서 3-3과 반드시 함께 진행한다.

### 3-5. 나머지

- `CherryPokemonTheme`의 `dynamicColor` 기본값이 `true`라서 Android 12 이상에서는 브랜드 색 스킴이 쓰이지 않는다. 브랜드 색을 쓰려면 `false`로 바꾼다.
- `PokemonCard`의 흰색 텍스트는 밝은 배경에서 대비가 부족하다. `luminance()`로 글자색을 고른다.
- `Shapes().medium`은 리컴포지션마다 객체를 새로 만든다. `MaterialTheme.shapes.medium`을 쓴다.
- `contentDescription`이 0건이다. 이미지에 최소한 포켓몬 이름을 넣는다.
- 타입 이름을 API 문자열 그대로 표시한다. 문자열 리소스로 옮긴다.

---

## Phase 4 — 모듈 경계

### 4-1. `:domain`을 순수 Kotlin 모듈로

```kotlin
plugins {
    `java-library`
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
dependencies {
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)
    api(libs.paging.common)   // Android가 아닌 JVM 아티팩트
    testImplementation(libs.junit)
}
```

- 지금 `:domain`은 Android 라이브러리이면서 Hilt, KSP, Parcelize, core-ktx, Paging을 전부 갖고 있는데, 실제로 쓰는 건 `javax.inject`와 `PagingData`뿐이다.
- `PagingData`까지 도메인에서 없애려면 자체 페이지 모델과 매핑이 필요한데, 이 규모에서는 비용 대비 효과가 낮다. `paging-common`이 현실적인 선택이다.
- 효과: Robolectric 없이 JUnit만으로 도메인 테스트를 돌릴 수 있고, Android API가 도메인에 들어오는 것을 컴파일 단계에서 막는다.

### 4-2. `:app → :data` 컴파일 의존 끊기

지금 `PokemonDetailViewModel`이 `com.cherryzp.data.extend.default`를 import 한다.

```
:core:common (신설, java-library)
  ├─ DefaultExtend.kt
  └─ KeyConsts.kt        // 지금은 domain에 있지만 도메인 개념이 아님
```

```kotlin
// app/build.gradle.kts
- implementation(project(":data"))
+ runtimeOnly(project(":data"))
```

`runtimeOnly`로 바꾸면 `:app`에서 data를 참조하는 순간 컴파일 에러가 난다. Hilt가 런타임 클래스패스에서 모듈을 찾으므로 동작은 해야 하지만, **이 변경만 따로 PR로 만들어 실기기에서 확인한다.**

### 4-3. 쓰지 않는 의존성 삭제

- `:data`: `appcompat`, `material`, Room 전부 (`@Entity` / `@Dao` 0건, `annotationProcessor`와 `ksp`가 중복 선언됨)
- `:app`: `room-paging`, `androidx.compose.material` (Material2 사용처 0건)
- `:domain`: Parcelize 플러그인, Hilt 플러그인, KSP, core-ktx

### 4-4. Repository·UseCase 시그니처

```kotlin
interface PokemonRepository {
    fun pokemonListStream(): Flow<PagingData<Pokemon>>   // Pager를 만드는 일은 suspend가 아님
    suspend fun pokemonDetail(pokeNo: Int): PokemonDetail
}
```

- `PokemonDetailUseCase`의 `flow { emit(...) }` 래핑을 없애고 `suspend operator fun invoke()`로 바꾼다.
- UseCase는 유지하거나(레이어를 일관되게) 없애거나(ViewModel이 Repository를 직접 사용) 둘 중 하나로 정한다. 지금처럼 그냥 전달만 하는 껍데기로 두는 게 가장 나쁘다.
- `PokemonPagingSource`의 `@Inject`는 Repository에서 직접 생성하므로 의미가 없다. 삭제한다.

### 4-5. DTO 직렬화

- `PokemonDetailResponse`는 `@SerializedName` 없이 snake_case 필드명에 기대고 있다. `PokemonSpeciesResponse`는 제대로 붙어 있어서 파일마다 규칙이 다르다.
- **R8을 켜면 필드명이 난독화되어 파싱 결과가 전부 null이 된다.** Phase 5-3보다 반드시 먼저 한다.
- 논-널로 선언된 DTO 필드(`Form.name` 등)는 Gson 리플렉션이 null을 넣을 수 있으므로 nullable로 바꾼다.
- 가능하면 kotlinx.serialization으로 바꾼다. 컴파일 타임에 검증되고, 리플렉션이 없고, R8에 안전하다.

---

## Phase 5 — 빌드 인프라

### 5-1. `buildSrc` → `build-logic` 컨벤션 플러그인

- 지금 `buildSrc`는 확장 함수 두 개만 제공하고, `compileOptions` / `kotlinOptions`는 세 모듈에 복사되어 있다.
- `buildSrc`를 고치면 전체 설정 단계가 무효화된다. included build로 바꾸면 이 문제가 없다.

```
build-logic/convention/src/main/kotlin/
  cherry.android.application.gradle.kts
  cherry.android.library.gradle.kts
  cherry.android.compose.gradle.kts
  cherry.jvm.library.gradle.kts
  cherry.hilt.gradle.kts
```

참고: [android/nowinandroid](https://github.com/android/nowinandroid)의 `build-logic`

### 5-2. gradle.properties

```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
```

`configuration-cache`는 Hilt 플러그인 버전에 따라 문제가 생길 수 있으니 마지막에 켜고 확인한다.

### 5-3. 릴리즈 빌드

- `BuildTaskRelease.isMinifyEnabled = false`이고 `proguard-rules.pro`가 비어 있다.
- `isMinifyEnabled = true`, `isShrinkResources = true`로 바꾼다. **4-5가 먼저 끝나 있어야 한다.**

---

## Phase 6 — 품질 게이트

### 6-1. 회귀 테스트

| 대상 | 확인 내용 | 넣을 PR |
|---|---|---|
| `PokemonMapper` / `Pokemon.id` | 비정상 URL이면 0 반환 | Phase 0 |
| `PokemonPagingSource` | `IOException`이면 `LoadResult.Error` 반환 | Phase 0 |
| `PokemonPagingSource` | `prevKey` / `nextKey`, 마지막 페이지의 `nextKey == null` | Phase 0 |
| `PokemonDetailViewModel` | 실패하면 에러 상태, `retry()`하면 다시 로딩 | Phase 2 |
| `MainViewModel` | 클릭하면 `NavigateToDetail` SideEffect 발생 | Phase 2 |
| `MainScreen` | `LoadState.Error`일 때 재시도 버튼 표시 | Phase 3 |

도구: `kotlinx-coroutines-test`, `orbit-test`, `turbine`, `androidx.paging:paging-testing`

### 6-2. 정적 분석

- ktlint (또는 spotless), detekt, Android Lint (`lintRelease` 실패 시 CI 실패)
- detekt 커스텀 룰로 `:app`에서 `com.cherryzp.data.*` import를 금지한다

### 6-3. GitHub Actions

```yaml
on: [push, pull_request]
jobs:
  build:
    steps:
      - setup-java 17 + gradle/actions/setup-gradle
      - ./gradlew ktlintCheck detekt
      - ./gradlew testDebugUnitTest
      - ./gradlew assembleDebug lintRelease
```

---

## 총평

골격(3-레이어 분리, 버전 카탈로그, Orbit 방식의 컨테이너, `ImmutableMap`, `collectAsStateWithLifecycle`)은 방향이 맞다. 실제 문제는 **정상 흐름만 구현되어 있다**는 것이다. 로딩·에러·빈 상태가 없고, 예외 경로를 확인한 적이 없다. Phase 0~2만 끝내도 체감 품질이 크게 달라진다.
