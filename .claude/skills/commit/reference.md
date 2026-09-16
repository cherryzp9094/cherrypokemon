# 커밋 규칙 참고

## 타입 판정표

| 타입 | 정의 | 이 프로젝트에서 |
|---|---|---|
| `feat` | 새 기능 | 새 화면, 새 API 연동, 사용자에게 보이는 동작 추가 |
| `fix` | 버그 수정 | 크래시, 잘못된 값, 의도와 다른 동작 |
| `refactor` | 버그 수정도 기능 추가도 아닌 코드 변경 | 구조 변경, Orbit 전환, 모듈 분리, 이름 변경·파일 이동, 죽은 코드 삭제 |
| `perf` | 성능 개선 | 리컴포지션 줄이기, 중복 다운로드 제거, 블로킹 호출 제거 |
| `style` | 코드 의미가 바뀌지 않는 변경 | 포맷, 공백, 미사용 import 삭제 |
| `test` | 테스트 추가·수정 | `src/test`, `src/androidTest` 변경만 있는 경우 |
| `docs` | 문서만 변경 | `CLAUDE.md`, `docs/`, KDoc·주석만 바꾼 경우 |
| `build` | 빌드 시스템, 외부 의존성 | `libs.versions.toml`, `build.gradle.kts`, `buildSrc`, `gradle.properties`, gradle wrapper, ProGuard 규칙, compileSdk·targetSdk |
| `ci` | CI 설정 | `.github/workflows` |
| `chore` | 소스·테스트를 건드리지 않는 기타 변경 | `.gitignore`, `.editorconfig`, `.claude/` 설정·스킬 |
| `revert` | 이전 커밋 되돌리기 | 아래 "되돌리기" 참고 |

## 헷갈리기 쉬운 경우

이 저장소의 실제 커밋 기록에서 가져온 예시다. 왼쪽처럼 쓰지 않는다.

| 기록에 남은 커밋 | 올바른 형태 |
|---|---|
| `feat: pokemonDetail LogView 삭제` | `refactor: PokemonDetail LogView 삭제` |
| `feat: pokemonDetail stats 데이터 제거` | `refactor: PokemonDetail stats 필드 삭제` |
| `feat: 팔레트 dependency 추가 및 포켓몬 색상에 따른 배경 추가` | `build: palette-ktx 의존성 추가` → `feat: 포켓몬 대표 색상 배경 추가` |
| `feat: buildSrc 설정 및 app 모듈 적용` | `build: buildSrc 공통 설정 추가` → `build: app 모듈에 buildSrc 설정 적용` |
| `feat: pokemonCard 백그라운드 컬러 처리 로직 최적화` | `perf: PokemonCard 배경색 계산 최적화` |
| `style: 불필요한 import 제거` | 그대로 맞음 |

그 밖의 경계 사례:

| 변경 | 타입 | 이유 |
|---|---|---|
| 파일·클래스 이름만 변경 | `refactor` | 동작이 그대로이고 코드 구조가 바뀜 |
| 미사용 클래스·함수 삭제 | `refactor` | 코드 구조가 바뀜 (import만 지우면 `style`) |
| 미사용 의존성 삭제 | `build` | 의존성 변경 |
| 버전 업데이트 때문에 생긴 코드 수정 | `build` 다음에 `fix` 또는 `refactor` | 의존성 변경과 코드 변경을 나눈다. 단, 버전만 올리면 빌드가 깨질 때는 한 커밋으로 묶는다 |
| 원래 의도대로 동작하지 않던 UI를 고침 | `fix` | |
| 기획이 바뀌어서 UI를 바꿈 | `feat` | |
| 에러·로딩 상태 UI 추가 | `feat` | 사용자에게 새로 보이는 동작 |
| Hilt·KSP 컴파일 에러 수정 | `fix` | 빌드가 안 되는 버그 |

## 커밋 나누기 예시

**버그 여러 개 + 테스트**

```
fix: PokemonPagingSource 네트워크 에러 크래시 수정
fix: PokemonDetail 배경색 폴백 값 수정
fix: Palette 폴백 색상 투명 문제 수정
fix: Pokemon id 파싱 예외 수정
test: PokemonPagingSource 에러 처리 테스트 추가
test: Pokemon id 파싱 테스트 추가
refactor: 미사용 PokemonSpecies api 삭제
style: 미사용 import 삭제
```

**레이어를 가로지르는 새 기능**

```
feat: pokemon-species api 추가
feat: PokemonSpecies 도메인 모델 추가
feat: PokemonSpecies repository 구현
feat: PokemonDetail 서식지 정보 표시
```

**의존성 추가와 그 사용**

```
build: orbit-mvi 의존성 추가
refactor: PokemonDetailViewModel Orbit 컨테이너로 전환
test: PokemonDetailViewModel 테스트 추가
refactor: MainViewModel Orbit 컨테이너로 전환
refactor: 커스텀 MVI base 패키지 삭제
```

**나누지 않는 경우**

```
refactor: ui.view 패키지를 feature 패키지로 이동     ← 파일 40개여도 하나
chore: 커밋 규칙 스킬 추가                          ← SKILL.md와 그것이 링크하는 reference.md
```

## 제목

| 나쁜 예 | 좋은 예 | 이유 |
|---|---|---|
| `fix: 버그 수정` | `fix: PokemonDetail 배경색 폴백 값 수정` | 무엇을 고쳤는지 알 수 없음 |
| `fix: PokemonDetailViewModel에서 SavedStateHandle로 배경색을 가져올 때 값이 없으면 잘못된 기본값이 들어가는 문제 수정` | `fix: PokemonDetail 배경색 폴백 값 수정` | 너무 김. 자세한 내용은 코드와 본문에 |
| `fix: 배경색 폴백 수정함.` | `fix: 배경색 폴백 값 수정` | 개조식 어미, 마침표 |
| `Fix: 배경색 폴백 값 수정` | `fix: 배경색 폴백 값 수정` | 타입 대문자 |
| `fix(app): 배경색 폴백 값 수정` | `fix: 배경색 폴백 값 수정` | 스코프를 쓰지 않음 |
| `feat: 에러 상태 추가 및 재시도 버튼 추가` | `feat: PokemonDetail 에러 상태 추가` → `feat: PokemonDetail 재시도 버튼 추가` | "및"이 들어가면 나눈다 |

## 본문

**쓰는 경우**: 코드만 봐서는 이유를 알 수 없을 때

```
fix: TestViewModel Hilt 생성자 누락 수정

@HiltViewModel은 의존성이 없어도 @Inject 생성자가 있어야
KSP가 처리한다.
```

```
build: Kotlin 2.1.21 업데이트

orbit-mvi 10 이상이 kotlin-stdlib 2.1.21과 lifecycle 2.9.0을
요구한다.
```

**쓰지 않는 경우**: 제목을 반복하게 될 때

```
style: 미사용 import 삭제

PokemonPagingSource에서 미사용 import를 삭제했다.   ← 쓰지 않는다
```

본문은 한 줄 72자 안팎에서 줄을 바꾼다.

## 되돌리기

`git revert`가 만드는 기본 메시지(`Revert "..."`)를 아래 형식으로 고친다.

```
revert: fix: PokemonPagingSource 네트워크 에러 크래시 수정

This reverts commit 1a2b3c4.

getRefreshKey 변경 후 새로고침할 때 목록이 비는 문제가 생겨 되돌린다.
```
