package com.cherryzp.cherrypokemon.core.network

import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonMovesResponse
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonNamesResponse
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonPage
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** PokeAPI 의 실제 응답으로 네트워크 모델을 검증한다. */
class NetworkPokemonSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun pokemonPage_realResponse_parsesResults() {
        val page = json.decodeFromString<NetworkPokemonPage>(readResource("pokemons.json"))

        assertEquals(1351, page.count)
        assertEquals(3, page.results.size)
        assertEquals("bulbasaur", page.results.first().name)
        assertEquals("https://pokeapi.co/api/v2/pokemon/1/", page.results.first().url)
    }

    @Test
    fun pokemonNames_realGraphQlResponse_parsesSpeciesAndForms() {
        val names = json.decodeFromString<NetworkPokemonNamesResponse>(
            readResource("pokemon_names.json")
        ).data

        assertEquals(
            setOf("피카츄", "Pikachu"),
            names?.speciesNames?.filter { it.speciesId == 25 }?.map { it.name }?.toSet()
        )
        val megaCharizard = names?.forms?.single { it.id == 10034 }
        assertEquals(6, megaCharizard?.speciesId)
        assertEquals(
            "메가리자몽X",
            megaCharizard?.forms?.single()?.names?.single { it.languageId == 3 }?.name
        )
    }

    @Test
    fun pokemonNames_graphQlError_hasNoData() {
        val response = json.decodeFromString<NetworkPokemonNamesResponse>(
            """{"errors":[{"message":"field not found"}]}"""
        )

        // 질의가 틀리면 HTTP 200 에 data 없이 온다. 네트워크 쪽이 예외로 바꾼다.
        assertEquals(null, response.data)
    }

    @Test
    fun pokemonMoves_realGraphQlResponse_parsesMoveDetails() {
        val moves = json.decodeFromString<NetworkPokemonMovesResponse>(
            readResource("pokemon_moves.json")
        ).data?.moves.orEmpty()

        val first = moves.first()
        assertEquals("scarlet-violet", first.versionGroup.name)
        assertEquals("electric", moves.first { it.move.name == "thunder-shock" }.move.type?.name)
        assertEquals(
            "전기쇼크",
            moves.first { it.move.name == "thunder-shock" }.move.names
                .single { it.languageId == 3 }.name
        )
    }

    @Test
    fun pokemonDetail_realResponse_parsesUsedFields() {
        val detail = json.decodeFromString<NetworkPokemonDetail>(
            readResource("pokemon_detail.json")
        )

        assertEquals(1, detail.id)
        assertEquals("bulbasaur", detail.name)
        assertEquals(7, detail.height)
        assertEquals(69, detail.weight)
        assertEquals(listOf("grass", "poison"), detail.types.map { it.type.name })
        assertEquals(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
            detail.sprites.other?.officialArtwork?.frontDefault
        )
    }

    @Test
    fun pokemonDetail_missingFields_usesDefaults() {
        val detail = json.decodeFromString<NetworkPokemonDetail>("""{"id":25,"name":"pikachu"}""")

        assertEquals(0, detail.height)
        assertEquals(emptyList<Any>(), detail.types)
        assertEquals(null, detail.sprites.other)
    }

    @Test
    fun pokemonDetail_brokenJson_throws() {
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<NetworkPokemonDetail>("""{"id":1,"name":""")
        }
    }

    private fun readResource(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) { "$name 이 없다" }
            .bufferedReader()
            .use { it.readText() }
}
