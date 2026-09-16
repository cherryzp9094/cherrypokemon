# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

PokeAPI 기반 포켓몬 도감 앱. Jetpack Compose + 커스텀 MVI + 3-레이어 멀티모듈 구성의 학습용 프로젝트.

## 컨벤션

코드를 쓰거나 고치기 전에 [`docs/conventions/`](docs/conventions/README.md)에서 해당 영역 문서를 읽는다.

| 작업 | 문서 |
|---|---|
| 모듈·레이어, ViewModel·UiState, 내비게이션 | `architecture.md` |
| 이름, 포맷, 코루틴 | `kotlin.md` |
| Composable | `compose.md` |
| Repository, UseCase, DTO, DI | `data.md` |
| 테스트 | `testing.md` |

이 파일의 아래 내용은 **지금 코드**의 설명이고, 컨벤션은 **목표 상태**다. 개선 계획의 해당 Phase가 끝나기 전에는 그 영역을 기존 구조에 맞춰 고친다.

## 명령어

```bash
./gradlew assembleDebug              # 디버그 APK 빌드
./gradlew installDebug               # 연결된 기기에 설치
./gradlew clean

# Hilt / KSP 에러만 빠르게 확인 (전체 빌드보다 훨씬 빠름)
./gradlew :app:kspDebugKotlin

# 단위 테스트
./gradlew testDebugUnitTest                      # 전 모듈
./gradlew :data:testDebugUnitTest                # 특정 모듈
./gradlew :data:testDebugUnitTest --tests "com.cherryzp.data.ExampleUnitTest"
./gradlew :data:testDebugUnitTest --tests "*.PokemonMapperTest.url에서 id를 파싱한다"

./gradlew connectedDebugAndroidTest  # 계측 테스트 (기기/에뮬레이터 필요)
./gradlew lint                       # Android Lint
```

ktlint / detekt / CI 는 아직 구성되어 있지 않다.

`buildSrc` 를 수정하면 전체 프로젝트의 configuration 이 무효화되어 빌드가 느려진다.

## 모듈 구조

```
:app     Compose UI + MVI (presentation)
:data    Retrofit / Paging / DI / DTO·Mapper
:domain  model, repository interface, usecase
buildSrc 공유 빌드 로직 (컨벤션 플러그인이 아닌 확장 함수 방식)
```

의존 방향: `:app → :data`, `:app → :domain`, `:data → :domain`

> `:app → :data` 는 DI 조립용이 아니라 **실제 코드 참조**다. `PokemonDetailViewModel` 이 `com.cherryzp.data.extend.default` 를 import 하고 있다. 레이어 위반이므로 새 코드에서 따라하지 말 것.

## MVI 구조 (Orbit-MVI 모방, 직접 구현)

`app/src/main/java/com/cherryzp/cherrypokemon/ui/view/base/base/` 의 5개 파일이 전체 흐름을 이룬다. 화면 하나를 추가하려면 이 구조를 먼저 이해해야 한다.

```
BaseViewModel<S : UiState>  ──has──▶ Container<S>  ──impl──▶ RealContainer<S>
       │                                   │
       │ reduceState / postSideEffect      ├─ uiState      : StateFlow<S>
       ▼                                   └─ uiSideEffect : SharedFlow<UiSideEffect>
  ContainerContext<S>  (state 읽기 + reduce + postSideEffect 를 묶은 컨텍스트)
```

- **State**: `BaseActivity.BuildContent()` 안에서 `viewModel.container.uiState.collectAsStateWithLifecycle()` 로 수집한다. `observe()` 는 State 를 수집하지 않는다 — SideEffect 전용이다.
- **SideEffect**: `BaseActivity.onCreate` → `observe()` → `repeatOnLifecycle(STARTED)` 로 수집되어 `handleSideEffect()` 로 전달된다. 네비게이션 같은 1회성 이벤트에 사용.
- **Intent 개념은 없다.** View 가 `viewModel::goPokemonDetail` 처럼 ViewModel 의 public 메서드를 직접 호출한다.

### 화면 추가 시 규약

화면마다 3개 파일을 `ui/view/<screen>/` 에 만든다.

| 파일 | 내용 |
|---|---|
| `XxxContract.kt` | `XxxUiState : UiState()`, `sealed class XxxUiSideEffect : UiSideEffect` |
| `XxxViewModel.kt` | `BaseViewModel<XxxUiState>()`, `initialState` 오버라이드 |
| `XxxActivity.kt` | `BaseActivity<XxxViewModel, XxxUiState>()`, `BuildContent()` + `handleSideEffect()` |

`@AndroidEntryPoint` 를 Activity 에, `@HiltViewModel` 을 ViewModel 에 붙인다.

**`@HiltViewModel` 은 의존성이 없어도 `@Inject constructor()` 를 명시해야 한다.** 생략하면 KSP 가 "should contain exactly one @Inject or @AssistedInject annotated constructor" 로 실패한다.

### 화면 간 데이터 전달

Navigation Compose 를 쓰지 않는다. **Activity + Intent extras + `SavedStateHandle`** 조합이다.

```kotlin
// 보내는 쪽: companion object 의 create() 가 Bundle 을 만든다
startActivity(Intent(this, PokemonDetailActivity::class.java)
    .putExtras(PokemonDetailActivity.create(pokeId, bgColor)))

// 받는 쪽: ViewModel 이 SavedStateHandle 로 읽는다
savedStateHandle.get<Int>(POKE_NO)
```

키는 `domain/src/main/java/com/cherryzp/consts/KeyConsts.kt` 에 모아둔다. 이 상수는 Retrofit `@Path` 이름으로도 재사용된다 (`PokemonApi`).

## 데이터 흐름

```
PokemonApi (Retrofit)
  └─ PokemonPagingSource ──▶ Pager ──▶ Flow<PagingData<Pokemon>>
       └─ PokemonRepositoryImpl ──▶ PokemonRepository (domain 인터페이스)
            └─ PokemonListUseCase / PokemonDetailUseCase
                 └─ ViewModel ──▶ UiState ──▶ Compose
```

- DTO → 도메인 변환은 `data/mapper/` 의 `toDomain()` 확장 함수로만 한다. DTO 필드는 nullable 로 두고 매퍼에서 `default()` / `orEmpty()` 로 기본값을 채운다.
- DI 는 `data/di/` 에만 있다. `ApiModule`(`@Provides`, `internal object`) + `RepositoryModule`(`@Binds`, `interface`). 둘 다 `SingletonComponent`.
- `Pokemon.id` 와 `Pokemon.imageUrl` 은 저장 필드가 아니라 `url` 문자열에서 매번 파싱하는 computed property 다.

## 버전 관리

**버전이 두 군데로 나뉘어 있다.** 수정 시 어느 쪽인지 확인할 것.

| 위치 | 담당 |
|---|---|
| `gradle/libs.versions.toml` | 라이브러리·플러그인 버전, 번들 |
| `buildSrc/.../app/Versions.kt` | `COMPILE_SDK`, `MIN_SDK`, `TARGET_SDK`, Java 버전 |
| `buildSrc/.../app/BuildTaskObject.kt` | 빌드 타입별 `isMinifyEnabled` / `isDebuggable` |

`buildSrc/.../app/BaseExtension.kt` 의 `setConfigs()` / `setBuildType()` 을 각 모듈 `build.gradle.kts` 에서 호출한다.

플러그인은 alias 가 아니라 `id(libs.plugins.xxx.get().pluginId)` 형태로 적용한다.

## 알려진 부채

새 코드를 쓸 때 아래를 답습하지 말고, 근처를 수정하게 되면 함께 고칠 것. 전체 개선 순서는 `docs/improvement-plan.md` 에 있다.

- `PokemonPagingSource.load()` 의 API 호출이 `try` 블록 **밖**에 있어 네트워크 에러가 크래시로 이어진다.
- 로딩 / 에러 / 빈 상태 UI 가 없다. Paging `loadState` 를 아무도 읽지 않는다.
- `RealContainer` 의 `MutableSharedFlow()` 는 버퍼가 0이라 구독자가 없으면 SideEffect 가 조용히 사라진다.
- `:domain` 이 Android library 이며 Hilt·KSP·Parcelize·Paging 을 물고 있다. Parcelize 와 Room 은 전 모듈 사용처가 0건이다.
- 상태바 색을 `CherryPokemonTheme` 과 `PokemonDetailScreen` 두 곳에서 서로 다르게 설정한다.
- 릴리즈 빌드에 `isMinifyEnabled = false`. `PokemonDetailResponse` 는 `@SerializedName` 없이 snake_case 필드명에 의존하므로 R8 을 켜면 파싱이 깨진다.
