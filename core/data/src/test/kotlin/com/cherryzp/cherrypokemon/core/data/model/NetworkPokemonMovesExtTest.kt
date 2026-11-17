package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource.Companion.pokemonMove
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonMoves
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkPokemonMovesExtTest {

    @Test
    fun asEntities_usesNewestMainGameOnly() {
        val entities = NetworkPokemonMoves(
            moves = listOf(
                pokemonMove("x-y", 1, "tackle", "몸통박치기", "normal", power = 40),
                pokemonMove("ultra-sun-ultra-moon", 1, "ember", "불꽃세례", "fire", power = 40),
                pokemonMove("ultra-sun-ultra-moon", 9, "smokescreen", "연막", "normal", power = null)
            )
        ).asEntities(pokemonId = 10034)

        // 메가리자몽X 는 스칼렛·바이올렛에 없다. 그다음 최신인 울트라썬·울트라문 것을 쓴다.
        assertEquals(setOf("ember", "smokescreen"), entities.moves.map { it.name }.toSet())
        assertEquals(listOf(1, 9), entities.pokemonMoves.map { it.level }.sorted())
        assertTrue(entities.pokemonMoves.all { it.pokemonId == 10034 })
        assertEquals(PokemonType.Fire.name, entities.moves.single { it.name == "ember" }.type)
    }

    @Test
    fun asEntities_noMainGame_returnsNothing() {
        val entities = NetworkPokemonMoves(
            moves = listOf(pokemonMove("legends-za", 1, "tackle", "몸통박치기", "normal", power = 40))
        ).asEntities(pokemonId = 1)

        assertTrue(entities.moves.isEmpty())
        assertTrue(entities.pokemonMoves.isEmpty())
    }
}
