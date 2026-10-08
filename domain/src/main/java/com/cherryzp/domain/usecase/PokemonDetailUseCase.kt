package com.cherryzp.domain.usecase

import com.cherryzp.domain.model.PokemonDetail
import com.cherryzp.domain.repository.PokemonRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PokemonDetailUseCase @Inject constructor(private val pokemonRepository: PokemonRepository) {
    operator fun invoke(pokeNo: Int): Flow<PokemonDetail> =
        flow { emit(pokemonRepository.fetchPokemonDetail(pokeNo)) }
}
