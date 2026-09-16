# Compose

> 코드 예시는 방향을 보여주기 위한 것이며, 이 프로젝트에서 아직 빌드해 보지 않았다.

## 1. Route와 Screen

### 1-1. 화면은 Route와 Screen으로 나눈다 `필수`

| | Route | Screen |
|---|---|---|
| 하는 일 | ViewModel을 받아 상태를 수집하고, 콜백을 연결한다 | 상태를 받아 그리기만 한다 |
| 파라미터 | `viewModel`, 내비게이션 콜백 | UiState, 이벤트 콜백, `modifier` |
| 호출하는 곳 | `CherryPokemonApp()`의 `entry<NavKey>` | Route, Preview, UI 테스트 |
| Preview | 만들지 않는다 | 상태마다 만든다 |

```kotlin
@Composable
fun PokemonDetailRoute(
    onBackClick: () -> Unit,
    viewModel: PokemonDetailViewModel,
) {
    val uiState by viewModel.collectAsState()

    PokemonDetailScreen(
        uiState = uiState,
        onRetryClick = viewModel::retry,
        onBackClick = onBackClick,
    )
}

@Composable
fun PokemonDetailScreen(
    uiState: PokemonDetailUiState,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        PokemonDetailUiState.Loading -> FullScreenLoading(modifier)
        PokemonDetailUiState.Error -> FullScreenError(onRetryClick = onRetryClick, modifier = modifier)
        is PokemonDetailUiState.Success -> PokemonDetailContent(uiState.pokemonDetail, onBackClick, modifier)
    }
}
```

인자가 없는 ViewModel은 Route의 기본값으로 `hiltViewModel()`을 둔다. NavKey가 필요한 ViewModel은 `entry`에서 만들어 넘긴다. ([architecture.md 4-5](architecture.md#4-5-viewmodel에-화면-인자-전달-필수))

근거: [State hoisting: Screen UI state](https://developer.android.com/develop/ui/compose/state-hoisting), [Recommendations: Use ViewModels at screen level](https://developer.android.com/topic/architecture/recommendations#viewmodel)

### 1-2. ViewModel을 하위 Composable로 넘기지 않는다 `필수`

ViewModel은 Route에서 멈춘다. 그 아래에는 상태 값과 콜백만 넘긴다.

근거: [State hoisting: Screen UI state](https://developer.android.com/develop/ui/compose/state-hoisting). 공식 문서가 ViewModel을 하위 Composable로 넘기지 말라고 경고한다.

### 1-3. 프레임워크 객체를 넘기지 않는다 `필수`

`Window`, `Activity`, `NavBackStack`을 Composable 파라미터로 받지 않는다. Preview와 테스트를 할 수 없게 된다.

```kotlin
// ❌ PokemonDetailScreen.kt: 화면이 Window를 받아 상태바 색을 직접 바꾼다
fun PokemonDetailScreen(
    paddingValues: PaddingValues,
    window: Window?,
    ...
) {
    LaunchedEffect(pokemonBackgroundColor) {
        window?.let { it.statusBarColor = Color(pokemonBackgroundColor).toArgb() }
    }
```

상태바 영역은 `enableEdgeToEdge()` 후 화면이 `WindowInsets.statusBars`만큼 배경을 그려서 처리한다. API 35부터 `window.statusBarColor`는 동작하지 않는다.

## 2. 상태 호이스팅 `권장`

| 상태 | 둘 곳 |
|---|---|
| 한 Composable 안에서만 쓰는 UI 상태 (펼침 여부 등) | 그 Composable의 `remember` / `rememberSaveable` |
| 여러 Composable이 함께 읽고 쓰는 UI 상태 (`LazyGridState` 등) | 공통 부모 중 가장 가까운 곳 |
| 필드가 여러 개인 복잡한 UI 로직 | 일반 state holder 클래스 + `rememberXxxState()` |
| 비즈니스 로직이 필요한 화면 상태 | ViewModel의 UiState |

- 설정 변경 후에도 남아야 하는 UI 상태는 `rememberSaveable`을 쓴다.
- 이미지 대표 색상처럼 화면 상태가 아닌 **캐시**는 UiState에 넣지 않는다.

근거: [State hoisting: Where to hoist state](https://developer.android.com/develop/ui/compose/state-hoisting)

## 3. Composable 파라미터

### 3-1. 순서 `필수`

1. 필수 파라미터 (상태, 이벤트 콜백)
2. `modifier: Modifier = Modifier`
3. 기본값이 있는 선택 파라미터
4. 마지막 `content: @Composable () -> Unit` (있으면)

```kotlin
// ❌ PokemonDetailScreen.kt: 레이아웃 값이 맨 앞에 있고 modifier가 없다
fun PokemonDetailScreen(
    paddingValues: PaddingValues,
    window: Window?,
    pokemonDetail: PokemonDetail,
    pokemonBackgroundColor: Int
)
```

근거: [Compose API guidelines: Parameter order](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md#Elements-accept-and-respect-a-Modifier-parameter)

### 3-2. modifier `필수`

- UI를 그리는 Composable은 `modifier: Modifier = Modifier`를 받는다.
- 받은 `modifier`는 **가장 바깥 레이아웃 하나에만** 적용하고, 내부 modifier는 그 뒤에 이어 붙인다.
- 하위 요소에는 새 `Modifier`로 시작한다.

```kotlin
// ✅ PokemonCard.kt는 이미 맞다
Column(modifier = modifier.clip(...).padding(8.dp))
```

근거: [Compose API guidelines: Modifier parameter](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md#Elements-accept-and-respect-a-Modifier-parameter)

### 3-3. 받지 않는 타입 `필수`

| 받지 않는 것 | 대신 |
|---|---|
| `MutableState<T>` | 값 `T` + `onValueChange: (T) -> Unit` |
| `Flow`, `StateFlow` | 수집한 값 |
| ViewModel, Repository | 상태와 콜백 |
| nullable로 "아직 없음"을 표현한 목록 (`LazyPagingItems<T>?`) | 로딩 상태를 UiState나 `loadState`로 표현 |

근거: [Compose API guidelines: Prefer stateless and controlled Composable functions](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md#Prefer-stateless-and-controlled-Composable-functions)

## 4. 부수 효과 `권장`

- `LaunchedEffect`의 key는 효과를 다시 시작해야 하는 값으로 한다. `LaunchedEffect(Unit)`은 정말 한 번만 실행할 때만 쓴다.
- 오래 도는 효과 안에서 부르는 콜백은 `rememberUpdatedState`로 최신 값을 참조한다.
- `SideEffect`로 시스템 UI를 바꾸지 않는다. 테마와 화면이 서로 덮어쓰게 된다.

```kotlin
// ❌ Theme.kt와 PokemonDetailScreen.kt가 각각 상태바 색을 칠해서 서로 덮어쓴다
SideEffect { window.statusBarColor = ... }
```

근거: [Side-effects in Compose](https://developer.android.com/develop/ui/compose/side-effects)

## 5. 성능과 안정성

### 5-1. 안정성 어노테이션으로 우기지 않는다 `권장`

- Kotlin 2.0.20부터 strong skipping이 기본으로 켜져서, 불안정한 파라미터도 인스턴스가 같으면 리컴포지션을 건너뛴다.
- 그래서 `@Stable` / `@Immutable`은 실제로 불변이고 동등성 비교가 필요할 때만 붙인다. `Flow`처럼 불변이 아닌 값을 담은 클래스에 붙이지 않는다.
- UiState의 컬렉션은 `ImmutableList` / `ImmutableMap`을 쓴다.

지금 프로젝트는 Kotlin 2.0.0이라 strong skipping이 꺼져 있다. 개선 계획 Phase 1에서 올라간다.

근거: [Strong skipping mode](https://developer.android.com/develop/ui/compose/performance/stability/strongskipping), [Stability](https://developer.android.com/develop/ui/compose/performance/stability)

### 5-2. Lazy 목록에는 key를 준다 `필수`

```kotlin
// ❌ MainScreen.kt: 인덱스 기준이라 앞에 아이템이 끼면 상태가 다른 카드에 붙는다
items(pokemons.itemCount) { index -> ... }

// ✅
items(
    count = pokemons.itemCount,
    key = pokemons.itemKey { it.id },
) { index -> ... }
```

근거: [Performance best practices: Use lazy layout keys](https://developer.android.com/develop/ui/compose/performance/bestpractices)

### 5-3. 리컴포지션마다 새로 만들지 않는다 `권장`

- 비용이 드는 계산과 객체는 `remember`로 감싸거나 Composable 밖으로 뺀다.
- 자주 바뀌는 값에서 파생된 값은 `derivedStateOf`를 쓴다.
- 스크롤 위치처럼 매 프레임 바뀌는 값은 람다 modifier(`Modifier.offset { }`, `graphicsLayer { }`)로 읽기를 늦춘다.
- 이미 읽은 상태를 같은 컴포지션에서 다시 쓰지 않는다(backwards write).

```kotlin
// ❌ PokemonCard.kt: 호출할 때마다 Shapes 객체를 새로 만든다
.clip(Shapes().medium)

// ✅
.clip(MaterialTheme.shapes.medium)
```

근거: [Performance best practices](https://developer.android.com/develop/ui/compose/performance/bestpractices)

## 6. 테마, 리소스, 접근성

### 6-1. 색·글꼴·모양은 테마에서 꺼낸다 `권장`

`MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`를 쓴다. `Color.White`, `fontSize = 22.sp` 같은 값을 화면에 직접 쓰지 않는다. 포켓몬 대표 색처럼 데이터에서 온 색 위의 글자색은 배경의 `luminance()`로 고른다.

```kotlin
// ❌ PokemonCard.kt: 밝은 대표 색(피카츄 노랑) 위에서 흰 글씨가 안 보인다
Text(text = name, color = Color.White)
```

### 6-2. 사용자에게 보이는 문자열은 리소스로 `필수`

```kotlin
// ❌ MainScreen.kt, PokemonCard.kt
Text(text = "Cherry Pokemon")
Text(text = "No.$id")

// ✅
Text(text = stringResource(R.string.app_title))
Text(text = stringResource(R.string.pokemon_number, id))
```

### 6-3. 이미지에 contentDescription `필수`

- 정보를 전달하는 이미지는 `contentDescription`을 채운다. (예: 포켓몬 이미지 → 포켓몬 이름)
- 장식용 이미지는 `contentDescription = null`로 명시한다.
- 클릭 가능한 요소는 최소 48dp 터치 영역을 갖는다.

근거: [Accessibility in Compose](https://developer.android.com/develop/ui/compose/accessibility)

## 7. Preview `권장`

- Screen마다 대표 상태(`Loading`, `Error`, `Success`)별 Preview를 만든다.
- Preview는 `private`이고 이름은 `<Composable>Preview`로 짓는다. (예: `PokemonDetailScreenErrorPreview`)
- 테마로 감싼다: `CherryPokemonTheme { ... }`
