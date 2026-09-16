# CherryPokemon 컨벤션

이 프로젝트의 **목표 상태**를 정의한다. 지금 코드가 어떻게 되어 있는지와 관계없이, Android 공식 가이드와 Google의 참고 앱 [Now in Android](https://github.com/android/nowinandroid)(이하 NiA)를 기준으로 삼는다. 지금 코드에 맞추려고 규칙을 낮추지 않는다.

- 기준일: 2026-09-16
- 규칙마다 근거 링크를 단다.

## 문서

| 문서 | 다루는 것 |
|---|---|
| [architecture.md](architecture.md) | 모듈 구성과 의존 방향, 레이어, 상태 관리, 단일 Activity와 Navigation 3 |
| [kotlin.md](kotlin.md) | 이름, 패키지, 파일 구성, 포맷, KDoc, 코루틴과 Flow |
| [compose.md](compose.md) | 화면 구성, 상태 호이스팅, 파라미터, 디자인 시스템, 안정성, 접근성, Preview |
| [data.md](data.md) | 오프라인 우선 데이터 레이어, 네트워크, 데이터베이스, Paging, 모델, UseCase, DI |
| [testing.md](testing.md) | 테스트 대상, 테스트 대역, 모듈별 테스트 방법 |
| [build.md](build.md) | convention plugin, 버전 카탈로그, 포맷 검사, Lint, 릴리즈 빌드 |

## 기준의 우선순위

1. **Android 공식 문서** (developer.android.com, AndroidX 가이드라인)
2. **NiA** — 공식 문서가 구체적으로 정하지 않은 모듈 구성, 이름, 빌드 설정
3. **이 프로젝트가 고른 서드파티 라이브러리의 공식 문서** — 아래 "라이브러리 예외"에 해당하는 영역만

공식 문서와 NiA가 다르면 공식 문서를 따른다. (예: 공식 이름 규칙은 스트림 함수에 `Stream` 접미사를 붙이지만 NiA는 붙이지 않는다 → 붙인다)

## 규칙의 강도

공식 [Architecture recommendations](https://developer.android.com/topic/architecture/recommendations)의 등급을 그대로 옮겼다. 공식 등급이 없는 규칙은 NiA가 일관되게 지키면 `필수`, 상황에 따라 다르게 하면 `권장`으로 둔다.

| 표시 | 공식 등급 | 의미 |
|---|---|---|
| `필수` | Strongly recommended | 어기면 리뷰에서 수정을 요청한다 |
| `권장` | Recommended | 따르지 않으려면 PR 본문의 참고에 이유를 적는다 |
| `선택` | Optional | 상황에 맞게 고른다. 한 번 고르면 같은 모듈 안에서는 통일한다 |

## 라이브러리 예외

프로젝트가 **직접 고른 서드파티 라이브러리**는 공식 가이드가 다른 방식을 권하더라도 쓸 수 있다. 그 라이브러리가 맡은 영역에서는 공식 가이드 대신 **라이브러리의 공식 문서와 기능 전체**를 따른다. 그 외 영역은 이 컨벤션을 그대로 따른다.

| 라이브러리 | 맡은 영역 | 대신하는 공식 방식 | 따르는 문서 |
|---|---|---|---|
| [Orbit MVI](https://orbit-mvi.org/) | ViewModel의 상태·SideEffect 관리 | `StateFlow` + `stateIn`, ViewModel에서 UI로 이벤트를 보내지 않기 | [Orbit 문서](https://orbit-mvi.org/) |
| [Landscapist](https://github.com/skydoves/landscapist) (Glide) | 이미지 로딩, 이미지에서 대표 색상 추출 | Coil (NiA) | [Landscapist 문서](https://skydoves.github.io/landscapist/) |

- 예외에 라이브러리를 추가하거나 빼려면 이 표를 고치는 PR을 따로 올린다.
- 표에 없는 라이브러리를 새로 들여올 때, 공식 가이드나 NiA가 같은 역할에 쓰는 라이브러리가 있으면 그것을 쓴다. (예: 네트워크 Retrofit + OkHttp, 직렬화 kotlinx.serialization, DI Hilt, 저장소 Room)

## 결정 사항

공식 가이드가 여러 선택지를 허용해서 이 프로젝트가 하나로 정한 것이다.

| 주제 | 결정 | 근거 |
|---|---|---|
| 화면 구조 | 단일 Activity + Navigation 3 | [Recommendations: UI layer](https://developer.android.com/topic/architecture/recommendations#ui-layer) |
| 데이터 | 오프라인 우선. Room이 기준 데이터(source of truth)이고 네트워크는 로컬을 갱신한다 | [Build an offline-first app](https://developer.android.com/topic/architecture/data-layer/offline-first) |
| 동기화 | pull 방식 (화면에 필요할 때 받아온다) | PokeAPI는 변경 목록이나 푸시를 제공하지 않아 push 방식이 불가능하다 |
| 모듈 | NiA 구성 (`:feature:*:api/impl`, `:core:*`) | [Modularization patterns](https://developer.android.com/topic/modularization/patterns), NiA |
| 도메인 레이어 | UseCase는 로직이 있을 때만 만든다 | [Domain layer](https://developer.android.com/topic/architecture/domain-layer) — 선택 레이어 |
| 에러 처리 | 데이터 레이어는 예외를 던지고, 상태를 만드는 쪽이 잡는다 | [Data layer](https://developer.android.com/topic/architecture/data-layer) |

## 다른 문서와의 관계

| 문서 | 성격 |
|---|---|
| `docs/conventions/` | 목표 상태의 규칙 |
| `CLAUDE.md` | 지금 코드의 구조 설명 |
| `docs/improvement-plan.md` | 지금 코드를 목표 상태로 옮기는 순서 |

컨벤션은 지금 코드와의 차이나 옮기는 순서를 다루지 않는다. 그것은 개선 계획의 몫이다.

## 공식 자료

- 아키텍처: [Guide to app architecture](https://developer.android.com/topic/architecture) · [UI layer](https://developer.android.com/topic/architecture/ui-layer) · [UI events](https://developer.android.com/topic/architecture/ui-layer/events) · [Domain layer](https://developer.android.com/topic/architecture/domain-layer) · [Data layer](https://developer.android.com/topic/architecture/data-layer) · [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first) · [Recommendations](https://developer.android.com/topic/architecture/recommendations)
- 모듈: [Guide to Android app modularization](https://developer.android.com/topic/modularization) · [Common modularization patterns](https://developer.android.com/topic/modularization/patterns)
- 내비게이션: [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) · [Save state](https://developer.android.com/guide/navigation/navigation-3/save-state) · [nav3-recipes](https://github.com/android/nav3-recipes)
- Kotlin: [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide) · [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) · [Coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
- Compose: [Compose API guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md) · [State hoisting](https://developer.android.com/develop/ui/compose/state-hoisting) · [Stability](https://developer.android.com/develop/ui/compose/performance/stability) · [Performance best practices](https://developer.android.com/develop/ui/compose/performance/bestpractices) · [Accessibility](https://developer.android.com/develop/ui/compose/accessibility)
- 데이터: [Paging: network and database](https://developer.android.com/topic/libraries/architecture/paging/v3-network-db) · [Room](https://developer.android.com/training/data-storage/room)
- DI: [Hilt with Jetpack](https://developer.android.com/training/dependency-injection/hilt-jetpack) · [Hilt testing](https://developer.android.com/training/dependency-injection/hilt-testing)
- 테스트: [What to test](https://developer.android.com/training/testing/fundamentals/what-to-test) · [Test doubles](https://developer.android.com/training/testing/fundamentals/test-doubles) · [Testing coroutines](https://developer.android.com/kotlin/coroutines/test) · [Test Paging](https://developer.android.com/topic/libraries/architecture/paging/test)
- 빌드: [Sharing build logic with convention plugins](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html) · [Version catalogs](https://developer.android.com/build/migrate-to-catalogs) · [Shrink, obfuscate, and optimize](https://developer.android.com/build/shrink-code)
- 참고 앱: [Now in Android](https://github.com/android/nowinandroid)
