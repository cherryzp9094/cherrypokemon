# 테스트

> 코드 예시는 방향을 보여주기 위한 것이며, 이 프로젝트에서 아직 빌드해 보지 않았다.

## 1. 무엇을 테스트하나 `필수`

| 대상 | 종류 | 위치 | 확인할 것 |
|---|---|---|---|
| ViewModel | 로컬 단위 | `app/src/test` | 초기 상태, 성공·실패에 따른 상태 변화, 재시도 |
| Repository | 로컬 단위 | `data/src/test` | DTO → 도메인 변환 결과, 예외가 삼켜지지 않고 전달되는지 |
| PagingSource | 로컬 단위 | `data/src/test` | `prevKey` / `nextKey`, 마지막 페이지, 네트워크 예외 시 `LoadResult.Error` |
| 매퍼 | 로컬 단위 | `data/src/test` | 빠진 필드, 잘못된 값 (예: id를 읽을 수 없는 URL) |
| UseCase (있으면) | 로컬 단위 | `domain/src/test` | 조합 로직 |
| Screen | 계측 (Compose UI) | `app/src/androidTest` | 상태별로 보이는 요소, 콜백 호출 |
| 내비게이션 | 계측 | `app/src/androidTest` | 목록 → 상세 → 뒤로가기 |

- 정상 흐름만 테스트하지 않는다. 네트워크 에러, 잘못된 JSON, 빈 목록을 같이 확인한다.
- 프레임워크와 라이브러리의 동작(Retrofit이 요청을 보내는지, Hilt가 주입하는지)은 테스트하지 않는다.
- 로직은 로컬 단위 테스트로 확인하고, 계측 테스트는 UI와 화면 흐름에만 쓴다.

근거: [What to test](https://developer.android.com/training/testing/fundamentals/what-to-test), [Recommendations: Know what to test](https://developer.android.com/topic/architecture/recommendations#testing)

## 2. 테스트 대역은 Fake `필수`

mock 라이브러리 대신 인터페이스를 직접 구현한 Fake를 쓴다.

```kotlin
class FakePokemonRepository : PokemonRepository {
    var pokemonDetail: PokemonDetail? = null
    var error: Throwable? = null

    override fun getPokemonsStream(): Flow<PagingData<Pokemon>> = flowOf(PagingData.empty())

    override suspend fun getPokemonDetail(pokeId: Int): PokemonDetail {
        error?.let { throw it }
        return checkNotNull(pokemonDetail) { "pokemonDetail을 먼저 설정해야 한다" }
    }
}
```

- 이름은 `Fake<인터페이스>`다.
- 한 모듈에서만 쓰면 그 모듈의 `src/test/.../fake` 패키지에 둔다.
- 두 모듈 이상에서 쓰게 되면 `:core:testing` 모듈로 옮긴다. `선택`
- Fake로 대체하기 어려운 인터페이스라면 설계를 먼저 의심한다. 구현 클래스를 직접 받고 있지 않은지 확인한다.

근거: [Test doubles](https://developer.android.com/training/testing/fundamentals/test-doubles), [Recommendations: Prefer fakes to mocks](https://developer.android.com/topic/architecture/recommendations#testing)

## 3. 코루틴 `필수`

- 테스트 함수는 `runTest { }`로 감싼다.
- 디스패처를 주입받는 클래스에는 `StandardTestDispatcher(testScheduler)` 또는 `UnconfinedTestDispatcher(testScheduler)`를 넘긴다.
- `viewModelScope`를 쓰는 ViewModel 테스트는 메인 디스패처를 테스트 디스패처로 바꾸는 규칙을 쓴다.

```kotlin
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

- `Thread.sleep()`이나 실제 시간 `delay`로 기다리지 않는다. 가상 시간(`advanceUntilIdle()`)을 쓴다.

근거: [Coroutines best practices: Inject TestDispatchers in tests](https://developer.android.com/kotlin/coroutines/coroutines-best-practices), [Testing Kotlin coroutines on Android](https://developer.android.com/kotlin/coroutines/test)

## 4. ViewModel (Orbit) `필수`

`orbit-test`의 `testWithInternalState`로 상태 변화를 순서대로 확인한다.

```kotlin
class PokemonDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pokemonRepository = FakePokemonRepository()

    @Test
    fun `상세 조회에 실패하면 Error 상태가 된다`() = runTest {
        pokemonRepository.error = IOException()
        val viewModel = PokemonDetailViewModel(PokemonDetailNavKey(pokeId = 25), pokemonRepository)

        viewModel.testWithInternalState(this) {
            runOnCreate()
            expectInternalState { PokemonDetailUiState.Error }
        }
    }
}
```

- 초기 상태는 자동으로 확인되고, 확인하지 않은 상태 변화가 남으면 테스트가 실패한다.
- `onCreate`는 `runOnCreate()`를 불러야 실행된다.
- SideEffect를 쓰지 않으므로([architecture.md 3-4](architecture.md#3-4-viewmodel에서-ui로-이벤트를-보내지-않는다-필수)) 상태만 확인하면 된다.
- Orbit을 쓰지 않는 `StateFlow`는 `value`를 확인하거나 Turbine으로 순서대로 받는다. `stateIn(WhileSubscribed)`는 구독자가 있어야 값이 흐르므로 테스트에서 수집을 시작한다.

근거: [Recommendations: Test StateFlows](https://developer.android.com/topic/architecture/recommendations#testing)

## 5. Paging `권장`

`androidx.paging:paging-testing`을 쓴다.

| 대상 | 도구 |
|---|---|
| `PagingSource` | `TestPager(config, pagingSource)`로 `refresh()`, `append()` 후 결과 확인 |
| `Flow<PagingData<T>>` | `flow.asSnapshot()`으로 목록을 꺼내 확인 |

```kotlin
@Test
fun `네트워크 에러면 LoadResult_Error를 돌려준다`() = runTest {
    val pagingSource = PokemonPagingSource(FakePokemonApi(error = IOException()), pageSize = 20)
    val pager = TestPager(PagingConfig(pageSize = 20), pagingSource)

    val result = pager.refresh()

    assertTrue(result is PagingSource.LoadResult.Error)
}
```

근거: [Test your Paging implementation](https://developer.android.com/topic/libraries/architecture/paging/test)

## 6. Compose UI `권장`

- Route가 아니라 **Screen**을 테스트한다. 상태를 직접 넣으므로 ViewModel과 Hilt가 필요 없다.
- 요소는 사용자가 보는 텍스트나 `contentDescription`으로 찾는다. `testTag`는 그걸로 찾을 수 없을 때만 쓴다.

```kotlin
@Test
fun errorState_showsRetryButton_andCallsOnRetryClick() {
    var retried = false
    composeTestRule.setContent {
        PokemonDetailScreen(
            uiState = PokemonDetailUiState.Error,
            onRetryClick = { retried = true },
            onBackClick = {},
        )
    }

    composeTestRule.onNodeWithText(context.getString(R.string.retry)).performClick()

    assertTrue(retried)
}
```

근거: [Testing your Compose layout](https://developer.android.com/develop/ui/compose/testing)

## 7. 이름과 구성

| 항목 | 규칙 | 강도 |
|---|---|---|
| 테스트 클래스 | `<대상>Test` (`PokemonDetailViewModelTest`) | `필수` |
| 로컬 단위 테스트 함수 | 백틱 안에 한국어 문장: `` `상세 조회에 실패하면 Error 상태가 된다` `` | `권장` |
| 계측 테스트 함수 | `대상_조건_결과` (`errorState_showsRetryButton_andCallsOnRetryClick`). Android는 API 30 전까지 함수 이름의 공백을 지원하지 않는다 | `필수` |
| 본문 구성 | 준비 / 실행 / 확인을 빈 줄로 나눈다 | `권장` |
| 테스트 하나에 확인 하나 | 한 테스트가 여러 동작을 확인하지 않는다 | `권장` |

## 8. 버그와 테스트 `권장`

- 버그를 고치면 그 버그를 재현하는 테스트를 같은 PR에 넣는다. 커밋은 `fix`와 `test`로 나눈다.
- 테스트를 몰아서 따로 쓰지 않는다. 기능이나 수정과 같은 PR에 넣는다.
