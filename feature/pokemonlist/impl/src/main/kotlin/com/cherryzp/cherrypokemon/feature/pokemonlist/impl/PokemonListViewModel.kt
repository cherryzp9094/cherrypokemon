package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cherryzp.cherrypokemon.core.data.repository.PokemonRepository
import com.cherryzp.cherrypokemon.core.data.repository.UserDataRepository
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val userDataRepository: UserDataRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel(),
    OrbitContainerHost<PokemonListUiState, PokemonListUiState, PokemonListSideEffect> {

    // 검색어는 프로세스가 죽었다 살아나도 남아야 한다. 회전만 버티는 메모리 값으로는 모자라다.
    private val query = savedStateHandle.getMutableStateFlow(QUERY_KEY, "")

    override val container = orbitContainer<PokemonListUiState, PokemonListSideEffect>(
        initialState = PokemonListUiState(query = query.value)
    ) {
        coroutineScope {
            launch { observeLanguage() }
            launch { fillIndexOnFirstSearch() }
            launch { fillNames() }
        }
    }

    /**
     * 모든 포켓몬의 한국어·영어 이름을 받아 둔다. 처음 한 번만 받는다.
     *
     * 받지 못해도 목록은 식별자로 보이므로 실패를 삼킨다. 다음에 화면을 열 때 다시 해 본다.
     */
    private suspend fun fillNames() {
        runCatching { pokemonRepository.ensurePokemonNames() }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val pokemons: Flow<PagingData<Pokemon>> = query
        // 글자를 칠 때마다 새 Pager 를 만들지 않는다.
        .debounce { if (it.isEmpty()) 0 else SEARCH_DEBOUNCE_MILLIS }
        .distinctUntilChanged()
        .flatMapLatest { text ->
            if (text.isBlank()) {
                pokemonRepository.getPokemonsStream()
            } else {
                pokemonRepository.searchPokemonsStream(text.trim())
            }
        }
        .cachedIn(viewModelScope)

    fun setLanguage(language: AppLanguage) = intent {
        userDataRepository.setLanguage(language)
    }

    /**
     * 검색어를 바꾼다.
     *
     * 입력란이 이 상태를 그대로 보여주므로 끝날 때까지 기다린다. 비동기로 두면 빨리 칠 때
     * 상태가 한 박자 늦게 돌아와 글자가 빠지거나 커서가 튄다. 그래서 여기서는 네트워크를 부르지 않는다.
     */
    fun setQuery(text: String) = blockingIntent {
        reduce { state.copy(query = text) }
        query.value = text
    }

    @OptIn(OrbitExperimental::class)
    private suspend fun observeLanguage() = subIntent {
        repeatOnSubscription {
            userDataRepository.userData.collect { userData ->
                val language = userData.language ?: return@collect
                reduce { state.copy(language = language) }
            }
        }
    }

    /**
     * 처음 검색할 때 전체 이름 색인을 한 번 받는다.
     *
     * 검색은 로컬에서만 하므로 색인이 있어야 한다. 받는 동안 친 글자는 [query] 가 하나로 합쳐
     * 요청이 겹치지 않는다. 받지 못하면 다음 검색어에서 다시 해 본다. 그동안은 지금까지 받아 둔
     * 범위에서 찾는다.
     */
    private suspend fun fillIndexOnFirstSearch() {
        query
            .filter { it.isNotBlank() }
            .first { runCatching { pokemonRepository.ensurePokemonIndex() }.isSuccess }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 250L
        const val QUERY_KEY = "query"
    }
}
