package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.PokemonNameEntity
import com.cherryzp.cherrypokemon.core.network.model.NetworkFormName
import com.cherryzp.cherrypokemon.core.network.model.NetworkFormNames
import com.cherryzp.cherrypokemon.core.network.model.NetworkFormWord
import com.cherryzp.cherrypokemon.core.network.model.NetworkLanguageName
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonForm
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonNames
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkPokemonNamesExtTest {

    private val species = listOf(
        NetworkLanguageName("리자몽", speciesId = 6, languageId = KOREAN),
        NetworkLanguageName("Charizard", speciesId = 6, languageId = ENGLISH),
        NetworkLanguageName("피카츄", speciesId = 25, languageId = KOREAN),
        NetworkLanguageName("Pikachu", speciesId = 25, languageId = ENGLISH),
        NetworkLanguageName("테오키스", speciesId = 386, languageId = KOREAN),
        NetworkLanguageName("Deoxys", speciesId = 386, languageId = ENGLISH),
        NetworkLanguageName("켄타로스", speciesId = 128, languageId = KOREAN),
        NetworkLanguageName("냐오닉스", speciesId = 678, languageId = KOREAN),
        NetworkLanguageName("짜랑고우거", speciesId = 784, languageId = KOREAN)
    )

    @Test
    fun asEntities_species_keepsBothLanguages() {
        val entities = NetworkPokemonNames(speciesNames = species).asEntities()

        assertEquals(
            PokemonNameEntity(id = 25, korean = "피카츄", english = "Pikachu"),
            entities.byId(25)
        )
    }

    @Test
    fun asEntities_formWithFullName_usesIt() {
        val entities = namesWith(
            form(
                10034,
                speciesId = 6,
                NetworkFormName(pokemonName = "", name = "메가리자몽X", languageId = KOREAN),
                NetworkFormName("Mega Charizard X", "Mega Charizard X", ENGLISH)
            )
        )

        // 한국어 폼 이름에 종 이름이 이미 들어 있으면 덧붙이지 않는다.
        assertEquals(
            PokemonNameEntity(id = 10034, korean = "메가리자몽X", english = "Mega Charizard X"),
            entities.byId(10034)
        )
    }

    @Test
    fun asEntities_formWithOnlyFormName_prefixesSpeciesName() {
        val entities = namesWith(
            form(
                10001,
                speciesId = 386,
                NetworkFormName(pokemonName = "", name = "어택폼", languageId = KOREAN),
                NetworkFormName("Attack Deoxys", "Attack Forme", ENGLISH)
            )
        )

        assertEquals(
            PokemonNameEntity(id = 10001, korean = "테오키스 어택폼", english = "Attack Deoxys"),
            entities.byId(10001)
        )
    }

    @Test
    fun asEntities_formWithoutKoreanName_fallsBackToSpeciesName() {
        val entities = namesWith(
            form(
                10080,
                speciesId = 25,
                NetworkFormName("Pikachu Rock Star", "Pikachu Rock Star", ENGLISH)
            )
        )

        assertEquals(
            PokemonNameEntity(id = 10080, korean = "피카츄", english = "Pikachu Rock Star"),
            entities.byId(10080)
        )
    }

    @Test
    fun asEntities_formWithoutKoreanName_buildsNameFromIdentifier() {
        val entities = NetworkPokemonNames(
            speciesNames = species,
            forms = listOf(
                form(10196, speciesId = 6, name = "charizard-gmax"),
                form(10035, speciesId = 6, name = "charizard-mega-y"),
                form(10100, speciesId = 25, name = "pikachu-hisui"),
                form(10150, speciesId = 25, name = "pikachu-totem-alola")
            )
        ).asEntities()

        assertEquals("거다이맥스 리자몽", entities.byId(10196).korean)
        assertEquals("메가리자몽Y", entities.byId(10035).korean)
        assertEquals("히스이 피카츄", entities.byId(10100).korean)
        // 주인 포켓몬이면서 알로라 모습이면 주인 쪽을 쓴다.
        assertEquals("주인 피카츄", entities.byId(10150).korean)
    }

    @Test
    fun asEntities_formWithoutKoreanName_appendsRemainingFormWord() {
        val entities = NetworkPokemonNames(
            speciesNames = species,
            forms = listOf(
                form(10326, speciesId = 678, name = "meowstic-female-mega"),
                form(10250, speciesId = 128, name = "tauros-paldea-combat-breed"),
                form(10146, speciesId = 784, name = "kommo-o-totem"),
                form(10080, speciesId = 25, name = "pikachu-rock-star")
            ),
            formWords = listOf(
                formWord("female", "암컷의 모습"),
                formWord("o", "O"),
                formWord("star", "스타컷"),
                formWord("white", "하얀 꽃"),
                formWord("white", "화이트큐레무")
            )
        ).asEntities()

        assertEquals("메가냐오닉스 암컷의 모습", entities.byId(10326).korean)
        // PokeAPI 에 없는 켄타로스 품종은 게임 표기로 채운다.
        assertEquals("팔데아 켄타로스 컴뱃종", entities.byId(10250).korean)
        // 종 식별자의 일부(`kommo-o` 의 `o`)나 뒤쪽 일부(`rock-star` 의 `star`)는 붙이지 않는다.
        assertEquals("주인 짜랑고우거", entities.byId(10146).korean)
        assertEquals("피카츄", entities.byId(10080).korean)
    }

    private fun formWord(formName: String, korean: String) = NetworkFormWord(
        formName = formName,
        names = listOf(NetworkFormName(name = korean))
    )

    private fun namesWith(form: NetworkPokemonForm) =
        NetworkPokemonNames(speciesNames = species, forms = listOf(form)).asEntities()

    private fun form(id: Int, speciesId: Int, vararg names: NetworkFormName, name: String = "") =
        NetworkPokemonForm(
            id = id,
            name = name,
            speciesId = speciesId,
            forms = listOf(NetworkFormNames(names.toList()))
        )

    private fun List<PokemonNameEntity>.byId(id: Int) = single { it.id == id }

    private companion object {
        const val KOREAN = 3
        const val ENGLISH = 9
    }
}
