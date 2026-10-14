package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.network.model.NetworkChainLink
import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionChain
import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedResource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkEvolutionChainExtTest {

    @Test
    fun asEntities_threeStageChain_isFlattenedInOrder() {
        val entities = chain(
            link(
                1,
                "bulbasaur",
                evolvesTo = listOf(
                    link(
                        2,
                        "ivysaur",
                        minLevel = 16,
                        evolvesTo = listOf(
                            link(3, "venusaur", minLevel = 32)
                        )
                    )
                )
            )
        ).asEntities()

        assertEquals(listOf(1, 2, 3), entities.map { it.pokemonId })
        assertEquals(listOf(1, 2, 3), entities.map { it.stage })
        assertNull(entities.first().minLevel)
        assertEquals(16, entities[1].minLevel)
    }

    @Test
    fun asEntities_branchingChain_keepsEveryBranchOnTheSameStage() {
        val entities = chain(
            link(
                133,
                "eevee",
                evolvesTo = listOf(
                    link(134, "vaporeon", item = "water-stone"),
                    link(135, "jolteon", item = "thunder-stone"),
                    link(136, "flareon", item = "fire-stone")
                )
            )
        ).asEntities()

        assertEquals(4, entities.size)
        assertEquals(listOf(1, 2, 2, 2), entities.map { it.stage })
        assertEquals("water-stone", entities[1].item)
    }

    @Test
    fun asEntities_idMissingFromUrl_dropsThatMember() {
        val entities = chain(
            NetworkChainLink(species = NetworkNamedResource(name = "mew", url = ""))
        ).asEntities()

        assertTrue(entities.isEmpty())
    }

    private fun chain(root: NetworkChainLink) = NetworkEvolutionChain(id = 1, chain = root)

    private fun link(
        id: Int,
        name: String,
        minLevel: Int? = null,
        item: String? = null,
        evolvesTo: List<NetworkChainLink> = emptyList(),
    ) = NetworkChainLink(
        species = NetworkNamedResource(
            name = name,
            url = "https://pokeapi.co/api/v2/pokemon-species/$id/"
        ),
        evolutionDetails = if (minLevel == null && item == null) {
            emptyList()
        } else {
            listOf(
                NetworkEvolutionDetail(
                    minLevel = minLevel,
                    item = item?.let { NetworkNamedResource(name = it) }
                )
            )
        },
        evolvesTo = evolvesTo
    )
}
