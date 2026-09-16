# CherryPokemon 컨벤션

이 프로젝트의 **목표 상태**를 정의한다. 지금 코드가 어떻게 되어 있는지와 관계없이 Android 공식 문서와 Google의 참고 앱 [Now in Android](https://github.com/android/nowinandroid)(이하 NiA)를 기준으로 삼는다. 지금 코드에 맞추려고 규칙을 낮추지 않는다.

- 기준일: 2026-09-16 (공식 문서 원문과 NiA `main` 브랜치를 이 날짜에 확인했다)
- 규칙마다 근거를 단다. 근거는 실제로 읽고 확인한 문서만 링크한다.

## 문서

| 문서 | 다루는 것 |
|---|---|
| [architecture.md](architecture.md) | 모듈 구성과 의존 방향, 레이어, 상태 관리, 단일 Activity와 Navigation 3 |
| [kotlin.md](kotlin.md) | 이름, 패키지, 파일 구성, 포맷, KDoc, 관용 표현, 코루틴과 Flow |
| [compose.md](compose.md) | 화면 구성, 상태 호이스팅, 파라미터, 디자인 시스템, 목록, 성능, 부수 효과, 적응형, 접근성, Preview |
| [data.md](data.md) | 오프라인 우선 데이터 레이어, 네트워크, 데이터베이스, Paging, 모델, UseCase, 에러, DI |
| [testing.md](testing.md) | 테스트 대상, 테스트 대역, 레이어별 테스트 방법, 이름 |
| [build.md](build.md) | convention plugin, 버전 카탈로그, SDK 버전, 포맷 검사, Lint, R8, CI |

## 기준의 우선순위

1. **Android 공식 문서** (developer.android.com, AndroidX 가이드라인, Gradle 공식 문서)
2. **NiA** — 공식 문서가 구체적으로 정하지 않은 모듈 구성, 이름, 빌드 설정
3. **이 프로젝트가 고른 서드파티 라이브러리의 공식 문서** — 아래 "라이브러리 예외"에 해당하는 영역만

- 공식 문서와 NiA가 다르면 공식 문서를 따른다.
- 공식 문서끼리 다르면 더 구체적인 문서를 따른다. (예: 테스트 함수 이름은 Kotlin 코딩 컨벤션보다 Android Kotlin 스타일 가이드를 따른다)
- 공식 문서 안에서 상충하는 곳은 [결정 사항](#결정-사항)에 판단과 이유를 적는다.

## 규칙의 강도

| 표시 | 뜻 | 정하는 기준 (위에서부터 적용) |
|---|---|---|
| `필수` | 어기면 리뷰에서 수정을 요청한다 | ① [Architecture recommendations](https://developer.android.com/topic/architecture/recommendations)의 Strongly recommended ② AndroidX 가이드라인의 MUST ③ 공식 문서가 명령형(don't, never, must, should)으로 적은 규칙 ④ NiA가 모든 모듈에서 일관되게 지키는 규칙 |
| `권장` | 따르지 않으려면 PR 본문의 참고에 이유를 적는다 | ① Recommended ② SHOULD(앱 대상) ③ 공식 문서가 prefer, consider, recommend로 적은 규칙 ④ NiA에서 일부만 지키는 규칙 |
| `선택` | 상황에 맞게 고른다. 한 번 고르면 같은 모듈 안에서는 통일한다 | ① Optional ② MAY |

## 라이브러리 예외

프로젝트가 **직접 고른 서드파티 라이브러리**는 공식 가이드가 다른 방식을 권하더라도 쓸 수 있다. 그 라이브러리가 맡은 영역에서는 공식 가이드 대신 **라이브러리의 공식 문서와 기능 전체**를 따른다. 그 외 영역은 이 컨벤션을 그대로 따른다.

| 라이브러리 | 맡은 영역 | 대신하는 공식 방식 | 따르는 문서 |
|---|---|---|---|
| [Orbit MVI](https://orbit-mvi.org/) | ViewModel의 상태·SideEffect 관리 | `StateFlow` + `stateIn`, "ViewModel에서 UI로 이벤트를 보내지 않는다"(Strongly recommended) | [Orbit 문서](https://orbit-mvi.org/) |
| [Landscapist](https://github.com/skydoves/landscapist) (Glide) | 이미지 로딩, 이미지에서 대표 색상 추출 | Coil (NiA) | [Landscapist 문서](https://skydoves.github.io/landscapist/) |

- 예외에 라이브러리를 추가하거나 빼려면 이 표를 고치는 PR을 따로 올린다.
- 표에 없는 라이브러리를 새로 들여올 때, 공식 문서나 NiA가 같은 역할에 쓰는 라이브러리가 있으면 그것을 쓴다. (예: Hilt, Room, kotlinx.serialization, Retrofit + OkHttp, Turbine)

## 결정 사항

공식 문서가 여러 선택지를 허용하거나 서로 상충해서 이 프로젝트가 하나로 정한 것이다.

| 주제 | 결정 | 근거와 이유 |
|---|---|---|
| 화면 구조 | 단일 Activity + Navigation 3 | [Recommendations](https://developer.android.com/topic/architecture/recommendations#ui-layer) Strongly recommended |
| 데이터 | 오프라인 우선. Room이 기준 데이터(source of truth), 네트워크는 로컬을 갱신 | [Data layer](https://developer.android.com/topic/architecture/data-layer): "In order to provide offline-first support, a local data source—such as a database—is the recommended source of truth." |
| 동기화 | pull 방식 (화면에 필요할 때 받아온다) | [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first): push 방식은 서버의 변경 알림이 필요한데 PokeAPI는 제공하지 않는다 |
| 모듈 | NiA 구성 (`:feature:*:api/impl`, `:core:*`) | [Modularization](https://developer.android.com/topic/modularization)은 "작은 프로젝트는 data 레이어를 모듈 하나에 둬도 된다", "너무 잘게 나누면 오버헤드"라고 규모에 따라 판단하라고 한다. 이 프로젝트는 학습 목적상 NiA와 같은 목표 구조로 정했다 |
| 도메인 레이어 | UseCase는 필요할 때만 만든다 | [Domain layer](https://developer.android.com/topic/architecture/domain-layer): "You should only use it when needed" |
| `Pager` 위치 | Repository(`:core:data`)에서 만들고 `Flow<PagingData<도메인 모델>>`을 노출 | [Paging overview](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)는 `Pager`를 ViewModel 레이어에 두지만, 그러면 ViewModel이 DAO·네트워크 데이터 소스에 의존해 Strongly recommended인 "UI 레이어는 데이터 소스와 직접 상호작용하지 않는다"와 [Data layer](https://developer.android.com/topic/architecture/data-layer)의 "State holder ... should never have a data source as a direct dependency"를 어긴다. 상위 규칙을 따른다 |
| 에러 처리 | 데이터 레이어는 예외를 던지고, 상태를 만드는 쪽이 잡는다 | [Data layer: Expose errors](https://developer.android.com/topic/architecture/data-layer) — `Result` 래퍼는 대안으로 소개됨 |
| 테스트 함수 이름 | 공백·백틱 없이 밑줄로 구분 | [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide#naming): "Function names should not contain spaces ... (notably, this is not fully supported in Android)" |

## 다른 문서와의 관계

| 문서 | 성격 |
|---|---|
| `docs/conventions/` | 목표 상태의 규칙 |
| `CLAUDE.md` | 지금 코드의 구조 설명 |
| `docs/improvement-plan.md` | 지금 코드를 목표 상태로 옮기는 순서 |

컨벤션은 지금 코드와의 차이나 옮기는 순서를 다루지 않는다. 그것은 개선 계획의 몫이다.

## 확인한 공식 문서

아래는 원문을 읽고 규칙에 반영한 문서다.

- **앱 아키텍처:** [Guide to app architecture](https://developer.android.com/topic/architecture) · [Recommendations](https://developer.android.com/topic/architecture/recommendations) · [UI layer](https://developer.android.com/topic/architecture/ui-layer) · [UI events](https://developer.android.com/topic/architecture/ui-layer/events) · [State holders](https://developer.android.com/topic/architecture/ui-layer/stateholders) · [State production](https://developer.android.com/topic/architecture/ui-layer/state-production) · [Domain layer](https://developer.android.com/topic/architecture/domain-layer) · [Data layer](https://developer.android.com/topic/architecture/data-layer) · [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first)
- **Lifecycle·ViewModel:** [Lifecycle in Compose](https://developer.android.com/topic/libraries/architecture/lifecycle) · [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) · [ViewModel scoping](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-apis) · [SavedStateHandle](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate) · [Save UI states](https://developer.android.com/topic/libraries/architecture/saving-states) · [Coroutines with lifecycle](https://developer.android.com/topic/libraries/architecture/coroutines) · [Configuration changes](https://developer.android.com/guide/topics/resources/runtime-changes)
- **Paging:** [Overview](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) · [Paged data](https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data) · [Network and database](https://developer.android.com/topic/libraries/architecture/paging/v3-network-db) · [Transform](https://developer.android.com/topic/libraries/architecture/paging/v3-transform) · [Load state](https://developer.android.com/topic/libraries/architecture/paging/load-state) · [Test](https://developer.android.com/topic/libraries/architecture/paging/test)
- **모듈화:** [Modularization](https://developer.android.com/topic/modularization) · [Patterns](https://developer.android.com/topic/modularization/patterns)
- **내비게이션:** [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) · [Get started](https://developer.android.com/guide/navigation/navigation-3/get-started) · [Basics](https://developer.android.com/guide/navigation/navigation-3/basics) · [Save state](https://developer.android.com/guide/navigation/navigation-3/save-state) · [Modularize](https://developer.android.com/guide/navigation/navigation-3/modularize) · [Entry decorators](https://developer.android.com/guide/navigation/navigation-3/naventrydecorators) · [Return results](https://developer.android.com/guide/navigation/navigation-3/return-results) · [Principles](https://developer.android.com/guide/navigation/principles) · [Predictive back](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture)
- **DI:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) · [Hilt multi-module](https://developer.android.com/training/dependency-injection/hilt-multi-module) · [Hilt with Jetpack](https://developer.android.com/training/dependency-injection/hilt-jetpack) · [Hilt testing](https://developer.android.com/training/dependency-injection/hilt-testing)
- **Kotlin:** [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide) · [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) · [Coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices) · [Testing coroutines](https://developer.android.com/kotlin/coroutines/test) · [Flow](https://developer.android.com/kotlin/flow) · [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) · [Testing flows](https://developer.android.com/kotlin/flow/test)
- **Compose:** [Compose API guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md) · [Architecture](https://developer.android.com/develop/ui/compose/architecture) · [State](https://developer.android.com/develop/ui/compose/state) · [State hoisting](https://developer.android.com/develop/ui/compose/state-hoisting) · [State saving](https://developer.android.com/develop/ui/compose/state-saving) · [Side effects](https://developer.android.com/develop/ui/compose/side-effects) · [Stability fix](https://developer.android.com/develop/ui/compose/performance/stability/fix) · [Strong skipping](https://developer.android.com/develop/ui/compose/performance/stability/strongskipping) · [Performance best practices](https://developer.android.com/develop/ui/compose/performance/bestpractices) · [Lists](https://developer.android.com/develop/ui/compose/lists) · [Modifiers](https://developer.android.com/develop/ui/compose/modifiers) · [Resources](https://developer.android.com/develop/ui/compose/resources) · [Material 3](https://developer.android.com/develop/ui/compose/designsystems/material3) · [Custom design systems](https://developer.android.com/develop/ui/compose/designsystems/custom) · [Edge-to-edge](https://developer.android.com/develop/ui/compose/system/setup-e2e) · [Adaptive](https://developer.android.com/develop/ui/compose/layouts/adaptive) · [Accessibility API defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults) · [Previews](https://developer.android.com/develop/ui/compose/tooling/previews) · [Testing](https://developer.android.com/develop/ui/compose/testing)
- **Room:** [Room](https://developer.android.com/training/data-storage/room) · [Async queries](https://developer.android.com/training/data-storage/room/async-queries) · [Relationships](https://developer.android.com/training/data-storage/room/relationships) · [Migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions) · [Testing](https://developer.android.com/training/data-storage/room/testing-db)
- **테스트:** [Fundamentals](https://developer.android.com/training/testing/fundamentals) · [What to test](https://developer.android.com/training/testing/fundamentals/what-to-test) · [Test doubles](https://developer.android.com/training/testing/fundamentals/test-doubles) · [Strategies](https://developer.android.com/training/testing/fundamentals/strategies) · [Local tests](https://developer.android.com/training/testing/local-tests) · [Instrumented tests](https://developer.android.com/training/testing/instrumented-tests)
- **빌드:** [Version catalogs](https://developer.android.com/build/migrate-to-catalogs) · [Dependencies](https://developer.android.com/build/dependencies) · [Build speed](https://developer.android.com/build/optimize-your-build) · [R8](https://developer.android.com/build/shrink-code) · [Lint](https://developer.android.com/studio/write/lint) · [Gradle Managed Devices](https://developer.android.com/studio/test/gradle-managed-devices) · [Gradle: sharing build logic](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html)
- **품질:** [Core app quality](https://developer.android.com/docs/quality-guidelines/core-app-quality)
