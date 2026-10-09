# CherryPokemon 개선 완료 기록

- 계획 작성일: 2026-09-17 (기준 커밋 `1106062`)
- 완료일: 2026-10-09
- 목표: [`docs/conventions/`](conventions/README.md)

계획한 모든 단계를 끝냈다. 이 문서는 무엇을 어떤 순서로 바꿨고 왜 그렇게 했는지 남기는 기록이다. 앞으로 할 일은 여기에 적지 않는다.

## 옮긴 방식

- **점진 전환.** PR 하나하나가 빌드되고 앱이 실행되는 상태를 유지했다. 새 모듈을 옛 모듈 옆에 만들고 화면 단위로 옮긴 뒤 옛 코드를 지웠다.
- **옛 코드의 버그는 고치지 않았다.** 결함은 새 구조로 옮기면서 사라졌고, 그 자리에 회귀 테스트를 넣었다.
- 과도기 연결은 한 곳(6-1 에서 새 목록 화면이 옛 상세 Activity 를 띄운 것)만 허용했고 6-2 에서 지웠다.

## 단계별 결과

| 단계 | 내용 | PR |
|---|---|---|
| 1-1 | `buildSrc` → `build-logic` convention plugin | [#6](https://github.com/cherryzp9094/cherrypokemon/pull/6) |
| 1-2 | 쓰지 않는 의존성 삭제 (APK 항목 984 → 222) | [#7](https://github.com/cherryzp9094/cherrypokemon/pull/7) |
| 1-3 | 병렬 빌드·빌드 캐시, Jetifier 끄기 | [#8](https://github.com/cherryzp9094/cherrypokemon/pull/8) |
| 2-1 | Spotless + ktlint | [#9](https://github.com/cherryzp9094/cherrypokemon/pull/9) |
| 2-2 | Android Lint 와 baseline | [#10](https://github.com/cherryzp9094/cherrypokemon/pull/10) |
| 2-3 | GitHub Actions | [#11](https://github.com/cherryzp9094/cherrypokemon/pull/11) |
| 3-1 | Gradle 9.8 / AGP 9.4 / Kotlin 2.4 / compileSdk 37 | [#12](https://github.com/cherryzp9094/cherrypokemon/pull/12) |
| 3-2 | configuration cache | [#13](https://github.com/cherryzp9094/cherrypokemon/pull/13) |
| 4-1~4-5 | `:core:model`·`common`·`testing`·`network`·`database`·`data` | [#14](https://github.com/cherryzp9094/cherrypokemon/pull/14) [#15](https://github.com/cherryzp9094/cherrypokemon/pull/15) [#16](https://github.com/cherryzp9094/cherrypokemon/pull/16) [#17](https://github.com/cherryzp9094/cherrypokemon/pull/17) [#18](https://github.com/cherryzp9094/cherrypokemon/pull/18) |
| 5 | `:core:designsystem`·`ui`·`navigation` | [#19](https://github.com/cherryzp9094/cherrypokemon/pull/19) |
| 6-1 | 단일 Activity + Navigation 3, 목록 화면 | [#20](https://github.com/cherryzp9094/cherrypokemon/pull/20) |
| 6-2 | 상세 화면, Orbit MVI 전환 | [#21](https://github.com/cherryzp9094/cherrypokemon/pull/21) |
| 6-3 | 옛 구조 삭제 (`:data`, `:domain`, 직접 만든 MVI) | [#22](https://github.com/cherryzp9094/cherrypokemon/pull/22) |
| 6-4, 6-5 | targetSdk 37, 예측형 뒤로가기, 내비게이션 계측 테스트 | [#23](https://github.com/cherryzp9094/cherrypokemon/pull/23) |
| 7-1 | release 빌드 R8 (APK 16.8MB → 1.6MB) | [#24](https://github.com/cherryzp9094/cherrypokemon/pull/24) |
| 7-2 | 적응형 목록-상세 레이아웃 | [#25](https://github.com/cherryzp9094/cherrypokemon/pull/25) |
| 7-3, 7-4 | Baseline Profile, 스크린샷 테스트 | [#26](https://github.com/cherryzp9094/cherrypokemon/pull/26) |

## 없앤 결함

계획을 세울 때 코드로 확인한 결함이다. 모두 새 구조로 바뀌면서 사라졌다.

| 결함 | 없앤 방법 |
|---|---|
| `PokemonPagingSource.load()` 의 API 호출이 `try` 밖이라 오프라인에서 크래시 | `RemoteMediator` 가 `IOException`·`HttpException` 을 `MediatorResult.Error` 로 바꾼다 |
| 로딩·에러·빈 상태 UI 없음 | 목록은 `loadState.source`/`mediator`, 상세는 `RefreshState` 로 그린다 |
| `getRefreshKey()` 가 `null` 이라 새로고침 시 스크롤 위치 유실 | Room 이 만든 `PagingSource` 를 쓴다 |
| Lazy 목록에 `key` 없음 | `itemKey { it.id }` |
| 상세 API 실패 시 크래시 | 상태로 표현하고, 캐시가 있으면 화면을 유지한 채 메시지만 띄운다 |
| 배경색 폴백 오류와 Palette alpha 0 | 상세 화면이 이미지에서 직접 추출한다 |
| `Pokemon.id` 의 `NumberFormatException`, 매번 URL 파싱 | 데이터 레이어가 한 번 계산해 저장한다 |
| 도메인 모델에 이미지 서버 주소 하드코딩 | 데이터 레이어의 매퍼가 만든다 |
| Gson 이 snake_case 필드명에 의존해 R8 을 켜면 파싱이 깨짐 | kotlinx.serialization |
| SideEffect 버퍼가 0이라 구독자가 없으면 유실 | Orbit |
| `fetchDominantColor` 가 IO 를 막고 이미지를 두 번 받음 | Landscapist palette 플러그인 |
| 색상 `ImmutableMap` 때문에 카드 하나가 바뀌면 전부 리컴포즈 | 카드가 자기 색을 직접 들고 있다 |
| `Window` 주입과 상태바 색 이중 설정 | edge-to-edge |
| `:app → :data` 레이어 위반 | 모듈 삭제 |
| `:domain` 이 Android 라이브러리이면서 Parcelize·Hilt 보유 | `:core:model` 은 JVM 모듈 |
| 디스패처 하드코딩 | `@Dispatcher` qualifier 주입 |
| 패키지 이름(`ui/view/base/base`, camelCase) | 새 모듈은 모듈 경로를 따른다 |

## 겪은 함정

같은 실수를 반복하지 않으려고 남긴다.

| 함정 | 내용 |
|---|---|
| `settings.gradle.kts` 등록 누락 | 모듈을 만들고 include 를 빠뜨리면 그 모듈은 빌드도 테스트도 되지 않는데 전체 빌드는 통과한다. 두 번 겪었다 |
| 계측 테스트 0개 통과 | `androidx.test:runner` 가 없으면 테스트가 하나도 실행되지 않고 성공으로 끝난다. `am instrument` 를 직접 돌려야 드러났다 |
| Espresso 버전 충돌 | Compose `ui-test-junit4` 가 옛 Espresso 를 끌어와 최신 기기에서 깨진다. 계측 테스트 공통 의존성을 convention plugin 으로 올려 고정했다 |
| Spotless 와 ktlint 클래스 로딩 | 모듈마다 Spotless 를 걸면 그 모듈의 플러그인 클래스패스가 ktlint 룰 초기화를 깨뜨린다. 루트에서만 설정한다 |
| Fake 의 스코프 | `@Singleton` 이 없으면 Hilt 테스트에서 테스트와 ViewModel 이 서로 다른 Fake 를 받는다 |
| `PagingData` 테스트 | 로드 상태를 주지 않으면 `asSnapshot` 이 로딩을 기다리며 멈춘다. `cachedIn` 코루틴도 테스트 끝에 정리해야 한다 |
| Orbit 테스트 | `onCreate` 가 끝나지 않는 구독을 띄우므로 `cancelAndIgnoreRemainingItems()` 가 필요하고, 단계마다 기대 상태를 기다려야 순서가 흔들리지 않는다 |
| 에뮬레이터 네트워크 | DNS 가 잡히기 전에는 어떤 빌드든 실패한다. R8 문제로 오인했다가 A/B 로 확인했다 |
| `internal` 의 모듈 경계 | 이 구성에서는 다른 Gradle 모듈에서도 참조된다. 경계는 의존성과 공개 API 설계로 지킨다 |

## 남은 숙제

- 스크린샷 테스트는 실험 기능이다. AGP 9.5 부터 설정 방식이 AGP test suites 로 바뀐다고 공지되어 있어 툴체인을 올릴 때 옮겨야 한다.
- `:core:ui` 의 스크린샷 테스트는 `PokemonCard` 하나뿐이다. 공용 컴포넌트가 늘면 함께 늘린다.
- Room 마이그레이션 테스트는 아직 없다. 스키마를 처음 바꿀 때 `room3-testing` 과 함께 추가한다.
