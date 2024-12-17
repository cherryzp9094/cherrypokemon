package com.cherryzp.domain.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.cherryzp.domain.repository.PokemonRepository
import javax.inject.Inject

class PokemonListUseCase @Inject constructor(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke() = pokemonRepository.fetchPokemonList()
}