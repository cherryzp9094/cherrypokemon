package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.PokemonFilterEntity
import com.cherryzp.cherrypokemon.core.network.model.NetworkFilterSpecies
import com.cherryzp.cherrypokemon.core.network.model.NetworkGraphQlName
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonFilter
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonFilters
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonTypeSlotName
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkPokemonFiltersExtTest {

    @Test
    fun asEntities_keepsTypesInSlotOrderAndGeneration() {
        val entities = NetworkPokemonFilters(
            pokemons = listOf(
                filter(10034, generation = 1, "dragon" to 2, "fire" to 1),
                filter(25, generation = 1, "electric" to 1)
            )
        ).asEntities()

        assertEquals(
            listOf(
                PokemonFilterEntity(id = 10034, type1 = "Fire", type2 = "Dragon", generation = 1),
                PokemonFilterEntity(id = 25, type1 = "Electric", type2 = null, generation = 1)
            ),
            entities
        )
    }

    @Test
    fun asEntities_withoutGenerationOrType_isDropped() {
        val entities = NetworkPokemonFilters(
            pokemons = listOf(
                filter(1, generation = null, "grass" to 1),
                NetworkPokemonFilter(id = 2, species = NetworkFilterSpecies(generation = 1))
            )
        ).asEntities()

        assertEquals(emptyList<PokemonFilterEntity>(), entities)
    }

    private fun filter(id: Int, generation: Int?, vararg types: Pair<String, Int>) =
        NetworkPokemonFilter(
            id = id,
            types = types.map { (name, slot) ->
                NetworkPokemonTypeSlotName(slot, NetworkGraphQlName(name))
            },
            species = NetworkFilterSpecies(generation = generation)
        )
}
