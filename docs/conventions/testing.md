# 테스트

> 코드 예시는 규칙을 보여주기 위한 것이다. 라이브러리 버전에 따라 API 이름이 다를 수 있으니 작성할 때 해당 버전 문서를 확인한다.

## 1. 무엇을 테스트하나 `필수`

| 대상 | 종류 | 위치 | 확인할 것 |
|---|---|---|---|
| ViewModel | 로컬 | `:feature:*:impl` `src/test` | 초기 상태, 데이터에 따른 상태 변화, 갱신 실패 시 상태·SideEffect, 재시도 |
| UseCase | 로컬 | `:core:domain` `src/test` | 조합 로직 |
| Repository | 로컬 | `:core:data` `src/test` | 로컬 데이터를 도메인 모델로 내보내는지, 갱신이 로컬에 저장되는지, 실패가 전달되는지 |
| 모델 변환 (`asEntity`, `asExternalModel`) | 로컬 | 변환 함수가 있는 모듈 `src/test` | 빠진 필드, 기본값 |
| 네트워크 모델 파싱 | 로컬 | `:core:network` `src/test` | 실제 응답 JSON(`src/test/resources`)이 모델로 파싱되는지 |
| DAO | 계측 | `:core:database` `src/androidTest` | 쿼리 결과, `Flow` 갱신, upsert |
| 마이그레이션 | 계측 | `:core:database` `src/androidTest` | 버전마다 `MigrationTestHelper` |
| RemoteMediator | 계측 | `:core:data` `src/androidTest` | `REFRESH` / `APPEND` 결과, 마지막 페이지, 네트워크 에러 시 `MediatorResult.Error` |
| 상태 없는 Screen | 계측 | `:feature:*:impl` `src/androidTest` | 상태별로 보이는 요소, 콜백 호출 |
| 내비게이션 흐름 | 계측 | `:app` `src/androidTest` | 목록 → 상세 → 뒤로가기 |

- 정상 흐름만 테스트하지 않는다. 네트워크 에러, 잘못된 응답, 빈 데이터, 캐시만 있는 상태를 같이 확인한다.
- 프레임워크와 라이브러리 자체의 동작은 테스트하지 않는다.

근거: [What to test](https://developer.android.com/training/testing/fundamentals/what-to-test), [Recommendations: Testing](https://developer.android.com/topic/architecture/recommendations#testing)

## 2. 테스트 대역 `필수`

mock 라이브러리를 쓰지 않고, 인터페이스를 구현한 Fake를 쓴다.

| 대역 | 위치 | 쓰는 곳 |
|---|---|---|
| `FakePokemonRepository` 등 Repository Fake | `:core:testing` | ViewModel·UseCase 로컬 테스트 |
| `MainDispatcherRule`, 테스트용 도메인 모델 데이터 | `:core:testing` | 모든 로컬 테스트 |
| `FakePokemonNetworkDataSource`, Fake DAO | `:core:data` `src/test` | Repository 로컬 테스트 |
| Repository 바인딩을 Fake로 바꾸는 `TestDataModule` | `:core:data-test` | Hilt 계측 테스트 |
| 테스트 디스패처를 주입하는 `TestDispatchersModule` | `:core:testing` | Hilt 계측 테스트 |
| `CherryPokemonTestRunner` (`HiltTestApplication` 사용) | `:core:testing` | Hilt 계측 테스트 |

```kotlin
class FakePokemonRepository : PokemonRepository {
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
internal interface TestDataModule {
    @Binds
    fun bindsPokemonRepository(repository: FakePokemonRepository): PokemonRepository
}
```

- 이름은 `Fake<인터페이스>`다.
- Fake는 테스트가 값을 넣고(`send…`) 실패를 지정(`…Error`)할 수 있게 만든다.

근거: [Test doubles](https://developer.android.com/training/testing/fundamentals/test-doubles), [Recommendations: Prefer fakes to mocks](https://developer.android.com/topic/architecture/recommendations#testing), [Hilt testing: Replace bindings](https://developer.android.com/training/dependency-injection/hilt-testing), NiA `core/testing`, `core/data-test`

## 3. 코루틴 `필수`

- 테스트 함수는 `runTest { }`로 감싼다.
- 디스패처를 주입받는 클래스에는 `StandardTestDispatcher(testScheduler)` 또는 `UnconfinedTestDispatcher(testScheduler)`를 넘긴다.
- `viewModelScope`를 쓰는 테스트는 `:core:testing`의 `MainDispatcherRule`을 쓴다.
- 실제 시간을 기다리지 않는다. `advanceUntilIdle()` 같은 가상 시간을 쓴다.
- `Flow`의 방출 순서는 Turbine으로 확인한다.

```kotlin
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

근거: [Testing Kotlin coroutines on Android](https://developer.android.com/kotlin/coroutines/test), [Coroutines best practices: Inject TestDispatchers](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)

## 4. ViewModel `필수`

[라이브러리 예외](README.md#라이브러리-예외)에 따라 `orbit-test`로 상태와 SideEffect를 순서대로 확인한다.

```kotlin
class PokemonDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pokemonRepository = FakePokemonRepository()

    @Test
    fun `갱신에 실패하면 에러 메시지를 보낸다`() = runTest {
        pokemonRepository.refreshError = IOException()
        val viewModel = PokemonDetailViewModel(pokeId = 25, pokemonRepository = pokemonRepository)

        viewModel.test(this) {
            runOnCreate()

            expectState { copy(isRefreshing = true) }
            expectSideEffect(PokemonDetailSideEffect.ShowMessage(R.string.feature_pokemondetail_impl_error_network))
            expectState { copy(isRefreshing = false) }
        }
    }
}
```

- `onCreate`는 `runOnCreate()`를 불러야 실행된다.
- 확인하지 않은 상태나 SideEffect가 남으면 테스트가 실패한다. 모두 확인한다.

근거: [Orbit: Testing](https://orbit-mvi.org/Test/)

## 5. 데이터 레이어

### 5-1. Repository `필수`

Fake DAO와 `FakePokemonNetworkDataSource`로 로컬 테스트한다.

- 조회: DAO에 엔티티를 넣으면 조회 `Flow`가 도메인 모델을 내보내는지
- 갱신: 네트워크 Fake의 응답이 엔티티로 바뀌어 DAO에 저장되는지
- 실패: 네트워크 Fake가 던진 예외가 그대로 전달되고 로컬 데이터가 남아 있는지

### 5-2. DAO와 마이그레이션 `필수`

- DAO는 `Room.inMemoryDatabaseBuilder`로 만든 데이터베이스로 계측 테스트한다.
- 마이그레이션은 `MigrationTestHelper`로 이전 버전 스키마에서 옮긴 뒤 데이터를 확인한다.

근거: [Test and debug your database](https://developer.android.com/training/data-storage/room/testing-db), [Test migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions#test)

### 5-3. Paging `필수`

| 대상 | 방법 |
|---|---|
| `RemoteMediator` | **계측 테스트.** 메모리 Room 데이터베이스와 네트워크 Fake로 `load(LoadType.REFRESH, pagingState)`를 불러 `MediatorResult`를 확인한다 |
| ViewModel의 `Flow<PagingData<T>>` | `asSnapshot { scrollTo(index) }`로 목록을 꺼내 확인한다 |
| Fake Repository의 Paging | `List<T>.asPagingSourceFactory()`로 만든다 |

```kotlin
@RunWith(AndroidJUnit4::class)
class PokemonRemoteMediatorTest {
    private val database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        CherryPokemonDatabase::class.java,
    ).build()
    private val network = FakePokemonNetworkDataSource()

    @Test
    fun networkError_refresh_returnsError() = runTest {
        network.error = IOException()
        val mediator = PokemonRemoteMediator(database, network)
        val pagingState = PagingState<Int, PokemonEntity>(listOf(), null, PagingConfig(POKEMON_PAGE_SIZE), 0)

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }
}
```

근거: [Test your Paging implementation](https://developer.android.com/topic/libraries/architecture/paging/test)

### 5-4. 네트워크 모델 `권장`

PokeAPI 실제 응답을 `src/test/resources`에 JSON으로 저장하고, 네트워크 모델로 파싱되는지 확인한다. API 응답 형식이 바뀌었을 때 가장 먼저 드러난다.

## 6. Compose UI `필수`

- **상태 없는 Screen**을 테스트한다. 상태를 직접 넣으므로 ViewModel과 Hilt가 필요 없다.
- 요소는 사용자에게 보이는 텍스트나 `contentDescription`으로 찾는다. `testTag`는 그걸로 찾을 수 없을 때만 쓴다.
- 문자열은 리소스에서 꺼내 비교한다. 테스트 코드에 화면 문구를 쓰지 않는다.
- 내비게이션 흐름 테스트는 `:app`에서 `@HiltAndroidTest`와 `:core:data-test`로 전체 앱을 띄워 확인한다.

```kotlin
@Test
fun noCachedDetail_notRefreshing_showsRetryButton() {
    var retried = false
    composeTestRule.setContent {
        CherryPokemonTheme {
            PokemonDetailScreen(
                uiState = PokemonDetailUiState(pokemonDetail = null, isRefreshing = false),
                onRetryClick = { retried = true },
                onBackClick = {},
            )
        }
    }

    val retryText = InstrumentationRegistry.getInstrumentation().targetContext
        .getString(R.string.feature_pokemondetail_impl_retry)
    composeTestRule.onNodeWithText(retryText).performClick()

    assertTrue(retried)
}
```

근거: [Testing your Compose layout](https://developer.android.com/develop/ui/compose/testing), [Hilt testing](https://developer.android.com/training/dependency-injection/hilt-testing)

## 7. 이름과 구성

| 항목 | 규칙 | 강도 |
|---|---|---|
| 테스트 클래스 | `<대상>Test` | `필수` |
| 로컬 테스트 함수 | 백틱 안에 한국어 문장 (`` `갱신에 실패하면 에러 메시지를 보낸다` ``) | `권장` |
| 계측 테스트 함수 | `조건_동작_결과` camelCase + 밑줄. Android는 API 30 전까지 함수 이름의 공백을 지원하지 않는다 | `필수` |
| 본문 | 준비 / 실행 / 확인을 빈 줄로 나눈다 | `권장` |
| 범위 | 테스트 하나는 동작 하나를 확인한다 | `권장` |

근거: [Kotlin coding conventions: Names for test methods](https://kotlinlang.org/docs/coding-conventions.html#names-for-test-methods), [Android Kotlin style guide: Naming](https://developer.android.com/kotlin/style-guide#naming)

## 8. 버그와 테스트 `필수`

- 버그를 고치면 그 버그를 재현하는 테스트를 같은 PR에 넣는다. 커밋은 `fix`와 `test`로 나눈다.
- 기능을 추가하면 1번 표에 해당하는 테스트를 같은 PR에 넣는다.
