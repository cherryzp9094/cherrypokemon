# Compose

> 코드 예시는 규칙을 보여주기 위한 것이다. 라이브러리 버전에 따라 API 이름이 다를 수 있으니 작성할 때 해당 버전 문서를 확인한다.

## 1. 화면

### 1-1. 상태를 가진 Screen과 상태 없는 Screen을 오버로드한다 `필수`

| | 상태를 가진 Screen | 상태 없는 Screen |
|---|---|---|
| 파라미터 | `viewModel`, 내비게이션 콜백, `modifier` | UiState, 이벤트 콜백, `modifier` |
| 하는 일 | ViewModel 상태·SideEffect 수집, ViewModel 함수를 콜백에 연결 | 상태를 그리기만 한다 |
| 부르는 곳 | entry provider | 상태를 가진 Screen, Preview, UI 테스트 |
| 공개 범위 | `internal` | `internal` |

```kotlin
@Composable
internal fun PokemonDetailScreen(
    viewModel: PokemonDetailViewModel,
    onBackClick: () -> Unit,
    onShowSnackbar: suspend (message: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.collectAsState()
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PokemonDetailSideEffect.ShowMessage ->
                onShowSnackbar(context.getString(sideEffect.messageId))
        }
    }

    PokemonDetailScreen(
        uiState = uiState,
        onRetryClick = viewModel::retry,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
internal fun PokemonDetailScreen(
    uiState: PokemonDetailUiState,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) { ... }
```

근거: [State hoisting: Screen UI state](https://developer.android.com/develop/ui/compose/state-hoisting), NiA `TopicScreen`

### 1-2. ViewModel은 화면 Composable에서 멈춘다 `필수`

ViewModel을 받는 것은 상태를 가진 Screen뿐이다. 그 아래로는 상태 값과 콜백만 넘긴다.

근거: [State hoisting](https://developer.android.com/develop/ui/compose/state-hoisting) — ViewModel을 하위 Composable로 넘기지 말라고 경고한다

### 1-3. 프레임워크 객체를 받지 않는다 `필수`

`Window`, `Activity`, `NavBackStack`, `Navigator`를 Screen 파라미터로 받지 않는다. Preview와 테스트를 할 수 없게 된다.

```kotlin
// ❌ 화면이 Window를 받아 상태바 색을 직접 바꾼다
fun PokemonDetailScreen(window: Window?, ...) {
    LaunchedEffect(color) { window?.statusBarColor = color }
}
```

### 1-4. 스낵바는 앱에 하나 `필수`

`SnackbarHostState`는 `CherryPokemonApp()`이 하나만 갖는다. 화면은 `onShowSnackbar` 콜백으로 요청한다.

근거: NiA `NiaApp`

### 1-5. edge-to-edge `필수`

- `MainActivity`에서 `enableEdgeToEdge()`를 부른다.
- 시스템 바 영역은 `Scaffold`의 `contentWindowInsets`나 `WindowInsets.safeDrawing` 등으로 처리한다.
- `window.statusBarColor`, `setDecorFitsSystemWindows`를 쓰지 않는다. API 35부터 edge-to-edge가 강제되어 동작하지 않는다.

근거: [Edge-to-edge in Compose](https://developer.android.com/develop/ui/compose/system/setup-e2e)

## 2. 상태 호이스팅 `필수`

| 상태 | 둘 곳 |
|---|---|
| 한 Composable 안에서만 쓰는 UI 상태 (펼침 여부 등) | 그 Composable의 `remember` / `rememberSaveable` |
| 여러 Composable이 함께 읽고 쓰는 UI 상태 (`LazyGridState` 등) | 공통 부모 중 가장 가까운 곳 |
| 필드가 여러 개인 복잡한 UI 로직 | 일반 state holder 클래스 + `rememberXxxState()` |
| 비즈니스 로직이 필요한 화면 상태 | ViewModel의 UiState |

- 설정 변경과 프로세스 종료 후에도 남아야 하는 UI 상태는 `rememberSaveable`을 쓴다.

근거: [State hoisting: Where to hoist state](https://developer.android.com/develop/ui/compose/state-hoisting)

## 3. Composable 파라미터 `필수`

### 3-1. 순서

1. 필수 파라미터 (상태, 이벤트 콜백)
2. `modifier: Modifier = Modifier` — 첫 번째 선택 파라미터
3. 기본값이 있는 나머지 선택 파라미터
4. 마지막에 주 content 람다 (있으면, trailing lambda로 쓸 수 있게)

### 3-2. modifier

- UI를 그리는 Composable은 `modifier` 파라미터를 하나만 받는다.
- 받은 `modifier`는 **가장 바깥 레이아웃 하나에만** 적용하고, 내부 modifier는 그 뒤에 이어 붙인다.
- 하위 요소에는 새 `Modifier`로 시작한다.

근거: [Compose API guidelines: Elements accept and respect a Modifier parameter](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md#Elements-accept-and-respect-a-Modifier-parameter)

### 3-3. 받지 않는 타입

| 받지 않는 것 | 대신 |
|---|---|
| `MutableState<T>` | 값 `T` + `onValueChange: (T) -> Unit` |
| `Flow`, `StateFlow` | 수집한 값 |
| ViewModel, Repository | 상태와 콜백 (상태를 가진 Screen만 예외) |
| "아직 없음"을 뜻하는 nullable 컬렉션 | 로딩 상태를 UiState나 `loadState`로 표현 |

근거: [Compose API guidelines: Prefer stateless and controlled Composable functions](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md#Prefer-stateless-and-controlled-Composable-functions)

## 4. 디자인 시스템

### 4-1. 테마와 기본 컴포넌트는 `:core:designsystem` `필수`

- 테마 함수는 `CherryPokemonTheme`, 색·타이포·모양 토큰은 이 모듈에만 정의한다.
- Material 컴포넌트를 앱 모양에 맞춘 래퍼는 `Cherry` 접두사를 붙여 이 모듈에 둔다. (`CherryTopAppBar`, `CherryLoadingWheel`)
- feature에서는 래퍼가 있으면 Material 컴포넌트 대신 래퍼를 쓴다.
- 이 모듈은 도메인 모델을 모른다.

근거: NiA `core/designsystem`, [Material 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)

### 4-2. 도메인 모델을 받는 공용 컴포넌트는 `:core:ui` `필수`

`PokemonCard(pokemon: Pokemon)`처럼 도메인 모델을 받아 그리는 컴포넌트, 도메인 값을 색·아이콘으로 바꾸는 함수는 `:core:ui`에 둔다.

```kotlin
// ❌ 도메인 모델(enum)이 Compose Color를 갖는다
enum class PokemonTypeEnum(val color: Color) { NORMAL(Color(0xFFA8A878)), ... }

// ✅ :core:model은 값만, :core:ui가 색으로 바꾼다
enum class PokemonType { Normal, Fighting, ... }

@Composable
fun PokemonType.containerColor(): Color = when (this) { ... }
```

### 4-3. 값을 직접 쓰지 않는다 `필수`

- 색·글꼴·모양·간격은 `MaterialTheme.colorScheme` / `typography` / `shapes`와 디자인 시스템 토큰에서 꺼낸다. `Color.White`, `fontSize = 22.sp`, `Shapes().medium`을 화면에 쓰지 않는다.
- 데이터에서 온 색(포켓몬 대표 색) 위의 글자색은 배경의 `luminance()`로 고른다.
- 테마는 라이트·다크를 모두 제공한다.

```kotlin
// ❌ 호출할 때마다 Shapes 객체를 새로 만들고, 밝은 배경에서 흰 글씨가 보이지 않는다
.clip(Shapes().medium)
Text(text = name, color = Color.White)
```

### 4-4. 리소스 `필수`

- 사용자에게 보이는 문자열은 모두 `strings.xml`에 둔다.
- 리소스 이름은 모듈 경로 접두사로 시작한다. (`:feature:pokemondetail:impl` → `feature_pokemondetail_impl_error_network`) `resourcePrefix`로 검사한다. ([build.md 2-3](build.md#2-3-리소스-접두사와-namespace-필수))

```kotlin
// ❌
Text(text = "No.$id")

// ✅
Text(text = stringResource(R.string.core_ui_pokemon_number, id))
```

## 5. 이미지 `필수`

[라이브러리 예외](README.md#라이브러리-예외)에 따라 Landscapist(Glide)를 쓴다.

- 이미지는 Landscapist `GlideImage`로만 불러온다. Glide API를 직접 호출하지 않는다.
- 대표 색상은 Landscapist의 palette 플러그인으로 **이미 불러온 비트맵에서** 뽑는다. 같은 이미지를 따로 다시 받지 않는다.
- 로딩·실패 상태를 지정한다.

근거: [Landscapist 문서](https://skydoves.github.io/landscapist/)

## 6. 목록과 Paging `필수`

- Lazy 목록의 `items`에는 `key`를 준다. Paging은 `itemKey { it.id }`, 종류가 섞이면 `contentType`도 준다.
- `LazyPagingItems.loadState`로 새로고침(`refresh`), 추가 로딩(`append`), 원격 동기화(`mediator`) 상태를 그린다.
- 캐시된 데이터가 있으면 새로고침 실패 중에도 목록을 보여주고 에러는 스낵바로 알린다.

```kotlin
// ❌ 인덱스 기준이라 앞에 아이템이 끼면 상태가 다른 카드에 붙는다
items(pokemons.itemCount) { index -> ... }

// ✅
items(
    count = pokemons.itemCount,
    key = pokemons.itemKey { it.id },
) { index -> ... }
```

근거: [Performance best practices: Use lazy layout keys](https://developer.android.com/develop/ui/compose/performance/bestpractices), [Paging: Display loading states](https://developer.android.com/topic/libraries/architecture/paging/load-state)

## 7. 성능과 안정성

### 7-1. 안정성 `필수`

- Kotlin 2.0.20+의 strong skipping을 전제로 한다.
- `@Stable` / `@Immutable`은 실제로 그 계약을 지키는 타입에만 붙인다. `Flow`나 가변 컬렉션을 가진 클래스에 붙이지 않는다.
- UiState와 파라미터의 컬렉션은 `ImmutableList` / `ImmutableMap`이다.
- 다른 모듈의 도메인 모델은 `:core:model`이 Compose 컴파일러를 쓰지 않아 불안정으로 추론된다. 필요하면 Compose 컴파일러의 stability configuration 파일에 등록한다.

근거: [Stability](https://developer.android.com/develop/ui/compose/performance/stability), [Strong skipping](https://developer.android.com/develop/ui/compose/performance/stability/strongskipping), [Fix stability issues](https://developer.android.com/develop/ui/compose/performance/stability/fix)

### 7-2. 리컴포지션 비용 `필수`

- 비용이 드는 계산은 `remember`로 감싸거나 ViewModel로 옮긴다.
- 자주 바뀌는 값에서 파생된 값은 `derivedStateOf`를 쓴다.
- 매 프레임 바뀌는 값(스크롤 위치 등)은 람다 modifier(`Modifier.offset { }`, `graphicsLayer { }`)로 읽기를 늦춘다.
- 이미 읽은 상태를 같은 컴포지션에서 다시 쓰지 않는다(backwards write).

근거: [Performance best practices](https://developer.android.com/develop/ui/compose/performance/bestpractices)

## 8. 부수 효과 `필수`

- `LaunchedEffect`의 key는 효과를 다시 시작해야 하는 값으로 한다.
- 오래 도는 효과 안에서 부르는 콜백은 `rememberUpdatedState`로 참조한다.
- 시스템 UI를 `SideEffect`로 바꾸지 않는다.

근거: [Side-effects in Compose](https://developer.android.com/develop/ui/compose/side-effects)

## 9. 화면 크기 대응 `권장`

- 레이아웃 분기는 기기 종류가 아니라 창 크기(`WindowSizeClass`)로 한다.
- 목록-상세처럼 넓은 화면에서 나란히 보일 수 있는 흐름은 Navigation 3의 `ListDetailSceneStrategy`를 쓴다.

근거: [Adaptive layouts](https://developer.android.com/develop/ui/compose/layouts/adaptive), [Navigation 3 scenes](https://developer.android.com/guide/navigation/navigation-3/scenes), NiA `TopicEntryProvider`

## 10. 접근성 `필수`

- 정보를 전달하는 이미지는 `contentDescription`을 채운다(포켓몬 이미지 → 포켓몬 이름). 장식용은 `null`로 명시한다.
- 클릭 가능한 요소는 최소 48dp 터치 영역을 갖는다.
- 카드처럼 여러 요소가 하나의 의미를 가지면 `Modifier.semantics(mergeDescendants = true)`로 묶는다.
- 색만으로 정보를 전달하지 않는다. (타입은 색과 함께 이름을 표시)

근거: [Accessibility in Compose](https://developer.android.com/develop/ui/compose/accessibility)

## 11. Preview `필수`

- 상태 없는 Screen과 공용 컴포넌트는 대표 상태마다 Preview를 만든다.
- Preview는 `private`이고 테마로 감싼다.
- 여러 기기 크기·다크 모드를 한 번에 보는 multipreview 어노테이션(`@DevicePreviews`)과 도메인 모델용 `PreviewParameterProvider`는 `:core:ui`에 둔다.

근거: [Preview your UI](https://developer.android.com/develop/ui/compose/tooling/previews), NiA `core/ui/DevicePreviews.kt`
