# CherryPokemon 개선 계획

- 작성일: 2026-09-17
- 기준 커밋: `1106062` (`main`)
- 목표: [`docs/conventions/`](conventions/README.md)
- 이 문서는 지금 코드를 목표 상태로 **옮기는 순서**만 다룬다. 규칙과 근거는 컨벤션 문서에 있고, 여기서는 링크만 한다.

> 버전은 2026-09-17에 Maven 저장소에서 확인한 최신 안정 버전이다. PR을 만들 때 다시 확인한다.
> 코드 예시는 넣지 않았다. 각 PR에서 컨벤션의 예시를 기준으로 쓰고, 로컬 빌드와 실기기로 확인한다.

---

## 옮기는 방식

### 원칙

| 원칙 | 이유 |
|---|---|
| **점진 전환.** PR마다 `assembleDebug`가 되고 앱이 실행된다 | PR이 작아서 리뷰와 되돌리기가 쉽다 |
| **옛 코드의 버그는 고치지 않는다.** 옛 코드는 옮기기 전까지 빌드가 깨질 때만 손댄다 | 버그가 있는 코드(`PokemonPagingSource`, 옛 ViewModel, Activity)는 전부 새 구조로 바뀌며 지워진다. 고친 코드가 곧 사라진다 |
| **새 코드는 컨벤션을 그대로 따른다.** 과도기라는 이유로 규칙을 낮추지 않는다 | [컨벤션 README](conventions/README.md) |
| 과도기 코드(옛 코드와 새 코드를 잇는 임시 연결)는 이 문서에 적힌 것만 허용하고, 적힌 단계에서 지운다 | 임시 코드가 남지 않게 |
| 툴체인 변경은 다른 변경과 섞지 않는다 | 빌드가 깨질 가능성이 가장 큰 단계다 |
| 테스트는 그 코드를 만드는 PR에 같이 넣는다 | [testing.md 8](conventions/testing.md#8-실행-시점-필수) |
| 구조를 바꾼 PR에서 `CLAUDE.md`의 해당 설명을 같이 고친다 | `CLAUDE.md`는 지금 코드를 설명한다 |

### 순서

```
Phase 1  빌드 기반        buildSrc → build-logic, 안 쓰는 의존성 삭제
Phase 2  품질 게이트      Spotless, Lint, GitHub Actions
Phase 3  툴체인           Gradle 9, AGP 9, Kotlin 2.4, compileSdk 36
Phase 4  데이터 모듈      :core:model, :core:common, :core:network, :core:database, :core:data, :core:testing
Phase 5  UI 기반 모듈     :core:designsystem, :core:ui, :core:navigation
Phase 6  화면 전환        단일 Activity + Nav3 + Orbit, 옛 모듈 삭제
Phase 7  마무리           R8, 적응형 레이아웃, Baseline Profile, 스크린샷 테스트
```

- Phase 1이 툴체인보다 먼저인 이유: 지금 `buildSrc`는 `com.android.build.gradle.BaseExtension`을 쓰는데, AGP 9는 새 DSL이 기본이라 이 옛 타입은 opt-out(`android.newDsl=false`) 없이는 쓸 수 없고 AGP 10에서 opt-out도 사라진다([AGP 9.0 release notes](https://developer.android.com/build/releases/agp-9-0-0-release-notes)). 먼저 새 DSL 타입으로 convention plugin을 만들어 두면 Phase 3의 빌드 설정 변경이 `build-logic` 한 곳에 모인다.
- Phase 2가 툴체인보다 먼저인 이유: Phase 3 PR부터 CI가 빌드·테스트·Lint를 확인한다.
- Phase 4, 5의 새 모듈은 Phase 6에서 화면이 옮겨지기 전까지 앱이 쓰지 않는다. 테스트로만 확인한다.

---

## 지금과 목표의 차이

| 영역 | 지금 | 목표 | 단계 |
|---|---|---|---|
| 빌드 로직 | `buildSrc` 확장 함수, 모듈마다 `compileOptions` 복사 | `build-logic` 클래스 convention plugin | 1-1 |
| 플러그인 적용 | `id(libs.plugins.x.get().pluginId)` | 카탈로그 플러그인은 `alias`, convention plugin은 `id` | 1-1 |
| 포맷·Lint·CI | 없음 | Spotless + ktlint, Lint, GitHub Actions | 2 |
| Gradle / AGP / Kotlin | 8.7 / 8.5.2 / 2.0.0 | 9.6 이상 / 9.4 / 2.4 (AGP 내장 Kotlin) | 3-1 |
| compileSdk / targetSdk | 34 / 34 | 36 / 최신 | 3-1 / 6-4 |
| 모듈 | `:app`, `:data`, `:domain` | `:feature:*:api/impl`, `:core:*` | 4, 5, 6 |
| 데이터 | 네트워크 직접, `PagingSource` | 오프라인 우선, Room 3 + `RemoteMediator` | 4 |
| 직렬화 | Gson(리플렉션, snake_case 필드명) | kotlinx.serialization | 4-3 |
| 도메인 레이어 | 전달만 하는 UseCase 2개 | UseCase 없음 (필요해질 때 `:core:domain`) | 6-3 |
| 화면 구조 | Activity 2개 + Intent extras | 단일 Activity + Navigation 3 | 6 |
| 상태 관리 | 직접 만든 MVI(`ui/view/base/base/`) | Orbit MVI 12 | 6 |
| 대표 색상 | Glide `submit().get()`으로 이미지를 다시 받음, 이전 화면이 색을 인자로 넘김 | Landscapist palette 플러그인, 각 화면이 직접 추출 | 6-1, 6-2 |
| 릴리즈 빌드 | `isMinifyEnabled = false` | R8 최적화 | 7-1 |
| 테스트 | 예제 테스트만 | 레이어별 테스트, 계측 테스트 CI | 4~7 |

---

## 옛 코드의 결함이 사라지는 곳

2026-09-16에 코드로 확인한 결함이다. `c4a3cf5` 이후 앱 코드는 바뀌지 않았다. 옛 코드에서는 고치지 않고, 아래 단계에서 새 코드로 대체하면서 **같은 경로의 회귀 테스트**를 넣는다.

| 결함 | 사라지는 곳 | 회귀 테스트 |
|---|---|---|
| `PokemonPagingSource.load()`의 API 호출이 `try` 밖이라 오프라인에서 크래시 | 4-5 `RemoteMediator` | 네트워크 예외 → `MediatorResult.Error` |
| 목록에 로딩·에러·빈 상태 UI가 없음 | 6-1 | `loadState` 에러일 때 재시도 표시 |
| `getRefreshKey()`가 `null`이라 새로고침하면 스크롤 위치를 잃음 | 4-5 Room이 만든 `PagingSource` | — |
| Lazy 목록에 `key`가 없어 색이 다른 카드에 붙음 | 6-1 `itemKey { it.id }` | — |
| 상세 API 실패 시 크래시 | 6-2 Orbit ViewModel | 갱신 실패, 캐시 없음 → `RefreshState.Failed` |
| 배경색 폴백 `Color.White.value.toInt()` 오류, Palette 폴백 alpha 0 | 6-2 (색을 인자로 넘기지 않음) | — |
| `Pokemon.id`가 `toInt()`로 `NumberFormatException`, 접근할 때마다 URL 파싱 | 4-5 데이터 레이어가 ID를 계산해 필드로 저장 | 비정상 URL 처리 |
| `Pokemon.imageUrl`이 도메인 모델에 이미지 서버 주소를 하드코딩 | 4-5 | — |
| Gson이 논-널 필드에 null을 넣을 수 있음, R8을 켜면 필드명이 깨짐 | 4-3 kotlinx.serialization | 실제 응답 JSON, 손상된 JSON 파싱 |
| `NamedResource` 중복 정의, 쓰지 않는 `PokemonSpeciesResponse` | 4-3 (`{ name, url }` 모델 하나, species는 옮기지 않음) | — |
| SideEffect 버퍼가 0이라 구독자가 없으면 유실 | 6-3 base 패키지 삭제 (Orbit은 버퍼 있음) | — |
| `fetchDominantColor`가 IO 스레드를 막고 이미지를 두 번 받음, `Dispatchers.IO` 하드코딩 | 6-1, 6-2 palette 플러그인 | — |
| 색상 `ImmutableMap`이 State에 있어 하나만 바뀌어도 모든 카드가 리컴포즈 | 6-1 (색은 카드가 palette로 직접 추출) | — |
| `Window`를 Composable에 주입, 테마와 상세 화면이 상태바 색을 서로 덮어씀 | 6-1, 6-2 edge-to-edge | — |
| `:app`이 `:data`의 `com.cherryzp.data.extend.default`를 import | 6-3 `:data` 삭제 | — |
| Room·Parcelize·Material2·appcompat 의존성을 쓰는 곳이 없음 | 1-2 | — |
| `contentDescription` 없음, 타입 이름 등 문자열 하드코딩 | 5-2, 6-1, 6-2 | Compose UI 테스트는 리소스 문자열로 찾음 |
| 패키지 이름(`ui/view/base/base`, camelCase `pokemonDetail`, `com.cherryzp.consts`) | 4~6 새 모듈은 `com.cherryzp.cherrypokemon.<모듈 경로>` | — |

---

## Phase 1 — 빌드 기반

버전은 올리지 않는다. 동작 변화 없음.

### 1-1. `build-logic` convention plugin (PR)

- `build-logic/convention` included build를 만들고 `settings.gradle.kts`의 `pluginManagement`에서 `includeBuild`한다.
- 지금 모듈에 필요한 플러그인만 만든다: `cherrypokemon.android.application`, `cherrypokemon.android.application.compose`, `cherrypokemon.android.library`, `cherrypokemon.hilt`. 나머지 플러그인은 해당 모듈을 만드는 PR에서 추가한다. ([build.md 1-2](conventions/build.md#1-2-플러그인-목록-필수))
- SDK·Java·Kotlin 설정은 `build-logic` 한 곳으로 모은다. `Versions.kt`, `BuildTaskObject.kt`의 값을 옮긴다.
- AGP 옛 타입(`BaseExtension`) 대신 `ApplicationExtension` / `LibraryExtension` / `CommonExtension`을 쓴다.
- 모듈 build 파일은 플러그인, `namespace`, 의존성만 남긴다. 루트 `build.gradle.kts`의 `buildscript { classpath }`는 `plugins { alias(...) apply false }`로 바꾼다.
- `buildSrc` 삭제.

**확인:** `./gradlew assembleDebug`, 실기기 실행, `buildSrc`가 없는지.

### 1-2. 쓰지 않는 의존성 삭제 (PR)

- `:data`: `appcompat`, `material`, Room 전부(`annotationProcessor`와 `ksp` 중복 선언 포함)
- `:app`: `room-paging`, `androidx.compose.material`(Material2), Parcelize 플러그인
- `:domain`: Parcelize 플러그인, `core-ktx`
- 카탈로그에서 쓰는 곳이 없어진 항목(`javapoet` 등)

**확인:** `assembleDebug`, 실기기 실행.

### 1-3. Gradle 설정 (PR)

[build.md 4](conventions/build.md#4-gradle-설정-권장): `org.gradle.parallel`, `org.gradle.caching`, `android.enableJetifier=false`, 저장소 순서. `org.gradle.configuration-cache`는 3-2에서 켠다.

---

## Phase 2 — 품질 게이트

### 2-1. Spotless + ktlint (PR)

- `build-logic`에서 모든 모듈에 적용한다. ([build.md 5](conventions/build.md#5-포맷-검사-필수))
- 옛 코드에 `spotlessApply`를 한 번 돌린다. 설정 추가와 포맷 적용은 **커밋을 나눈다** (포맷 커밋은 `style`).

### 2-2. Lint (PR)

- `cherrypokemon.android.lint`를 만들고 `checkDependencies = true`.
- 지금 있는 이슈는 baseline에 넣는다. 옛 코드를 지우는 PR에서 baseline도 같이 줄인다. ([build.md 6](conventions/build.md#6-lint-필수))

### 2-3. GitHub Actions (PR)

- PR마다 `spotlessCheck`, `testDebugUnitTest`, `lintRelease`, `assembleDebug`, `assembleRelease`. ([build.md 8](conventions/build.md#8-ci-필수))
- 계측 테스트(Gradle Managed Device)는 계측 테스트가 생기는 4-4에서 추가한다.
- GitHub 브랜치 보호에서 CI 통과를 머지 조건으로 설정한다. (저장소 설정이라 사용자가 직접 한다)

---

## Phase 3 — 툴체인

### 3-1. Gradle 9 · AGP 9 · Kotlin 2.4 (PR, 다른 변경 섞지 않음)

| | 지금 | 올릴 버전 | 조건 |
|---|---|---|---|
| Gradle | 8.7 | 9.7.1 | AGP 9.4는 Gradle 9.6.0 이상 |
| AGP | 8.5.2 | 9.4.0 | JDK 17, Build Tools 36.0.0 |
| Kotlin(KGP) | 2.0.0 | 2.4.20 | AGP 9는 KGP 2.2.10 이상을 런타임 의존성으로 가짐. 더 높은 버전은 루트 build 파일에 선언 |
| KSP | 2.0.0-1.0.23 | 2.3.12 | |
| Hilt | 2.51.1 | 2.60.1 | |
| Compose BOM | 2024.10.01 | 2026.09.00 | |
| lifecycle | 2.8.7 | 2.11.0 | Orbit 12는 lifecycle 2.9.0, Kotlin 2.1.21 이상 |
| compileSdk | 34 | 36 | Navigation 3 요구. targetSdk는 6-4에서 올린다 |

- AGP 9 내장 Kotlin으로 옮긴다: `org.jetbrains.kotlin.android` 플러그인 삭제, `android.kotlinOptions {}` → `kotlin.compilerOptions {}`. ([Migrate to built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin))
- AGP 9에서 `CommonExtension`의 타입 파라미터가 없어졌다. 1-1의 convention plugin을 고친다.
- `android.newDsl=false`로 opt-out하지 않는다. Hilt·KSP Gradle 플러그인이 새 DSL을 지원하는 버전인지 먼저 확인하고, 지원하지 않으면 이 PR을 보류한다.
- Lint 버전이 바뀌어 새로 잡히는 기존 코드 이슈는 baseline을 다시 만든다.

**확인:** `./gradlew :app:kspDebugKotlin` → `assembleDebug` → 실기기 실행 → CI.

### 3-2. configuration cache (PR)

`org.gradle.configuration-cache=true`. 플러그인이 호환되지 않으면 원인을 PR 본문에 적고 이 PR을 보류한다.

---

## Phase 4 — 데이터 모듈

옛 `:data`, `:domain` 옆에 새로 만든다. 앱은 Phase 6까지 옛 모듈을 쓴다. 규칙은 [data.md](conventions/data.md), 테스트는 [testing.md](conventions/testing.md).

| PR | 모듈 | 내용 | 테스트 |
|---|---|---|---|
| 4-1 | `:core:model`, `:core:common` | `cherrypokemon.jvm.library`. `Pokemon(id, name, imageUrl)`, `PokemonDetail`, `PokemonType`. `@Dispatcher` qualifier, `DispatchersModule`, `@ApplicationScope` | — |
| 4-2 | `:core:testing` | `MainDispatcherRule`, 테스트 데이터. Fake Repository는 인터페이스가 생기는 4-5에서 추가 | — |
| 4-3 | `:core:network` | kotlinx.serialization(1.11.0), Retrofit(3.0.0), OkHttp(5.5.0). `PokemonNetworkDataSource` + `internal RetrofitPokemonNetwork`, `Network*` 모델, `NetworkModule`. 기본 주소 `BuildConfig`, 로그는 디버그만, network security config | 실제 응답 JSON 파싱, 손상된 JSON |
| 4-4 | `:core:database` | Room 3(3.0.3), `cherrypokemon.android.room`. `PokemonEntity`, `PokemonDetailEntity`, 원격 키 테이블, DAO, 스키마 내보내기 | DAO 계측 테스트. **여기서 CI에 Gradle Managed Device 추가** |
| 4-5 | `:core:data` | `PokemonRepository`, `OfflineFirstPokemonRepository`, `PokemonRemoteMediator`, `asEntity()`, `DataModule`. ID·이미지 주소는 여기서 계산. `FakePokemonRepository`를 `:core:testing`에 추가 | Repository 로컬 테스트, `RemoteMediator` 계측 테스트 |

- Paging은 3.5.1로 올린다(4-4 또는 4-5).
- 4-3에서 올리는 Retrofit·OkHttp 버전은 카탈로그를 같이 쓰는 옛 `:data`에도 적용된다. 옛 앱이 계속 동작하는지 실기기로 확인한다.
- 캐시만 있으므로 `fallbackToDestructiveMigration()`을 허용한다. 사용자 데이터 테이블이 생기면 금지. ([data.md 3-2](conventions/data.md#3-2-스키마와-마이그레이션-필수))

---

## Phase 5 — UI 기반 모듈

| PR | 모듈 | 내용 |
|---|---|---|
| 5-1 | `:core:designsystem` | `cherrypokemon.android.library.compose`. `CherryPokemonTheme`(라이트·다크, Material 3), 기본 컴포넌트. 상태바 색을 칠하는 코드는 옮기지 않는다. dynamic color 사용 여부는 이 PR에서 정한다(`선택`) |
| 5-2 | `:core:ui` | `PokemonType` → 색 매핑(지금 `PokemonTypeEnum`), `PokemonCard(name, number, imageUrl, onClick, modifier)`, Preview 데이터. 문자열 리소스, `contentDescription` |
| 5-3 | `:core:navigation` | `NavigationState`, `Navigator` (NiA `core/navigation` 기준). Navigation 3(1.1.7) |

- 5-1, 5-2는 Preview와 Compose UI 테스트로 확인한다. 앱은 아직 옛 테마를 쓴다.

---

## Phase 6 — 화면 전환

Orbit MVI(12.0.1), `lifecycle-viewmodel-navigation3`(2.11.0), `hilt-lifecycle-viewmodel-compose`(1.4.0), Landscapist(2.13.1)를 이 단계에서 들여온다. 규칙은 [architecture.md 3, 4](conventions/architecture.md#3-상태-관리)와 [compose.md](conventions/compose.md).

### 6-1. 단일 Activity와 목록 화면 (PR)

- `:feature:pokemonlist:api`(`PokemonListNavKey`), `:feature:pokemonlist:impl`(Screen, ViewModel, entry builder).
- `:app`에 새 `MainActivity`(`enableEdgeToEdge()`, `adjustResize`)와 `CherryPokemonApp`(`NavDisplay`, `SnackbarHostState`)을 만든다. 런처를 새 `MainActivity`로 바꾸고 옛 `ui/view/main/`을 삭제한다.
- 목록은 `:core:data`의 `getPokemonsStream()`을 쓴다. `loadState.source` / `mediator`로 로딩·에러·재시도, `itemKey`, placeholder.
- 카드 대표 색은 Landscapist palette 플러그인으로 카드 안에서 추출한다.
- **과도기 코드 (6-2에서 삭제):** `CherryPokemonApp`에서 카드 클릭 시 옛 `PokemonDetailActivity`를 `Intent`로 띄운다. 배경색 인자는 `Color.White.toArgb()`로 고정한다.
- 테스트: ViewModel(`asSnapshot`), 상태 없는 Screen UI 테스트.

### 6-2. 상세 화면 (PR)

- `:feature:pokemondetail:api`(`PokemonDetailNavKey(pokeId)`, `navigateToPokemonDetail`), `:feature:pokemondetail:impl`.
- ViewModel은 assisted injection으로 `pokeId`를 받고, `getPokemonDetailStream` 관찰 + `refreshPokemonDetail`. 배경색은 상세 화면이 이미지에서 직접 추출한다.
- 6-1의 과도기 코드와 옛 `PokemonDetailActivity`, `ui/view/pokemonDetail/`, `extend/ColorExtend.kt` 삭제.
- 테스트: ViewModel(`orbit-test`: 캐시 있음·없음 × 갱신 성공·실패, 재시도), 상태 없는 Screen UI 테스트.

### 6-3. 옛 구조 삭제 (PR)

- 옛 `:data`, `:domain` 모듈, `ui/view/base/base/`, `enums/`, 옛 `ui/theme/`, `KeyConsts` 삭제. 옛 UseCase는 옮기지 않는다. ([data.md 6-1](conventions/data.md#6-1-필요할-때만-만든다-필수))
- 카탈로그에서 쓰는 곳이 없어진 항목(Gson 컨버터, `palette-ktx`, `hilt-navigation-compose` 등) 삭제.
- Lint baseline에서 사라진 이슈 정리.
- `CLAUDE.md`의 모듈 구조, MVI, 화면 간 데이터 전달, 데이터 흐름, 알려진 부채 섹션을 새 구조 설명으로 다시 쓴다.

### 6-4. targetSdk와 뒤로가기 (PR)

- targetSdk를 최신으로 올린다. edge-to-edge 강제(API 35+)는 6-1, 6-2에서 이미 처리했다.
- 예측형 뒤로가기 동작 확인. ([compose.md 1-4](conventions/compose.md#1-4-뒤로가기-필수))

### 6-5. 내비게이션 테스트 (PR)

- `:core:data-test`(`TestDataModule`), `CherryPokemonTestRunner`.
- `:app` 계측 테스트: 시작 화면, 목록 → 상세 → 뒤로가기. ([testing.md 6-2](conventions/testing.md#6-2-내비게이션-테스트-필수))

---

## Phase 7 — 마무리

| PR | 내용 | 규칙 |
|---|---|---|
| 7-1 | release 빌드 R8 최적화(AGP 9.3+ `optimization { enable = true }`), keep 규칙 최소화, release 빌드 실기기 확인 | [build.md 7](conventions/build.md#7-릴리즈-빌드-필수) |
| 7-2 | 적응형 레이아웃: 창 크기에 따라 목록-상세를 나란히 (`adaptive-navigation3`의 `ListDetailSceneStrategy`) | [compose.md 9](conventions/compose.md#9-적응형-레이아웃-필수) |
| 7-3 | Baseline Profile | [compose.md 7-3](conventions/compose.md#7-3-앱-설정-권장) (`권장`) |
| 7-4 | 스크린샷 테스트 | [testing.md 6-3](conventions/testing.md#6-3-스크린샷-테스트-권장) (`권장`) |

---

## 이 계획이 끝나면

- 이 문서의 모든 PR이 머지되면 문서를 삭제하거나 완료로 표시한다.
- `CLAUDE.md`는 지금 코드 설명이 컨벤션과 같아지므로, 컨벤션과 겹치는 설명을 줄이고 명령어와 모듈 안내만 남긴다.
