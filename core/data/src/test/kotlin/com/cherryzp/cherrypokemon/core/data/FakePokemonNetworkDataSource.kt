package com.cherryzp.cherrypokemon.core.data

import com.cherryzp.cherrypokemon.core.network.PokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.network.model.NetworkDamageRelations
import com.cherryzp.cherrypokemon.core.network.model.NetworkFlavorText
import com.cherryzp.cherrypokemon.core.network.model.NetworkGenus
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedEntry
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedResource
import com.cherryzp.cherrypokemon.core.network.model.NetworkOfficialArtwork
import com.cherryzp.cherrypokemon.core.network.model.NetworkOtherSprites
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonAbility
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonPage
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonSpecies
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonStat
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonTypeSlot
import com.cherryzp.cherrypokemon.core.network.model.NetworkSpeciesName
import com.cherryzp.cherrypokemon.core.network.model.NetworkSprites
import com.cherryzp.cherrypokemon.core.network.model.NetworkType

/** 테스트가 응답과 실패를 지정할 수 있는 네트워크 대역. */
class FakePokemonNetworkDataSource : PokemonNetworkDataSource {

    var error: Throwable? = null
    var page: NetworkPokemonPage = NetworkPokemonPage()
    var detail: NetworkPokemonDetail = NetworkPokemonDetail()
    var species: NetworkPokemonSpecies = NetworkPokemonSpecies(id = 0)

    /** 종 정보만 실패시킬 때 쓴다. */
    var speciesError: Throwable? = null

    /** 종 정보 호출이 몇 번 불렸는지. */
    var speciesCount: Int = 0
        private set

    override suspend fun getPokemons(offset: Int, limit: Int): NetworkPokemonPage {
        error?.let { throw it }
        return page
    }

    override suspend fun getPokemonDetail(pokeId: Int): NetworkPokemonDetail {
        error?.let { throw it }
        return detail
    }

    /** 타입·특성 이름 호출 횟수. 캐시가 듣는지 보는 데 쓴다. */
    var nameCallCount: Int = 0
        private set

    override suspend fun getType(name: String): NetworkType {
        nameCallCount++
        error?.let { throw it }
        return NetworkType(
            name = name,
            names = listOf(
                NetworkSpeciesName("풀", NetworkNamedResource(name = "ko")),
                NetworkSpeciesName("Grass", NetworkNamedResource(name = "en"))
            ),
            damageRelations = NetworkDamageRelations(
                doubleDamageFrom = listOf(NetworkNamedResource(name = "fire")),
                halfDamageFrom = listOf(NetworkNamedResource(name = "water")),
                noDamageFrom = emptyList()
            )
        )
    }

    override suspend fun getAbility(name: String): NetworkNamedEntry {
        nameCallCount++
        error?.let { throw it }
        return NetworkNamedEntry(
            name = name,
            names = listOf(
                NetworkSpeciesName("심록", NetworkNamedResource(name = "ko")),
                NetworkSpeciesName("Overgrow", NetworkNamedResource(name = "en"))
            )
        )
    }

    override suspend fun getPokemonSpecies(pokeId: Int): NetworkPokemonSpecies {
        speciesCount++
        error?.let { throw it }
        speciesError?.let { throw it }
        return species
    }

    companion object {
        fun networkPokemon(id: Int, name: String) = NetworkNamedResource(
            name = name,
            url = "https://pokeapi.co/api/v2/pokemon/$id/"
        )

        fun networkPokemonDetail(
            id: Int,
            name: String,
            typeNames: List<String> = listOf("grass"),
        ) = NetworkPokemonDetail(
            id = id,
            name = name,
            height = 7,
            weight = 69,
            types = typeNames.mapIndexed { index, typeName ->
                NetworkPokemonTypeSlot(
                    slot = index + 1,
                    type = NetworkNamedResource(name = typeName)
                )
            },
            sprites = NetworkSprites(
                other = NetworkOtherSprites(
                    officialArtwork = NetworkOfficialArtwork(
                        frontDefault = "https://example.com/$id.png"
                    )
                )
            ),
            baseExperience = 64,
            stats = listOf(
                networkStat("hp", 45),
                networkStat("attack", 49),
                networkStat("defense", 49),
                networkStat("special-attack", 65),
                networkStat("special-defense", 65),
                networkStat("speed", 45)
            ),
            abilities = listOf(
                NetworkPokemonAbility(
                    ability = NetworkNamedResource(name = "overgrow"),
                    isHidden = false,
                    slot = 1
                ),
                NetworkPokemonAbility(
                    ability = NetworkNamedResource(name = "chlorophyll"),
                    isHidden = true,
                    slot = 3
                )
            )
        )

        fun networkPokemonSpecies(
            id: Int,
            koreanName: String? = "이상해씨",
            englishName: String? = "Bulbasaur",
            koreanFlavorTexts: List<String> = listOf("오래된 설명", "등의 씨앗에\n영양을\u000c섭취한다."),
        ) = NetworkPokemonSpecies(
            id = id,
            names = listOfNotNull(
                koreanName?.let { NetworkSpeciesName(it, NetworkNamedResource(name = "ko")) },
                englishName?.let { NetworkSpeciesName(it, NetworkNamedResource(name = "en")) },
                NetworkSpeciesName("フシギダネ", NetworkNamedResource(name = "ja"))
            ),
            genera = listOf(
                NetworkGenus("씨앗포켓몬", NetworkNamedResource(name = "ko")),
                NetworkGenus("Seed Pokémon", NetworkNamedResource(name = "en"))
            ),
            flavorTextEntries = koreanFlavorTexts.map {
                NetworkFlavorText(it, NetworkNamedResource(name = "ko"))
            } + NetworkFlavorText(
                "A strange seed was planted\non its back at birth.",
                NetworkNamedResource(name = "en")
            )
        )

        private fun networkStat(name: String, value: Int) = NetworkPokemonStat(
            baseStat = value,
            stat = NetworkNamedResource(name = name)
        )
    }
}
