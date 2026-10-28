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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import org.orbitmvi.orbit.OrbitContainerHost
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
        repeatOnSubscription {
            userDataRepository.userData.collect { userData ->
                val language = userData.language ?: return@collect
                reduce { state.copy(language = language) }
            }
        }
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
     * 검색은 로컬에서만 하므로, 처음 검색할 때 전체 이름 색인을 받아 둔다.
     * 색인을 받지 못해도 지금까지 받아 둔 범위에서는 찾을 수 있으므로 실패를 삼킨다.
     */
    fun setQuery(text: String) = intent {
        reduce { state.copy(query = text) }
        query.value = text
        if (text.isNotBlank()) {
            runCatching { pokemonRepository.ensurePokemonIndex() }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 250L
        const val QUERY_KEY = "query"
    }
}
