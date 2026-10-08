package com.cherryzp.cherrypokemon.feature.pokemondetail.impl

import com.cherryzp.cherrypokemon.core.testing.data.testPokemonDetail
import com.cherryzp.cherrypokemon.core.testing.repository.FakePokemonRepository
import com.cherryzp.cherrypokemon.core.testing.util.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test

class PokemonDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pokemonRepository = FakePokemonRepository()

    @Test
    fun onCreate_refreshSucceeds_showsDetailFromLocalStream() = runTest {
        val viewModel = PokemonDetailViewModel(pokeId = 1, pokemonRepository = pokemonRepository)

        viewModel.test(this) {
            runOnCreate()
            expectState { copy(refreshState = RefreshState.Idle) }

            // 화면에 보이는 값은 네트워크 응답이 아니라 로컬 스트림에서 온다.
            pokemonRepository.sendPokemonDetail(testPokemonDetail)
            expectState { copy(pokemonDetail = testPokemonDetail) }

            cancelAndIgnoreRemainingItems()
        }

        assertEquals(1, pokemonRepository.refreshCount)
    }

    @Test
    fun onCreate_networkErrorWithoutCache_setsFailed() = runTest {
        pokemonRepository.refreshError = IOException()
        val viewModel = PokemonDetailViewModel(pokeId = 1, pokemonRepository = pokemonRepository)

        viewModel.test(this) {
            runOnCreate()

            expectState { copy(refreshState = RefreshState.Failed) }
            // 캐시가 없으면 메시지 대신 에러 화면을 보여주므로 SideEffect 가 없다.
            expectNoItems()

            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun retry_networkErrorWithCache_keepsCacheAndSendsMessage() = runTest {
        val viewModel = PokemonDetailViewModel(pokeId = 1, pokemonRepository = pokemonRepository)

        viewModel.test(this) {
            runOnCreate()
            expectState { copy(refreshState = RefreshState.Idle) }
            pokemonRepository.sendPokemonDetail(testPokemonDetail)
            expectState { copy(pokemonDetail = testPokemonDetail) }

            pokemonRepository.refreshError = IOException()
            viewModel.retry()

            expectState { copy(refreshState = RefreshState.Refreshing) }
            expectState { copy(refreshState = RefreshState.Failed) }
            expectSideEffect(
                PokemonDetailSideEffect.ShowMessage(
                    R.string.feature_pokemondetail_impl_error_refresh
                )
            )

            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun retry_afterFailure_refreshesAgain() = runTest {
        pokemonRepository.refreshError = IOException()
        val viewModel = PokemonDetailViewModel(pokeId = 1, pokemonRepository = pokemonRepository)

        viewModel.test(this) {
            runOnCreate()
            expectState { copy(refreshState = RefreshState.Failed) }

            pokemonRepository.refreshError = null
            viewModel.retry()

            expectState { copy(refreshState = RefreshState.Refreshing) }
            expectState { copy(refreshState = RefreshState.Idle) }

            cancelAndIgnoreRemainingItems()
        }

        assertEquals(2, pokemonRepository.refreshCount)
    }
}
