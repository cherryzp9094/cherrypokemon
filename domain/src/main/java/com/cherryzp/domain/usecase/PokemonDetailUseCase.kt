package com.cherryzp.domain.usecase

import com.cherryzp.domain.model.PokemonDetail
import com.cherryzp.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PokemonDetailUseCase @Inject constructor(
    private val pokemonRepository: PokemonRepository
) {
    operator fun invoke(
        pokeNo: Int
    ): Flow<PokemonDetail> =
        flow { emit(pokemonRepository.fetchPokemonDetail(pokeNo)) }

}