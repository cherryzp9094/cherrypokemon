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
    fun asEntities_locationAndItemDetails_prefersItem() {
        // 리피아: 옛 게임은 장소(이끼바위 근처), 최신 게임은 리프의돌. 장소는 게임마다 다르다.
        val leafeon = withDetails(
            470,
            "leafeon",
            NetworkEvolutionDetail(
                trigger = resource("level-up"),
                location = resource("eterna-forest")
            ),
            NetworkEvolutionDetail(trigger = resource("use-item"), item = resource("leaf-stone"))
        )

        assertEquals("leaf-stone", leafeon.item)
        assertNull(leafeon.location)
    }

    @Test
    fun asEntities_regionalFormDetail_isSkipped() {
        // 야도킹: 두 번째 조건은 갈라르 야도킹으로 진화하는 것이라 기본 모습 체인과 맞지 않는다.
        val slowking = withDetails(
            199,
            "slowking",
            NetworkEvolutionDetail(trigger = resource("trade"), heldItem = resource("kings-rock")),
            NetworkEvolutionDetail(
                trigger = resource("use-item"),
                item = resource("galarica-wreath"),
                evolvedPokemonForm = resource("slowking-galar")
            )
        )

        assertEquals("trade", slowking.trigger)
        assertEquals("kings-rock", slowking.heldItem)
        assertNull(slowking.item)
    }

    @Test
    fun asEntities_friendshipTimeAndMoveType_areKept() {
        val umbreon = withDetails(
            197,
            "umbreon",
            NetworkEvolutionDetail(
                trigger = resource("level-up"),
                minHappiness = 160,
                timeOfDay = "night"
            )
        )
        val sylveon = withDetails(
            700,
            "sylveon",
            NetworkEvolutionDetail(
                trigger = resource("level-up"),
                minAffection = 2,
                knownMoveType = resource("fairy")
            )
        )

        assertEquals(160, umbreon.minHappiness)
        assertEquals("night", umbreon.timeOfDay)
        assertEquals(2, sylveon.minAffection)
        // 타입은 화면이 이름을 붙일 수 있게 PokemonType 이름으로 둔다.
        assertEquals("Fairy", sylveon.knownMoveType)
        assertNull(sylveon.timeOfDay)
    }

    @Test
    fun asEntities_idMissingFromUrl_dropsThatMember() {
        val entities = chain(
            NetworkChainLink(species = NetworkNamedResource(name = "mew", url = ""))
        ).asEntities()

        assertTrue(entities.isEmpty())
    }

    private fun chain(root: NetworkChainLink) = NetworkEvolutionChain(id = 1, chain = root)

    /** 이브이에서 한 단계 진화하는 [id] 의 구성원을 만든다. */
    private fun withDetails(id: Int, name: String, vararg details: NetworkEvolutionDetail) = chain(
        link(133, "eevee").copy(
            evolvesTo = listOf(
                link(id, name).copy(evolutionDetails = details.toList())
            )
        )
    ).asEntities().single { it.pokemonId == id }

    private fun resource(name: String) = NetworkNamedResource(name = name)

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
