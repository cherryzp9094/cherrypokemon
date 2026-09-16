# Compose

> 코드 예시는 규칙을 보여주기 위한 것이다. 라이브러리 버전에 따라 API 이름이 다를 수 있으니 작성할 때 해당 버전 문서를 확인한다.
>
> [Compose API guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md)는 대상별로 요구 수준이 다르다. 앱 개발에 대한 SHOULD는 `권장`, MUST는 `필수`로 옮겼다.

## 1. 화면

### 1-1. 상태를 가진 Screen과 상태 없는 Screen을 같은 이름으로 오버로드한다 `필수`

```kotlin
@Composable
internal fun PokemonDetailScreen(
    viewModel: PokemonDetailViewModel,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.collectAsState()
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PokemonDetailSideEffect.ShowMessage ->
                snackbarHostState.showSnackbar(context.getString(sideEffect.messageId))
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

| | 상태를 가진 Screen | 상태 없는 Screen |
|---|---|---|
| 받는 것 | ViewModel, 내비게이션 콜백, 앱 수준 UI 객체 | UI 상태 값, 이벤트 콜백, `modifier` |
| 하는 일 | 상태·SideEffect 수집, ViewModel 함수를 콜백에 연결 | 그리기만 한다 |
| 부르는 곳 | entry builder | 상태를 가진 Screen, Preview, UI 테스트 |

- ViewModel을 받는 것은 상태를 가진 Screen뿐이다. 그 아래로는 필요한 값과 콜백만 넘긴다.
- Screen은 `Navigator`, back stack, `Window`, `Activity`를 받지 않는다.

근거: [State hoisting](https://developer.android.com/develop/ui/compose/state-hoisting) — `ConversationScreen` 오버로드 예시, "You shouldn't pass ViewModel instances down to other composables", [State holders](https://developer.android.com/topic/architecture/ui-layer/stateholders) Warning, [Previews](https://developer.android.com/develop/ui/compose/tooling/previews): ViewModel을 쓰는 composable은 값을 받는 버전을 만들어 Preview, [Test Compose navigation](https://developer.android.com/guide/navigation/testing/compose): "Don't pass the NavController directly into any composable"

### 1-2. 스낵바는 앱에 하나 `권장`

`SnackbarHostState`는 `CherryPokemonApp()`이 하나만 만들고, 필요한 화면에 파라미터로 전달한다.

근거: [UI events](https://developer.android.com/topic/architecture/ui-layer/events) — `LatestNewsScreen(snackbarHostState: SnackbarHostState, ...)` 예시, NiA `NiaApp`(앱에 하나)

### 1-3. edge-to-edge `필수`

- `MainActivity.onCreate()`에서 `enableEdgeToEdge()`를 호출한다.
- `AndroidManifest.xml`의 Activity에 `android:windowSoftInputMode="adjustResize"`를 둔다.
- 시스템 바·디스플레이 컷아웃과 겹치는 영역은 insets(padding modifier, size modifier, ruler)로 처리한다.
- 시스템 바 아이콘 밝기를 바꿀 때는 `WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars`를 쓴다.

근거: [Set up edge-to-edge](https://developer.android.com/develop/ui/compose/system/setup-e2e): Android 15(API 35) 이상 타깃은 edge-to-edge 강제

### 1-4. 뒤로가기 `필수`

- 표준 뒤로가기와 예측형 뒤로가기(predictive back)를 지원한다. `NavDisplay`의 `onBack`으로 back stack을 줄인다.
- `BackHandler` / `PredictiveBackHandler`는 UI 로직(다이얼로그 닫기 등)에만 쓴다. 비즈니스 로직이나 로그 기록에 쓰지 않는다.
- 콜백의 활성 여부는 관찰 가능한 상태로 정한다.

근거: [Predictive back](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture): "We strongly recommend that you implement predictive back navigation", "Use system back callbacks for UI Logic", [Core app quality](https://developer.android.com/docs/quality-guidelines/core-app-quality) Back_Button_Nav

## 2. 상태 호이스팅 `필수`

| 상태 | 둘 곳 |
|---|---|
| 한 Composable 안에서만 쓰고 로직이 단순한 UI 상태 (펼침, 애니메이션) | 그 Composable의 `remember` / `rememberSaveable` |
| 여러 Composable이 읽고 쓰는 UI 상태 (`LazyListState`) | 읽고 쓰는 Composable들의 가장 가까운 공통 부모 |
| 필드가 여러 개인 복잡한 UI 로직 | 일반 state holder 클래스 + `remember<이름>State()` |
| 비즈니스 로직이 필요한 상태 | ViewModel |

- 상태는 읽는 곳들의 최소 공통 부모 이상, 쓰는 곳 중 가장 높은 곳 이상으로 올린다. 같은 이벤트로 함께 바뀌는 상태는 함께 올린다.
- 상태를 필요 없는 Composable에 넘기지 않는다.
- UI 요소 상태는 비즈니스 로직이 읽거나 쓸 때만 ViewModel로 올린다.
- 호이스팅한 상태 파라미터는 `value: T` + `onValueChange: (T) -> Unit` 형태로 받는다. 상태 객체를 받으면 기본값을 `= remember<이름>State()`로 주고, `null`을 "내부에서 만들라"는 표시로 쓰지 않는다.
- 상태와 콜백을 감싼 래퍼 클래스를 만들기보다 개별 파라미터로 넘긴다(property drilling).

근거: [State and Jetpack Compose](https://developer.android.com/develop/ui/compose/state) Key Point(3규칙), [State hoisting](https://developer.android.com/develop/ui/compose/state-hoisting): "Hoist state to the lowest common ancestor and avoid passing it to composables that don't need it", "Property drilling is preferable over creating wrapper classes", [Compose API guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md): Default policies through hoisted state objects

## 3. Composable 파라미터

### 3-1. 필요한 정보만 받는다 `필수`

도메인 모델 전체보다 화면에 그릴 값만 받는다. 재사용하기 쉽고, 모델의 다른 필드가 바뀌어도 리컴포즈되지 않는다.

```kotlin
// ❌ Pokemon 인스턴스가 새로 오면 리컴포즈된다
@Composable fun PokemonCard(pokemon: Pokemon, ...)

// ✅
@Composable fun PokemonCard(name: String, number: Int, imageUrl: String, onClick: () -> Unit, modifier: Modifier = Modifier)
```

파라미터가 너무 많아지면 관련 값을 클래스로 묶는다.

근거: [Compose UI Architecture: Define composable parameters](https://developer.android.com/develop/ui/compose/architecture): "each composable should hold the least amount of information possible"

### 3-2. 순서와 modifier `권장`

- UI를 그리는 Composable은 `modifier: Modifier` 파라미터를 **하나** 받고, 이름은 `modifier`, **첫 번째 선택 파라미터**로 두고 기본값은 `Modifier`다.
- 받은 `modifier`는 UI를 내보내는 첫 자식(루트)에 전달한다. 추가 modifier는 받은 것 **뒤에** 이어 붙인다.
- `@Composable` 람다 파라미터가 하나면 이름을 `content`로 하고 마지막에 둔다.
- `scope` 전용 modifier(`weight`, `matchParentSize`)는 같은 스코프의 직계 자식에게만 넘긴다.

근거: [Compose API guidelines: Elements accept and respect a Modifier parameter / Compose UI layouts](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md#Elements-accept-and-respect-a-Modifier-parameter), [Modifiers](https://developer.android.com/develop/ui/compose/modifiers)

### 3-3. 이벤트 `필수`

- 모든 입력은 이벤트(콜백)로 올린다. UI는 이벤트 핸들러 밖에서 상태를 바꾸지 않는다.
- 상태에는 불변 값, 이벤트에는 람다를 넘긴다.
- 목록 아이템의 클릭도 ViewModel을 넘기지 않고 람다로 올린다.

근거: [Compose UI Architecture: Events](https://developer.android.com/develop/ui/compose/architecture): "The UI layer should never change state outside of an event handler", [UI events](https://developer.android.com/topic/architecture/ui-layer/events): User events in lazy lists

## 4. 디자인 시스템

### 4-1. Material 3 테마 `필수`

- `CherryPokemonTheme`은 `MaterialTheme(colorScheme, typography, shapes)`를 감싸고 `:core:designsystem`에 둔다.
- 라이트·다크 색 구성을 모두 제공한다.
- dynamic color를 쓸지는 선택이다. 쓴다면 Android 12 미만에서는 커스텀 라이트·다크 색으로 폴백한다.
- Material에 없는 색·모양이 필요하면 `ColorScheme`·`Shapes` 확장 프로퍼티로 추가하고, 테마가 여러 개면 `CompositionLocal`을 쓰는 확장 테마 객체로 만든다.
- 앱 UI는 Material 컴포넌트로 만든다. 컴포넌트 색은 `CardDefaults.cardColors(...)` 같은 Defaults로 바꾼다.

근거: [Material 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3), [Custom design systems](https://developer.android.com/develop/ui/compose/designsystems/custom), [Core app quality](https://developer.android.com/docs/quality-guidelines/core-app-quality): Theme_Support(라이트·다크), "Use Material design components"

### 4-2. 값을 직접 쓰지 않는다 `필수`

- 색·글꼴·모양은 `MaterialTheme.colorScheme` / `typography` / `shapes`에서 꺼낸다. `Color.White`, `fontSize = 22.sp`, `Shapes().medium`을 화면에 쓰지 않는다.
- 색 역할은 짝을 맞춘다. `primary` 위에는 `onPrimary`, `primaryContainer` 위에는 `onPrimaryContainer`.
- 데이터에서 온 색(포켓몬 대표 색) 위의 글자는 대비 4.5:1(작은 글자) 이상이 되도록 고른다.

```kotlin
// ❌ 밝은 대표 색 위에서 흰 글씨가 보이지 않는다
Text(text = name, color = Color.White)
```

근거: [Compose resources](https://developer.android.com/develop/ui/compose/resources) Warning: "Prefer colors from the theme rather than hard-coded colors", [Material 3: Color accessibility](https://developer.android.com/develop/ui/compose/designsystems/material3), [Core app quality](https://developer.android.com/docs/quality-guidelines/core-app-quality) Visual_Contrast

### 4-3. 공용 컴포넌트 위치 `권장`

| 모듈 | 담는 것 |
|---|---|
| `:core:designsystem` | 테마, 도메인을 모르는 기본 컴포넌트. Material 컴포넌트를 앱 모양에 맞춘 래퍼는 `Cherry` 접두사 |
| `:core:ui` | 도메인 값을 표시 방식으로 바꾸는 함수(예: `PokemonType` → 색), 여러 feature가 쓰는 컴포넌트, Preview 데이터 |

근거: [Modularization patterns](https://developer.android.com/topic/modularization/patterns): UI module, NiA `core/designsystem`, `core/ui`

### 4-4. 리소스 `필수`

- 사용자에게 보이는 문자열은 코드에 쓰지 않고 `strings.xml`에 둔다. 수량에 따라 바뀌는 문장은 plurals와 `pluralStringResource`를 쓴다.
- 리소스 이름은 모듈 경로 접두사로 시작한다. (`:feature:pokemondetail:impl` → `feature_pokemondetail_impl_error_refresh`)
- locale 등 설정 값은 `LocalConfiguration`으로 읽는다. `Locale.getDefault()`는 설정이 바뀌어도 리컴포즈되지 않는다.

```kotlin
// ❌
Text(text = "No.$id")

// ✅
Text(text = stringResource(R.string.core_ui_pokemon_number, id))
```

근거: [Localize your app](https://developer.android.com/guide/topics/resources/localization): "don't hardcode any strings", [String resources](https://developer.android.com/guide/topics/resources/string-resource), [Configuration changes](https://developer.android.com/guide/topics/resources/runtime-changes) Warning, NiA `resourcePrefix`

## 5. 이미지 `필수`

[라이브러리 예외](README.md#라이브러리-예외)에 따라 Landscapist(Glide)를 쓴다.

- 이미지는 Landscapist `GlideImage`로 불러온다. Glide API를 직접 호출하지 않는다.
- 대표 색상은 Landscapist palette 플러그인으로 이미 불러온 비트맵에서 뽑는다.
- 로딩·실패 상태를 지정하고, 로딩 전후 크기가 같도록 고정 크기나 placeholder를 둔다. 0px 아이템은 Lazy 목록이 필요 이상으로 아이템을 구성하게 만든다.

근거: [Landscapist 문서](https://skydoves.github.io/landscapist/), [Lists: Avoid using 0-pixel sized items](https://developer.android.com/develop/ui/compose/lists)

## 6. 목록과 Paging

| 규칙 | 강도 | 근거 |
|---|---|---|
| Lazy 목록 아이템에 안정적이고 고유한 `key`를 준다. Paging은 `itemKey { it.id }` | `필수` | [Paged data](https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data): "Make sure to provide a unique, stable identifier", [Performance best practices](https://developer.android.com/develop/ui/compose/performance/bestpractices) |
| 종류가 다른 아이템이 섞이면 `contentType`을 준다 | `권장` | [Lists](https://developer.android.com/develop/ui/compose/lists): "consider adding contentType" |
| `collectAsLazyPagingItems()`는 Composable에서 호출한다 | `필수` | Paged data Caution |
| 새로고침·추가 로딩의 로딩·에러와 재시도를 `loadState`로 그린다 | `필수` | [Load state](https://developer.android.com/topic/libraries/architecture/paging/load-state) |
| RemoteMediator를 쓰면 전체 화면 로딩은 `loadState.source`(로컬)와 `loadState.mediator`(네트워크)를 나눠 판단한다. 캐시가 있으면 목록을 보여준다 | `필수` | 같음: `loadState.refresh`는 둘의 차이를 가린다 |
| RemoteMediator를 쓰면 실제 크기에 가까운 placeholder를 준다 | `필수` | [Lists](https://developer.android.com/develop/ui/compose/lists) Warning |
| 같은 방향으로 스크롤되는 컴포넌트를 크기 지정 없이 중첩하지 않는다 | `필수` | 같음 |

## 7. 성능

### 7-1. 성능 모범 사례 `필수`

- 비싼 계산은 `remember(키)`로 캐시하거나 ViewModel로 옮긴다.
- 자주 바뀌는 상태에서 파생된 값은 `derivedStateOf`로 리컴포지션을 줄인다. (`derivedStateOf`는 비싸므로 결과가 덜 자주 바뀔 때만)
- 상태 읽기는 필요한 곳까지 늦춘다. 자주 바뀌는 값은 람다 modifier(`Modifier.offset { }`, `drawBehind { }`)로 읽는다.
- 이미 읽은 상태를 같은 컴포지션에서 다시 쓰지 않는다(backwards write). 상태는 이벤트 람다 안에서만 쓴다.
- 성능은 release 빌드 + R8에서 측정한다.

근거: [Performance best practices](https://developer.android.com/develop/ui/compose/performance/bestpractices), [Side effects](https://developer.android.com/develop/ui/compose/side-effects) derivedStateOf Caution, [Lists: Measuring performance](https://developer.android.com/develop/ui/compose/lists)

### 7-2. 안정성 `권장`

- Kotlin 2.0.20 이상의 strong skipping을 전제로 한다.
- 안정성 수정은 성능 문제를 진단한 뒤에 한다. 모든 Composable을 skippable로 만들려고 하지 않는다.
- 고칠 때는 순서대로: 클래스를 불변으로(모든 프로퍼티 `val` + 불변 타입) → 불변 컬렉션(`kotlinx.collections.immutable`) → stability configuration file → `@Stable` / `@Immutable`.
- `@Stable` / `@Immutable`은 컴파일러와의 계약이다. 어노테이션 없이 안정적으로 만들 수 있으면 붙이지 않는다.
- 다른 모듈의 모델(`:core:model`)은 Compose 컴파일러가 불안정으로 추론한다. 문제가 되면 stability configuration file에 등록한다.
- Room처럼 매번 새 객체를 할당하는 데이터를 관찰하는데 동등성 비교가 필요하면 `@Stable`을 유지한다.

근거: [Fix stability issues](https://developer.android.com/develop/ui/compose/performance/stability/fix): "Before you fix stability issues, you should learn to properly diagnose them", "Not every composable should be skippable", [Strong skipping](https://developer.android.com/develop/ui/compose/performance/stability/strongskipping)

### 7-3. 앱 설정 `권장`

앱 전용 Baseline Profile을 만든다.

근거: [Compose performance](https://developer.android.com/develop/ui/compose/performance): "ideally, you should create an app-specific one"

## 8. 부수 효과 `필수`

- 효과 블록에서 쓰는 변수는 효과의 키로 넘기거나 `rememberUpdatedState`로 감싼다.
- `LaunchedEffect(true)` / `LaunchedEffect(Unit)`은 정말 호출 위치의 수명을 따라야 할 때만 쓴다.
- 이벤트 콜백에서 코루틴이 필요하면 `rememberCoroutineScope()`를 쓴다.
- 정리가 필요한 효과는 `DisposableEffect`, 빈 `onDispose`는 두지 않는다.
- Compose 상태를 Compose가 관리하지 않는 객체에 알릴 때는 `SideEffect`를 쓴다.
- 화면이 실제로 보일 때만 실행해야 하면(분석 이벤트) `LaunchedEffect` 대신 `LifecycleEventEffect`를 쓴다.
- 애니메이션을 일으키는 Compose UI 상태의 suspend 함수(`animateScrollToItem`, `DrawerState.close`)는 Composition 스코프에서 호출한다.

근거: [Side-effects in Compose](https://developer.android.com/develop/ui/compose/side-effects): Key Point, "LaunchedEffect(true) is as suspicious as a while(true)", [Coroutines with lifecycle](https://developer.android.com/topic/libraries/architecture/coroutines) Note, [State hoisting](https://developer.android.com/develop/ui/compose/state-hoisting) Warning

## 9. 적응형 레이아웃 `필수`

- 레이아웃 분기는 기기 종류가 아니라 창 크기(`currentWindowAdaptiveInfo().windowSizeClass`)로 한다.
- 목록-상세처럼 나란히 보일 수 있는 흐름은 Compose Material 3 Adaptive(`ListDetailSceneStrategy`, `ListDetailPaneScaffold`)를 쓴다.
- 방향·폴딩 상태가 바뀌어도 같은 기능을 제공하고 상태를 잃지 않는다. 방향을 고정하지 않는다.

근거: [Adaptive apps](https://developer.android.com/develop/ui/compose/layouts/adaptive): "Use window size classes to make layout decisions / Build with the Compose Material 3 Adaptive library", [Guide to app architecture](https://developer.android.com/topic/architecture): "Don't assume that your app always stays fixed in a portrait or landscape orientation", [Core app quality](https://developer.android.com/docs/quality-guidelines/core-app-quality) Display_State_Parity, NiA `TopicEntryProvider`

## 10. 접근성 `필수`

- 상호작용하는 요소의 터치 영역은 최소 48dp. 작은 clickable 요소는 `sizeIn(minWidth = 48.dp, minHeight = 48.dp)`로 명시한다.
- 체크박스·스위치 같은 선택 컨트롤은 클릭을 부모 행으로 올리고 `toggleable`/`selectable`과 `role`을 준다.
- 정보를 전달하는 이미지·아이콘은 현지화된 문자열로 `contentDescription`을 채우고, 장식용은 `null`로 둔다.
- `clickable`은 자식의 semantics를 자동으로 합치므로 클릭 가능한 카드는 따로 병합하지 않는다.
- 글자 대비는 작은 글자 4.5:1, 큰 글자·그래픽 3:1 이상.

근거: [Accessibility API defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults), [Core app quality](https://developer.android.com/docs/quality-guidelines/core-app-quality): Touch_Target_Size, Visual_Contrast, Content_Description

## 11. Preview `권장`

- 상태 없는 Screen과 공용 컴포넌트는 대표 상태마다 Preview를 만든다. ViewModel을 받는 Screen은 Preview하지 않는다.
- 여러 조건은 공식 multipreview 템플릿(`@PreviewLightDark`, `@PreviewFontScales`, `@PreviewScreenSizes`)을 쓴다.
- 도메인 모델 샘플 데이터는 `PreviewParameterProvider`로 제공하고 `:core:ui`에 둔다.
- Preview는 테마로 감싼다.

근거: [Previews](https://developer.android.com/develop/ui/compose/tooling/previews): Multipreview templates, `@PreviewParameter`, Previews and ViewModels
