# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

PokeAPI 기반 포켓몬 도감 앱. Jetpack Compose + Orbit MVI + 오프라인 우선 멀티모듈 구성의 학습용 프로젝트.

## 컨벤션

코드를 쓰거나 고치기 전에 [`docs/conventions/`](docs/conventions/README.md)에서 해당 영역 문서를 읽는다.

| 작업 | 문서 |
|---|---|
| 모듈·레이어, ViewModel·UiState, 내비게이션 | `architecture.md` |
| 이름, 포맷, 코루틴 | `kotlin.md` |
| Composable | `compose.md` |
| Repository, UseCase, DTO, DI | `data.md` |
| 테스트 | `testing.md` |
| Gradle, 모듈 build 파일, CI | `build.md` |

남은 작업 순서는 `docs/improvement-plan.md`에 있다.

## 명령어

```bash
./gradlew assembleDebug              # 디버그 APK 빌드
./gradlew installDebug               # 연결된 기기에 설치
./gradlew clean

# Hilt / KSP 에러만 빠르게 확인 (전체 빌드보다 훨씬 빠름)
./gradlew :app:kspDebugKotlin

# 단위 테스트
./gradlew testDebugUnitTest                      # 전 모듈
./gradlew :core:data:testDebugUnitTest           # 특정 모듈
./gradlew :core:data:testDebugUnitTest --tests "*.OfflineFirstPokemonRepositoryTest"

# 계측 테스트 (기기/에뮬레이터 필요)
./gradlew connectedDebugAndroidTest
# CI 와 같은 가상 기기로 실행
./gradlew pixel5Api35Check
```

포맷 검사와 Lint 는 모든 모듈에 적용된다.

```bash
./gradlew spotlessCheck   # 포맷 검사 (ktlint, Android 스타일)
./gradlew spotlessApply   # 포맷 자동 수정
./gradlew lintRelease     # Lint (기존 경고는 각 모듈 lint-baseline.xml 로 제외)
```

CI 는 `.github/workflows/ci.yml` 이다. PR 마다 포맷·단위 테스트·Lint·빌드와 계측 테스트를 돌린다.

## 모듈 구조

```
:app                          MainActivity, CherryPokemonApp (NavDisplay 조립)
:feature:<화면>:api            NavKey, 이동 함수
:feature:<화면>:impl           Screen, ViewModel, UiState, entry builder
:core:navigation              NavigationState, Navigator
:core:designsystem            테마, 색, 타이포그래피
:core:ui                      PokemonCard, 타입 색, Preview 데이터
:core:data                    Repository, RemoteMediator, 네트워크 모델 → 엔티티 변환
:core:database                Room 3 데이터베이스, 엔티티, DAO
:core:network                 네트워크 데이터 소스, 네트워크 모델
:core:model                   도메인 모델 (JVM)
:core:common                  디스패처 qualifier, 애플리케이션 스코프 (JVM)
:core:testing                 테스트 규칙, 테스트 데이터, Fake Repository
build-logic                   공유 빌드 로직 (included build 의 convention plugin)
```

의존 방향은 UI → data 한쪽이다. `:feature:*:impl` 은 다른 feature 의 `impl` 에 의존하지 않고, 이동이 필요하면 그 화면의 `api` 를 쓴다.

## 화면 구조

단일 Activity 다. 화면은 Activity 가 아니라 NavKey 와 entry 로 추가한다.

| 파일 | 내용 |
|---|---|
| `:api` 의 `<화면>NavKey.kt` | `@Serializable` NavKey 와 `Navigator.navigateTo<화면>()`. 키에는 ID 같은 원시값만 담는다 |
| `:impl` 의 `<화면>Screen.kt` | ViewModel 을 받는 Screen 과 상태만 받는 Screen, 같은 이름으로 오버로드 |
| `:impl` 의 `<화면>ViewModel.kt` | `OrbitContainerHost` 구현 |
| `:impl` 의 `<화면>UiState.kt` | UiState 와 SideEffect |
| `:impl` 의 `navigation/<화면>EntryBuilder.kt` | `EntryProviderScope<NavKey>` 확장 함수 |

새 화면을 추가하면 `:app` 의 `CherryPokemonApp` 에 entry builder 를 등록하고 `settings.gradle.kts` 에 모듈을 넣는다. **`settings.gradle.kts` 등록을 빠뜨리면 그 모듈은 빌드도 테스트도 되지 않는데 전체 빌드는 통과한다.**

## 상태 관리 (Orbit MVI)

```kotlin
@HiltViewModel(assistedFactory = PokemonDetailViewModel.Factory::class)
internal class PokemonDetailViewModel @AssistedInject constructor(
    @Assisted private val pokeId: Int,
    private val pokemonRepository: PokemonRepository,
) : ViewModel(),
    OrbitContainerHost<PokemonDetailUiState, PokemonDetailUiState, PokemonDetailSideEffect> {

    override val container = orbitContainer<PokemonDetailUiState, PokemonDetailSideEffect>(
        initialState = PokemonDetailUiState(refreshState = RefreshState.Refreshing),
    ) { /* onCreate: 스트림 구독과 첫 갱신 */ }
}
```

- 로딩과 구독은 `init` 이 아니라 `orbitContainer` 의 `onCreate` 블록에서 시작한다. 테스트에서는 `runOnCreate()` 로 실행한다.
- 화면 인자는 NavKey 값을 assisted injection 으로 받는다.
- 상태는 `collectAsState()`, 일회성 이벤트는 `collectSideEffect {}` 로 받는다.
- UI 에서 시작하는 이동(버튼 클릭)은 ViewModel 을 거치지 않고 Screen 콜백으로 처리한다.

## 데이터 흐름

오프라인 우선이다. **읽기는 항상 Room 에서** 하고, 네트워크는 로컬을 갱신하는 데만 쓴다.

```
PokemonNetworkDataSource (Retrofit + kotlinx.serialization)
  └─ OfflineFirstPokemonRepository ─ asEntity() ─▶ PokemonDao (Room)
       ├─ 목록: Pager(PokemonRemoteMediator, PokemonDao::pagingSource)
       └─ 상세: getPokemonDetailStream() ─ asExternalModel() ─▶ Flow<PokemonDetail?>
            └─ ViewModel ──▶ UiState ──▶ Compose
```

- 네트워크 모델(`Network*`)과 엔티티(`*Entity`)는 데이터 레이어 밖으로 나가지 않는다. 밖에는 `:core:model` 의 모델을 노출한다.
- 단위 변환(PokeAPI 의 데시미터·헥토그램 → m·kg)과 ID·이미지 주소 계산은 `:core:data` 의 매퍼가 한다. 모델은 계산하지 않는다.
- 데이터베이스 클래스는 모듈 밖으로 내보내지 않는다. 여러 DAO 를 한 트랜잭션으로 묶을 때는 `DatabaseTransactionRunner` 를 주입받는다.
- 네트워크 실패는 예외로 던지고, 상태를 만드는 쪽(ViewModel)이 잡아 상태나 SideEffect 로 바꾼다.

## 빌드 설정

| 위치 | 담당 |
|---|---|
| `gradle/libs.versions.toml` | 라이브러리·플러그인 버전 |
| `build-logic/convention/.../KotlinAndroid.kt` | `COMPILE_SDK`, `MIN_SDK`, `TARGET_SDK`, Java·Kotlin JVM 타깃 |
| `build-logic/convention/src/main/kotlin/*ConventionPlugin.kt` | 모듈 종류별 공통 설정 |

| convention plugin | 적용 모듈 |
|---|---|
| `cherrypokemon.android.application`, `.application.compose` | `:app` |
| `cherrypokemon.android.library`, `.library.compose` | Android 라이브러리 |
| `cherrypokemon.android.feature.api`, `.feature.impl` | `:feature:*` |
| `cherrypokemon.jvm.library` | `:core:model`, `:core:common` |
| `cherrypokemon.android.room` | `:core:database` |
| `cherrypokemon.hilt`, `cherrypokemon.android.lint` | 해당 모듈 |

- 모듈 `build.gradle.kts` 에는 플러그인, `namespace`, 그 모듈에만 필요한 설정과 의존성만 둔다.
- convention plugin 은 `id("cherrypokemon.…")`, 카탈로그 플러그인은 `alias(libs.plugins.…)` 로 적용한다.
- Spotless 는 루트에서만 설정한다. 모듈마다 적용하면 그 모듈의 플러그인 클래스패스가 ktlint 룰 초기화를 깨뜨린다.

## 남은 작업

`docs/improvement-plan.md` 의 6-4 이후다.

- targetSdk 를 최신으로 올리기 (지금 34)
- 내비게이션 계측 테스트 (`:core:data-test`, `CherryPokemonTestRunner`)
- release 빌드 R8 켜기 (지금 `isMinifyEnabled = false`)
- 적응형 레이아웃, Baseline Profile, 스크린샷 테스트
