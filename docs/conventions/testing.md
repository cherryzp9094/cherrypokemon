# 테스트

> 코드 예시는 규칙을 보여주기 위한 것이다. 라이브러리 버전에 따라 API 이름이 다를 수 있으니 작성할 때 해당 버전 문서를 확인한다.
>
> 이 문서가 이 프로젝트의 테스트 전략 문서다. ([Testing strategies](https://developer.android.com/training/testing/fundamentals/strategies) Key Point: "write a testing strategy for your app as part of your internal documentation")

## 1. 무엇을 테스트하나

### 1-1. 최소 범위 `필수`

- ViewModel 단위 테스트 (Flow 포함)
- 데이터 레이어(Repository, 데이터 소스) 단위 테스트
- CI에서 회귀 테스트로 돌리는 UI 내비게이션 테스트

근거: [Recommendations: Testing](https://developer.android.com/topic/architecture/recommendations#testing) — Know what to test(Strongly recommended)

### 1-2. 대상별 테스트 `필수`

| 대상 | 종류 | 위치 | 확인할 것 |
|---|---|---|---|
| ViewModel | 로컬 | `:feature:*:impl` `src/test` | 초기 상태, 데이터에 따른 상태 변화, 갱신 실패 시 상태·SideEffect, 재시도 |
| UseCase | 로컬 | `:core:domain` `src/test` | 조합 로직 |
| Repository | 로컬 | `:core:data` `src/test` | 로컬 데이터가 외부 모델로 나오는지, 갱신이 로컬에 저장되는지, 실패가 전달되는지 |
| 네트워크 데이터 소스·모델 | 로컬 | `:core:network` `src/test` | 실제 응답 JSON 파싱, 손상된 JSON |
| DAO | 계측 | `:core:database` `src/androidTest` | 쿼리 결과, `Flow` 갱신 |
| 마이그레이션 | 계측 | `:core:database` `src/androidTest` | 버전마다 |
| RemoteMediator | 계측 | `:core:data` `src/androidTest` | `load()`의 `MediatorResult` |
| 상태 없는 Screen | 계측 또는 Robolectric | `:feature:*:impl` | 상태별로 보이는 요소, 콜백 호출 (화면당 테스트 클래스 하나로 시작) |
| 내비게이션 흐름 | 계측 | `:app` `src/androidTest` | 시작 화면, 목록 → 상세 → 뒤로가기, 초기화 크래시 |

- 정상 흐름과 함께 경계 조건을 테스트한다: 모든 네트워크 연결 오류, 손상된 데이터(잘못된 JSON), 회전 등으로 인한 재생성.
- 프레임워크·라이브러리 자체 동작과 Activity 단위 테스트는 하지 않는다.
- 피드백을 줄 수 있는 가장 낮은 계층의 테스트를 고른다. 계측 테스트는 실제 기기 동작이 필요할 때만 쓴다.
- 커버리지를 유일한 지표로 삼지 않는다.

근거: [What to test](https://developer.android.com/training/testing/fundamentals/what-to-test), [Testing strategies](https://developer.android.com/training/testing/fundamentals/strategies): "consider the lowest layer of the pyramid that can give the team the right level of feedback", [Instrumented tests](https://developer.android.com/training/testing/instrumented-tests): "We recommend using instrumented tests only in cases where you must test against the behavior of a real device."

## 2. 테스트 대역 `필수`

mock 라이브러리 대신 인터페이스를 구현한 Fake를 쓴다. 복잡한 mock을 만들지 않는다.

| 대역 | 위치 | 쓰는 곳 |
|---|---|---|
| `FakePokemonRepository` 등 Repository Fake | `:core:testing` | ViewModel·UseCase 로컬 테스트, `:core:data-test` |
| `MainDispatcherRule`, 테스트용 외부 모델 데이터 | `:core:testing` | 로컬 테스트 |
| `FakePokemonNetworkDataSource` | `:core:data` `src/test` | Repository 로컬 테스트 |
| `TestDataModule` (Repository 바인딩을 Fake로 교체) | `:core:data-test` (`:core:testing`에 의존) | Hilt 계측 테스트 |
| `TestDispatchersModule` | `:core:testing` | Hilt 계측 테스트 |
| `CherryPokemonTestRunner` (`HiltTestApplication`) | `:core:testing` | Hilt 계측 테스트 |

```kotlin
class FakePokemonRepository @Inject constructor() : PokemonRepository {
    private val pokemonDetails = MutableSharedFlow<PokemonDetail?>(replay = 1)
    var refreshError: Throwable? = null

    fun sendPokemonDetail(detail: PokemonDetail?) = pokemonDetails.tryEmit(detail)

    override fun getPokemonDetailStream(pokeId: Int): Flow<PokemonDetail?> = pokemonDetails

    override suspend fun refreshPokemonDetail(pokeId: Int) {
        refreshError?.let { throw it }
    }
    ...
}
```

```kotlin
// :core:data-test
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DataModule::class])
internal abstract class TestDataModule {
    @Binds
    abstract fun bindsPokemonRepository(repository: FakePokemonRepository): PokemonRepository
}
```

- Fake는 테스트가 값을 넣고(`send…`) 실패를 지정(`…Error`)할 수 있게 만든다.
- 단위 테스트에서는 Hilt를 쓰지 않고 생성자에 Fake를 넘긴다. ViewModel도 직접 생성한다.
- 바인딩 교체는 테스트 폴더 전체에 적용되는 `@TestInstallIn`을 쓴다. `@UninstallModules`는 빌드 시간이 늘어나므로 꼭 필요할 때만.

근거: [Recommendations: Prefer fakes to mocks](https://developer.android.com/topic/architecture/recommendations#testing) Strongly recommended, [Test doubles](https://developer.android.com/training/testing/fundamentals/test-doubles), [Local tests](https://developer.android.com/training/testing/local-tests) Caution: "Complex mocks should be avoided", [Hilt testing](https://developer.android.com/training/dependency-injection/hilt-testing), [Modularization patterns: Test modules](https://developer.android.com/topic/modularization/patterns), NiA `core/testing`, `core/data-test`

## 3. 코루틴과 Flow `필수`

- 코루틴을 쓰는 테스트는 `runTest { }`로 감싼다.
- 디스패처를 주입받는 클래스에는 `TestDispatcher`를 넘긴다. 한 테스트의 모든 `TestDispatcher`는 같은 scheduler를 공유한다.
- 로컬 테스트에서는 `Main` 디스패처를 `TestDispatcher`로 바꾼다(`MainDispatcherRule`). 계측 테스트에서는 바꾸지 않는다.
- 실제 시간을 기다리지 않는다. 가상 시간(`advanceUntilIdle()`)을 쓴다.
- `StateFlow`는 가능하면 `value`로 확인한다. `stateIn(WhileSubscribed)`로 만든 흐름은 `backgroundScope`에서 수집자를 붙인다.
- 방출 순서를 확인해야 하면 Turbine을 쓴다.

```kotlin
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

근거: [Testing Kotlin coroutines](https://developer.android.com/kotlin/coroutines/test), [Testing Kotlin flows](https://developer.android.com/kotlin/flow/test), [Recommendations: Test StateFlows](https://developer.android.com/topic/architecture/recommendations#testing) Strongly recommended

## 4. ViewModel `필수`

[라이브러리 예외](README.md#라이브러리-예외)에 따라 `orbit-test`로 상태와 SideEffect를 순서대로 확인한다.

```kotlin
class PokemonDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pokemonRepository = FakePokemonRepository()

    @Test
    fun refresh_networkErrorWithoutCache_setsFailed() = runTest {
        pokemonRepository.refreshError = IOException()
        val viewModel = PokemonDetailViewModel(pokeId = 25, pokemonRepository = pokemonRepository)

        viewModel.test(this) {
            runOnCreate()

            expectState { copy(refreshState = RefreshState.Failed) }
        }
    }
}
```

- `onCreate`는 `runOnCreate()`를 불러야 실행된다.
- 확인하지 않은 상태나 SideEffect가 남으면 테스트가 실패한다. 모두 확인한다.

근거: [Orbit: Testing](https://orbit-mvi.org/Test/), [Hilt testing: Unit tests](https://developer.android.com/training/dependency-injection/hilt-testing)

## 5. 데이터 레이어

### 5-1. Repository `필수`

`FakePokemonNetworkDataSource`와 DAO 대역(또는 in-memory DB)으로 테스트한다.

- 조회: 로컬에 엔티티가 있으면 조회 `Flow`가 외부 모델을 내보내는지
- 갱신: 네트워크 응답이 엔티티로 바뀌어 로컬에 저장되는지
- 실패: 네트워크 예외가 그대로 전달되고 로컬 데이터가 남는지

근거: [Data layer: Testing](https://developer.android.com/topic/architecture/data-layer): "fake any dependencies that reach out to external sources", [Room testing](https://developer.android.com/training/data-storage/room/testing-db) Note: DB 자체를 테스트하지 않으면 DAO를 대체

### 5-2. DAO와 마이그레이션 `필수`

- DAO는 in-memory 데이터베이스로 테스트한다. 기기에서 돌리거나, Room KMP + `BundledSQLiteDriver`로 JVM에서 돌린다. Robolectric 로컬 테스트로 돌리지 않는다.
- 마이그레이션은 `room3-testing`의 `MigrationTestHelper`로 테스트한다.

근거: [Test and debug your database](https://developer.android.com/training/data-storage/room/testing-db): "We don't recommend Android local unit tests with Robolectric", [Migrations: Test migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions)

### 5-3. Paging `필수`

| 대상 | 방법 |
|---|---|
| `RemoteMediator` | **계측 테스트.** in-memory Room DB와 네트워크 Fake로 `load(LoadType.REFRESH, pagingState)`를 호출해 결과를 확인한다: 데이터 있음 → `Success(false)`, 빈 응답 → `Success(true)`, 예외 → `Error`. DB 저장 같은 부수 효과는 통합 테스트에서 확인한다 |
| ViewModel의 `Flow<PagingData<T>>` | `asSnapshot { scrollTo(index) }`로 목록을 꺼내 확인한다 |
| Fake Repository의 Paging | `List<T>.asPagingSourceFactory()` / `Flow<List<T>>.asPagingSourceFactory()` |
| 화면 통합 | 네트워크만 Fake로 바꾸고, `waitUntilExactlyOneExists`로 비동기 로드를 기다린다 |

```kotlin
@RunWith(AndroidJUnit4::class)
class PokemonRemoteMediatorTest {
    private val network = FakePokemonNetworkDataSource()
    private lateinit var database: CherryPokemonDatabase

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder<CherryPokemonDatabase>(ApplicationProvider.getApplicationContext())
            .build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun refresh_networkError_returnsError() = runTest {
        network.error = IOException()
        val mediator = PokemonRemoteMediator(database, network)
        val pagingState = PagingState<Int, PokemonEntity>(listOf(), null, PagingConfig(POKEMON_PAGE_SIZE), 0)

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }
}
```

근거: [Test your Paging implementation](https://developer.android.com/topic/libraries/architecture/paging/test)

## 6. UI

### 6-1. Compose UI 테스트 `필수`

- **상태 없는 Screen**에 상태를 직접 넣어 테스트한다. ViewModel과 Hilt가 필요 없다.
- 요소는 사용자에게 보이는 텍스트나 `contentDescription`으로 찾는다. 테스트 문구는 리소스에서 꺼낸다.
- `rememberSaveable` 상태 복원은 `StateRestorationTester`로 확인한다.

```kotlin
@Test
fun failedWithoutCache_showsRetry_callsOnRetryClick() {
    var retried = false
    composeTestRule.setContent {
        CherryPokemonTheme {
            PokemonDetailScreen(
                uiState = PokemonDetailUiState(pokemonDetail = null, refreshState = RefreshState.Failed),
                onRetryClick = { retried = true },
                onBackClick = {},
            )
        }
    }

    composeTestRule.onNodeWithText(retryText).performClick()

    assertTrue(retried)
}
```

근거: [Test your Compose layout](https://developer.android.com/develop/ui/compose/testing) — `MainScreen(uiState = fakeUiState)` 예시, [State saving in Compose](https://developer.android.com/develop/ui/compose/state-saving)

### 6-2. 내비게이션 테스트 `필수`

`:app`에서 `@HiltAndroidTest`와 `:core:data-test`로 앱을 띄워 시작 화면과 주요 경로를 확인한다.

근거: [Recommendations: Know what to test](https://developer.android.com/topic/architecture/recommendations#testing), [What to test: UI tests](https://developer.android.com/training/testing/fundamentals/what-to-test), [Hilt testing](https://developer.android.com/training/dependency-injection/hilt-testing)

### 6-3. 스크린샷 테스트 `권장`

공용 컴포넌트와 Screen의 모양은 Compose Preview Screenshot Testing으로 확인한다.

근거: [Testing strategies](https://developer.android.com/training/testing/fundamentals/strategies): Component tests — "Compose Preview Screenshot test"

## 7. 이름과 구성 `필수`

| 항목 | 규칙 | 근거 |
|---|---|---|
| 테스트 클래스 | `<대상>Test`, 통합 테스트는 `<대상>IntegrationTest` | [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide#naming) |
| 테스트 함수 | camelCase 조각을 밑줄로 구분: `대상_조건_결과`. 백틱과 공백을 쓰지 않는다 | 같음: "Underscores are permitted to appear in test function names", "Function names should not contain spaces" |
| 로컬 / 계측 위치 | `src/test` / `src/androidTest` | [What to test](https://developer.android.com/training/testing/fundamentals/what-to-test) |

## 8. 실행 시점 `필수`

| 테스트 | 언제 |
|---|---|
| 로컬 단위·컴포넌트 테스트 | PR마다 (CI) |
| 계측 테스트(RemoteMediator, DAO, UI, 내비게이션) | PR 머지 전 (CI, Gradle Managed Device) |

- 버그를 고치면 재현 테스트를 같은 PR에 넣는다. 테스트는 개발 초기에 작은 테스트부터 추가한다.

근거: [Testing strategies](https://developer.android.com/training/testing/fundamentals/strategies): Test infrastructure, "try to add tests as soon as possible in the development cycle"
