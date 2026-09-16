---
name: pr
description: 팀 PR 규칙에 따라 현재 브랜치를 푸시하고 main 대상 PR을 만든다. 이미 열린 PR이 있으면 제목과 본문을 규칙에 맞게 고친다.
disable-model-invocation: true
allowed-tools: Bash(git branch *) Bash(git status *) Bash(git log *) Bash(git diff *) Bash(git fetch *) Bash(git push *) Bash(gh pr list *) Bash(gh pr view *) Bash(gh pr create *) Bash(gh pr edit *) Bash(./gradlew *)
---

# PR

## 현재 상태

- 브랜치: !`git branch --show-current`
- 커밋 안 된 변경:
!`git status --short`

## 규칙이 충돌할 때의 우선순위

1. **PR 하나에 목적 하나.** 제목에 "및"이 필요하면 PR을 나눠야 한다는 신호다.
2. **확인 방법이 사실이다.** 실제로 해본 확인만 적고, 하지 않은 확인은 하지 않았다고 적는다.
3. **본문이 짧다.** 코드와 커밋에 이미 있는 내용을 다시 풀어 쓰지 않는다.

## 제목

- 커밋 규칙과 같다: `타입: 한국어 명사형 제목`. 스코프 없음, 마침표 없음, 50자 상한.
- 커밋이 하나면 그 커밋 헤더를 그대로 쓴다.
- 커밋이 여러 개면 전체 목적을 요약한다. 타입은 PR의 주된 목적을 따른다. (예: `build` 1개 + `refactor` 4개로 Orbit 전환 → `refactor: MVI를 Orbit으로 전환`)
- 타입 정의는 [../commit/reference.md](../commit/reference.md)의 판정표를 따른다.

## 브랜치 이름

`타입/짧은-영문-설명`. 소문자 kebab-case. (예: `fix/paging-crash`, `chore/pr-skill`) 브랜치는 `/commit`이 커밋하기 전에 만든다.

## 본문

섹션 이름과 순서를 고정한다. `.github/pull_request_template.md`와 같다.

```markdown
## 개요

왜 하는지 1~3줄.

## 변경 사항

- 요점 목록

## 확인 방법

- [x] 실제로 한 확인
- [ ] 하지 않은 확인 (이유)

## 참고

리뷰어가 알아야 할 것. 없으면 섹션째 뺀다.
```

- **개요**: 무엇을 바꿨는지가 아니라 **왜** 바꾸는지. 관련 이슈나 계획 문서(`docs/improvement-plan.md`의 Phase 등)가 있으면 링크한다.
- **변경 사항**: 커밋이 여러 개면 커밋 헤더 목록을 그대로 쓴다. 커밋이 하나면 제목만으로 부족한 요점을 2~5개 적는다.
- **확인 방법**: 체크박스로 적는다. 코드 변경이 없으면 `- 코드 변경 없음 (빌드 영향 없음)` 한 줄로 끝낸다.
- **참고**: 선택. 후속 작업, 의도적으로 남긴 것, 리뷰 때 봐줬으면 하는 곳.
- 대화에 PR 꼬리줄이 지정되어 있으면 본문 끝에 빈 줄 하나 뒤에 붙인다.

작성이 애매하면 [reference.md](reference.md)의 예시를 읽는다.

## 절차

### 1. 브랜치와 변경 확인

1. 현재 브랜치가 `main`이면 PR을 만들지 않고 멈춘다. `git log --oneline origin/main..main`에 커밋이 있으면 그 목록을 보여주고, 브랜치로 옮기려면 로컬 `main`을 되돌려야 하니 어떻게 할지 사용자에게 묻는다.
2. 커밋 안 된 변경이 있으면 `/commit`을 먼저 하라고 안내하고 멈춘다. 단, `/commit`이 커밋하지 않는 파일(`.idea/`, `build/`, 줄바꿈만 바뀐 파일 등)만 있으면 무시하고 진행한다.
3. `git fetch origin main`
4. `git log --oneline origin/main..HEAD`로 PR에 들어갈 커밋을 확인한다. 없으면 그렇게 알리고 끝낸다.

### 2. 내용 파악

- `git log origin/main..HEAD --format='%h %s%n%b'`로 커밋 헤더와 본문을 읽는다.
- `git diff origin/main...HEAD --stat`으로 변경 범위를 보고, 필요한 파일은 `git diff origin/main...HEAD -- <파일>`로 읽는다.
- 커밋들의 목적이 둘 이상으로 갈리면 확인 단계에서 그 사실을 알린다.

### 3. 빌드 확인

`.kt`, `.kts`, `.toml`, `.xml`, `.pro` 변경이 있으면 `./gradlew assembleDebug`를 실행한다. `src/test`에 변경이 있으면 `./gradlew testDebugUnitTest`도 실행한다.

- 실패하면 PR을 만들지 않고 에러를 보고한 뒤 멈춘다.
- 결과를 본문의 **확인 방법**에 적는다. 실기기 확인처럼 이 단계에서 할 수 없는 것은 체크하지 않은 항목으로 남긴다.

### 4. 기존 PR 확인

`gh pr list --head <브랜치> --state open --json number,url,title,body`

- 열린 PR이 있으면 새로 만들지 않고 5단계에서 수정안을 보여준다.

### 5. 확인받기

제목과 본문 전체를 보여준다. 기존 PR을 고치는 경우에는 무엇이 바뀌는지도 적는다.

그 다음 **AskUserQuestion**으로 진행 여부를 묻는다. 일반 메시지로 묻고 턴을 끝내면 `allowed-tools`의 사전 승인이 사라지기 때문이다. 1번 우선순위를 어겼다면(목적이 둘 이상) 선택지에 "그대로 진행 / 중단하고 나누기"를 넣는다.

### 6. 푸시와 PR

1. `git push -u origin <브랜치>`. 거부되면 멈추고 보고한다. **force push 하지 않는다.**
2. 본문은 heredoc으로 넘긴다.
   - 새 PR:

     ```bash
     gh pr create --base main --head <브랜치> --title "<제목>" --body-file - <<'MSG'
     ## 개요
     ...
     MSG
     ```
   - 기존 PR: `gh pr edit <번호> --title "<제목>" --body-file - <<'MSG'`
3. 머지는 하지 않는다.

### 7. 보고

- PR URL
- 포함된 커밋 목록
- 확인하지 않은 항목
