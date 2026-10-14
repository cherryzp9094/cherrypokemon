# 상세 화면 정보 확장 기록

- 계획 작성: 2026-10-09 (`c2723a7`)
- 완료: 2026-10-14 (`main`)
- 규칙: [`docs/conventions/`](conventions/README.md). 이 구조로 오기까지는 [개선 기록](improvement-plan.md)에 있다.

이미지·이름·타입·키·몸무게만 있던 상세 화면에 **종족값·특성·한국어 표기·진화 체인·타입 상성**을 넣었다. 중간에 **국제화**가 들어와 계획보다 범위가 넓어졌다.

## 한 일

| 단계 | 내용 | PR |
|---|---|---|
| 계획 | 네 기능과 순서를 정한다 | [#28](https://github.com/cherryzp9094/cherrypokemon/pull/28) |
| F-1 | 종족값·특성. 상세 화면을 머리말 띠 + 두 칸 구성으로 다시 만든다 | [#29](https://github.com/cherryzp9094/cherrypokemon/pull/29) |
| I-1 | 앱 문자열을 영어 기본 + `values-ko/` 로 분리 | [#30](https://github.com/cherryzp9094/cherrypokemon/pull/30) |
| I-2 | 앱 안에서 언어 전환. 목록 상단을 제목 줄 + 국기 토글로 | [#31](https://github.com/cherryzp9094/cherrypokemon/pull/31) |
| F-2 | 한국어 이름·분류·도감 설명 | [#32](https://github.com/cherryzp9094/cherrypokemon/pull/32) |
| F-4a | 타입·특성 이름을 한국어로 | [#33](https://github.com/cherryzp9094/cherrypokemon/pull/33) |
| F-4b | 타입 상성(약점·저항·무효) | [#34](https://github.com/cherryzp9094/cherrypokemon/pull/34) |
| F-3 | 진화 체인과 체인 안 이동 | [#35](https://github.com/cherryzp9094/cherrypokemon/pull/35) |

계획은 F-1 → F-2 → F-3 → F-4 순서였다. 국제화를 넣기로 하면서 **타입·특성 이름(F-4a)을 진화 체인보다 먼저** 했다. 상세 화면에서 영어로 남은 게 그 둘뿐이라 체감이 컸고, F-4b 와 데이터가 같아 한 번에 받을 수 있었다.

## 늘어난 데이터

| 출처 | 쓰는 것 | 호출 |
|---|---|---|
| `/pokemon/{id}` | 종족값 6개, 특성, 기초 경험치 | 이미 받던 것 |
| `/pokemon-species/{id}` | 한국어·영어 이름, 분류, 도감 설명, 체인 주소 | 상세 1회 |
| `/type/{name}` | 언어별 이름, 받는 쪽 상성 | 타입당 1회, 영구 캐시 |
| `/ability/{name}` | 언어별 이름 | 특성당 1회, 영구 캐시 |
| `/evolution-chain/{id}` | 진화 단계와 조건 | 체인당 1회 |

**쓸수록 호출이 사라진다.** 타입은 21개, 특성은 374개뿐이고 체인은 구성원이 공유하므로, 몇 마리만 열어보면 새 포켓몬을 열어도 상세와 종 정보 두 번만 받는다.

**언어는 둘 다 저장한다.** PokeAPI 가 한 응답에 한국어와 영어를 함께 주므로 둘 다 넣어 둔다. 국기를 눌러도 다시 받지 않고, 비행기 모드에서도 바뀐다.

## 스키마 변화

| 버전 | 내용 |
|---|---|
| 1 | 목록, 상세, remote key |
| 2 | 종족값 6개, 특성, 기초 경험치 |
| 3 | 언어별 이름·분류·도감 설명 |
| 4 | `type_names`, `ability_names` 이름 캐시 |
| 5 | 타입별 받는 상성 3개 컬럼 |
| 6 | `evolution_chain_members`, 상세의 체인 ID |

모두 `AutoMigration` 이고 **버전마다 마이그레이션 테스트가 있다**(`MigrationTest`). 계획에 적어둔 "스키마를 바꿀 때마다 마이그레이션 테스트" 를 지켰다.

## 겪은 함정

새로 추가된 것만 적는다. 그 전 함정은 [개선 기록](improvement-plan.md)에 있다.

**`MigrationTestHelper` 는 테스트마다 다른 파일을 써야 한다.** 앞 테스트가 남긴 파일이 있으면 `createDatabase` 가 `A database at the driver configured path likely already exists` 로 실패한다. 파일 이름에 `UUID` 를 넣는다.

**`LocalContext` 를 덮어쓰면 `hiltViewModel()` 이 죽는다.** 언어 전환은 `createConfigurationContext` 로 만든 컨텍스트를 `CompositionLocalProvider` 에 넣는데, 그건 Activity 가 아니라서 `Expected an activity context` 가 난다. `stringResource` 는 `LocalResources` 에서 읽으므로 `LocalConfiguration` 과 `LocalResources` 만 바꾸면 된다.

**CI 기기는 개발 기기보다 화면이 작다.** CI 는 `pixel5Api35`, 손에 있는 에뮬레이터는 더 크다. 상세 화면이 길어질수록 로컬에서는 보이는 것이 CI 에서는 화면 밖이라 `assertIsDisplayed` 가 깨진다. **상세 화면을 건드리면 `./gradlew pixel5Api35Check` 로 먼저 확인한다.** 화면 아래쪽 단언에는 `performScrollTo()` 를 붙인다.

**같은 글자가 두 곳에 나오면 `onNodeWithText` 가 노드를 둘 찾는다.** 진화 체인이 들어오면서 이름이 머리말과 체인에 모두 나왔다. `onAllNodesWithText(...).onFirst()` 로 어느 쪽을 보는지 분명히 한다.

**Landscapist 의 기본 `contentScale` 은 `Crop` 이다.** 영역에 맞춰 잘라내므로 이미지 전체를 보여주려면 `ImageOptions(contentScale = ContentScale.Fit)` 를 명시한다.

**`Scaffold` 의 content padding 을 쓰지 않으면 Lint 가 막는다**(`UnusedMaterial3ScaffoldPaddingParameter`). 상태 바까지 그리고 싶으면 `Scaffold` 를 쓰지 말고 직접 겹쳐 그린 뒤 `statusBarsPadding()` 으로 맞춘다.

**화면에 고정한 버튼은 스크롤할 때 내용과 겹친다.** 뒤로가기 버튼을 머리말 안으로 넣어 함께 올라가게 했다.

**모듈을 새로 만들면 빈 `lint-baseline.xml` 이 생기며 한 번 실패한다.** 다시 돌리면 통과한다.

**ktlint 는 파일 이름과 단일 클래스 이름이 같기를 요구한다**(`standard:filename`). 모델 파일을 쪼갤 때 걸린다.

## 지금 상태

- 단위·계측 테스트 82개
- 계측 테스트 32개가 기기에서 돈다 (마이그레이션 1→2, 2→3, 3→4, 4→5, 5→6 포함)
- 상세 화면에 영어로 남은 것 없음. 목록은 계획대로 영문 이름 유지
- 국기 토글로 앱 전체 언어 전환. 선택은 DataStore 에 저장

## 하지 않은 것

- **목록 화면 한국어 이름.** 아이템마다 species 호출이 필요해 페이징과 맞지 않는다.
- **목록 검색·필터·즐겨찾기.** 즐겨찾기는 사용자 데이터라 마이그레이션 규칙이 더 엄격해진다.
- **진화 조건 전부.** 레벨과 도구만 보여준다. 교환·친밀도 같은 조건과 도구의 한국어 이름은 호출이 더 필요하다.
- **기술(move) 목록.** 포켓몬당 수백 개라 별도 화면과 페이징이 필요하다.
- **울음소리·애니메이션 스프라이트.**
