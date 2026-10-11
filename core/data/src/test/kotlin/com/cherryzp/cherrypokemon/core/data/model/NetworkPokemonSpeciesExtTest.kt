package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource.Companion.networkPokemonSpecies
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkPokemonSpeciesExtTest {

    @Test
    fun localizedName_picksKoreanAndEnglish() {
        val name = networkPokemonSpecies(id = 1).localizedName()

        assertEquals("이상해씨", name.korean)
        assertEquals("Bulbasaur", name.english)
    }

    @Test
    fun localizedName_koreanMissing_leavesItEmpty() {
        val name = networkPokemonSpecies(id = 1, koreanName = null).localizedName()

        // 비어 있으면 화면이 다른 언어로 떨어뜨린다.
        assertEquals("", name.korean)
        assertEquals("Bulbasaur", name.english)
    }

    @Test
    fun localizedGenus_picksBothLanguages() {
        val genus = networkPokemonSpecies(id = 1).localizedGenus()

        assertEquals("씨앗포켓몬", genus.korean)
        assertEquals("Seed Pokémon", genus.english)
    }

    @Test
    fun localizedFlavorText_takesLatestAndRemovesGameLineBreaks() {
        val flavorText = networkPokemonSpecies(id = 1).localizedFlavorText()

        assertEquals("등의 씨앗에 영양을 섭취한다.", flavorText.korean)
        assertEquals("A strange seed was planted on its back at birth.", flavorText.english)
    }

    @Test
    fun localizedFlavorText_noEntries_isEmpty() {
        val flavorText = networkPokemonSpecies(id = 1, koreanFlavorTexts = emptyList())
            .localizedFlavorText()

        assertEquals("", flavorText.korean)
    }
}
