package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource.Companion.networkPokemon
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource.Companion.networkPokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedResource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkPokemonExtTest {

    @Test
    fun asEntity_url_parsesIdAndBuildsImageUrl() {
        val entity = networkPokemon(id = 25, name = "pikachu").asEntity()

        assertEquals(25, entity?.id)
        assertEquals("pikachu", entity?.name)
        assertEquals(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png",
            entity?.imageUrl
        )
    }

    @Test
    fun asEntity_urlWithoutId_returnsNull() {
        assertNull(
            NetworkNamedResource(
                name = "mew",
                url = "https://pokeapi.co/api/v2/pokemon/"
            ).asEntity()
        )
        assertNull(NetworkNamedResource(name = "mew", url = "").asEntity())
    }

    @Test
    fun asEntity_detail_convertsUnits() {
        val entity = networkPokemonDetail(id = 1, name = "bulbasaur").asEntity()

        // PokeAPI 는 데시미터와 헥토그램으로 준다.
        assertEquals(0.7f, entity.heightMeters)
        assertEquals(6.9f, entity.weightKilograms)
    }

    @Test
    fun asEntity_detail_keepsTypeOrderAndFallsBackToUnknown() {
        val entity = networkPokemonDetail(
            id = 1,
            name = "bulbasaur",
            typeNames = listOf("grass", "poison", "mystery")
        ).asEntity()

        assertEquals(
            listOf(PokemonType.Grass, PokemonType.Poison, PokemonType.Unknown),
            entity.types
        )
    }
}
