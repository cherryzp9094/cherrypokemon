package com.cherryzp.cherrypokemon.feature.pokemondetail.api

import androidx.navigation3.runtime.NavKey
import com.cherryzp.cherrypokemon.core.navigation.Navigator
import kotlinx.serialization.Serializable

/** 상세 화면. 도감 번호만 넘긴다. 표시용 값은 화면이 직접 불러온다. */
@Serializable
data class PokemonDetailNavKey(val pokeId: Int) : NavKey

fun Navigator.navigateToPokemonDetail(pokeId: Int) {
    navigate(PokemonDetailNavKey(pokeId))
}
