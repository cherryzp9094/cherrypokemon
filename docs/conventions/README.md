# CherryPokemon 컨벤션

코드를 새로 쓰거나 고칠 때 따르는 규칙이다. Android 공식 아키텍처 가이드, Kotlin 스타일 가이드, Compose API 가이드라인을 근거로 이 프로젝트에 해당하는 것만 골랐다.

- 기준일: 2026-09-16
- 규칙마다 근거 링크를 단다. 링크가 없는 규칙은 이 프로젝트에서 정한 것이다.

## 문서

| 문서 | 다루는 것 |
|---|---|
| [architecture.md](architecture.md) | 모듈과 레이어, 의존 방향, 상태 관리(UDF·Orbit), 단일 Activity와 Navigation 3 |
| [kotlin.md](kotlin.md) | 이름, 패키지, 파일 구성, 포맷, 코루틴과 Flow |
| [compose.md](compose.md) | Route/Screen 분리, 상태 호이스팅, 파라미터, 안정성, 리스트, 테마·리소스·접근성 |
| [data.md](data.md) | Repository, UseCase, DTO와 매퍼, 에러 처리, DI |
| [testing.md](testing.md) | 무엇을 어떤 테스트로 확인할지, Fake, 코루틴·Flow·Paging 테스트 |

## 규칙의 강도

공식 [Architecture recommendations](https://developer.android.com/topic/architecture/recommendations)의 등급을 그대로 옮겼다.

| 표시 | 공식 등급 | 의미 |
|---|---|---|
| `필수` | Strongly recommended | 어기면 리뷰에서 수정을 요청한다. 예외가 필요하면 PR 본문의 참고에 이유를 적는다 |
| `권장` | Recommended | 특별한 이유가 없으면 따른다 |
| `선택` | Optional | 상황에 맞게 고른다. 한 번 고르면 같은 코드 안에서는 통일한다 |

## 다른 문서와의 관계

| 문서 | 성격 |
|---|---|
| `CLAUDE.md` | **지금 코드**가 어떻게 되어 있는지 |
| `docs/conventions/` | **앞으로 코드**를 어떻게 써야 하는지 |
| `docs/improvement-plan.md` | 지금 코드를 컨벤션으로 **어떤 순서로** 옮길지 |

- 지금 코드는 컨벤션과 다른 곳이 많다. 그 차이는 `CLAUDE.md`의 "알려진 부채"와 개선 계획에서 다룬다. 컨벤션 문서에는 목표 상태만 적는다.
- 개선 계획의 Phase가 끝나기 전에 그 영역을 건드리면, 한 번에 컨벤션으로 바꾸지 말고 기존 구조에 맞춘다. 예를 들어 Phase 2(Orbit 전환) 전에 화면을 고치면 `BaseViewModel`을 그대로 쓴다.
- 규칙을 바꾸려면 이 문서를 고치는 PR을 따로 올린다.

## 결정 사항

공식 가이드가 여러 선택지를 허용하거나, 공식 가이드와 이 프로젝트의 선택이 다른 곳이다.

| 주제 | 결정 | 이유 |
|---|---|---|
| 화면 구조 | **단일 Activity + Navigation 3** | 공식 권장(`필수`). Compose 전용 앱이고, back stack을 직접 상태로 다룰 수 있다 |
| 상태 관리 | **Orbit MVI**, State만 사용하고 SideEffect는 쓰지 않는다 | 공식 가이드는 ViewModel이 UI로 이벤트를 보내지 말라고 한다(`필수`). 내비게이션은 UI 레이어에서 처리한다 |
| 도메인 레이어 | `:domain` 모듈은 유지하되 **UseCase는 로직이 있을 때만** 만든다 | 공식 등급은 "큰 앱에서 권장". 전달만 하는 UseCase는 만들지 않는다 |
| 직렬화 | **kotlinx.serialization** | Navigation 3 키에 이미 필요하다. 리플렉션이 없어 R8에 안전하다 |
| 모듈 | `:app` / `:data` / `:domain` + `:core:common` | 화면이 2개라 feature 모듈은 아직 만들지 않는다 |

## 공식 자료

- 아키텍처: [Guide to app architecture](https://developer.android.com/topic/architecture) · [UI layer](https://developer.android.com/topic/architecture/ui-layer) · [UI events](https://developer.android.com/topic/architecture/ui-layer/events) · [Domain layer](https://developer.android.com/topic/architecture/domain-layer) · [Data layer](https://developer.android.com/topic/architecture/data-layer) · [Recommendations](https://developer.android.com/topic/architecture/recommendations)
- 모듈: [Common modularization patterns](https://developer.android.com/topic/modularization/patterns)
- 내비게이션: [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) · [Save state](https://developer.android.com/guide/navigation/navigation-3/save-state) · [nav3-recipes](https://github.com/android/nav3-recipes)
- Kotlin: [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide) · [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) · [Coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
- Compose: [Compose API guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md) · [State hoisting](https://developer.android.com/develop/ui/compose/state-hoisting) · [Stability](https://developer.android.com/develop/ui/compose/performance/stability) · [Strong skipping](https://developer.android.com/develop/ui/compose/performance/stability/strongskipping) · [Performance best practices](https://developer.android.com/develop/ui/compose/performance/bestpractices)
- DI: [Hilt with Jetpack](https://developer.android.com/training/dependency-injection/hilt-jetpack)
- 테스트: [What to test](https://developer.android.com/training/testing/fundamentals/what-to-test) · [Test doubles](https://developer.android.com/training/testing/fundamentals/test-doubles)
- 참고 앱: [Now in Android](https://github.com/android/nowinandroid)
