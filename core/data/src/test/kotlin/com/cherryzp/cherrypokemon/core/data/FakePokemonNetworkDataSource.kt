package com.cherryzp.cherrypokemon.core.data

import com.cherryzp.cherrypokemon.core.network.PokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.network.model.NetworkAbility
import com.cherryzp.cherrypokemon.core.network.model.NetworkChainLink
import com.cherryzp.cherrypokemon.core.network.model.NetworkDamageRelations
import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionChain
import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkFlavorText
import com.cherryzp.cherrypokemon.core.network.model.NetworkGenus
import com.cherryzp.cherrypokemon.core.network.model.NetworkGraphQlLanguageName
import com.cherryzp.cherrypokemon.core.network.model.NetworkGraphQlName
import com.cherryzp.cherrypokemon.core.network.model.NetworkMove
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedEntry
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedResource
import com.cherryzp.cherrypokemon.core.network.model.NetworkOfficialArtwork
import com.cherryzp.cherrypokemon.core.network.model.NetworkOtherSprites
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonAbility
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonMove
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonMoves
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonNames
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonPage
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonSpecies
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonStat
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonTypeSlot
import com.cherryzp.cherrypokemon.core.network.model.NetworkSpeciesName
import com.cherryzp.cherrypokemon.core.network.model.NetworkSprites
import com.cherryzp.cherrypokemon.core.network.model.NetworkType
import kotlinx.coroutines.delay

/** 테스트가 응답과 실패를 지정할 수 있는 네트워크 대역. */
class FakePokemonNetworkDataSource : PokemonNetworkDataSource {

    var error: Throwable? = null
    var page: NetworkPokemonPage = NetworkPokemonPage()
    var detail: NetworkPokemonDetail = NetworkPokemonDetail()
    var species: NetworkPokemonSpecies = NetworkPokemonSpecies(id = 0)

    /** 상세·종·타입·특성·체인 호출마다 걸리는 시간. 병렬로 부르는지 보는 데 쓴다. */
    var callDelayMillis: Long = 0

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
        delay(callDelayMillis)
        error?.let { throw it }
        return detail
    }

    /** 타입·특성 이름 호출 횟수. 캐시가 듣는지 보는 데 쓴다. */
    var nameCallCount: Int = 0
        private set

    override suspend fun getType(name: String): NetworkType {
        delay(callDelayMillis)
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

    override suspend fun getAbility(name: String): NetworkAbility {
        nameCallCount++
        delay(callDelayMillis)
        error?.let { throw it }
        return NetworkAbility(
            name = name,
            names = listOf(
                NetworkSpeciesName("심록", NetworkNamedResource(name = "ko")),
                NetworkSpeciesName("Overgrow", NetworkNamedResource(name = "en"))
            ),
            flavorTextEntries = listOf(
                NetworkFlavorText("옛 설명", NetworkNamedResource(name = "ko")),
                NetworkFlavorText(
                    "HP가 줄었을 때\n풀타입 기술의\n위력이 올라간다.",
                    NetworkNamedResource(name = "ko")
                ),
                NetworkFlavorText(
                    "Powers up Grass-type moves\nwhen the Pokémon's HP is low.",
                    NetworkNamedResource(name = "en")
                )
            )
        )
    }

    /** 진화 체인 호출 횟수. 이미 받았으면 다시 받지 않는지 보는 데 쓴다. */
    var chainCallCount: Int = 0
        private set

    var evolutionChain: NetworkEvolutionChain = NetworkEvolutionChain(
        id = 1,
        chain = NetworkChainLink(
            species = NetworkNamedResource(
                name = "bulbasaur",
                url = "https://pokeapi.co/api/v2/pokemon-species/1/"
            ),
            evolvesTo = listOf(
                NetworkChainLink(
                    species = NetworkNamedResource(
                        name = "ivysaur",
                        url = "https://pokeapi.co/api/v2/pokemon-species/2/"
                    ),
                    evolutionDetails = listOf(NetworkEvolutionDetail(minLevel = 16))
                )
            )
        )
    )

    override suspend fun getEvolutionChain(chainId: Int): NetworkEvolutionChain {
        delay(callDelayMillis)
        chainCallCount++
        error?.let { throw it }
        return evolutionChain
    }

    /** 아이템 이름 호출 횟수. */
    var itemCallCount: Int = 0
        private set

    override suspend fun getItem(name: String): NetworkNamedEntry {
        itemCallCount++
        error?.let { throw it }
        return NetworkNamedEntry(
            name = name,
            names = listOf(
                NetworkSpeciesName("천둥의돌", NetworkNamedResource(name = "ko")),
                NetworkSpeciesName("Thunder Stone", NetworkNamedResource(name = "en"))
            )
        )
    }

    /** 알 그룹 이름 호출 횟수. */
    var eggGroupCallCount: Int = 0
        private set

    override suspend fun getEggGroup(name: String): NetworkNamedEntry {
        eggGroupCallCount++
        error?.let { throw it }
        return NetworkNamedEntry(
            name = name,
            names = listOf(
                NetworkSpeciesName("괴수", NetworkNamedResource(name = "ko")),
                NetworkSpeciesName("Monster", NetworkNamedResource(name = "en"))
            )
        )
    }

    /** 레벨업 기술. 기본은 피카츄 스칼렛·바이올렛 두 개와 소드·실드 하나다. */
    var pokemonMoves: NetworkPokemonMoves = NetworkPokemonMoves(
        moves = listOf(
            pokemonMove("scarlet-violet", 1, "thunder-shock", "전기쇼크", "electric", power = 40),
            pokemonMove("scarlet-violet", 1, "growl", "울음소리", "normal", power = null),
            pokemonMove("sword-shield", 5, "tail-whip", "꼬리흔들기", "normal", power = null)
        )
    )

    /** 기술 호출 횟수. 한 번만 받는지 보는 데 쓴다. */
    var movesCallCount: Int = 0
        private set

    override suspend fun getPokemonMoves(pokeId: Int): NetworkPokemonMoves {
        movesCallCount++
        error?.let { throw it }
        return pokemonMoves
    }

    var pokemonNames: NetworkPokemonNames = NetworkPokemonNames()

    /** 이름 전체 호출 횟수. 한 번만 받는지 보는 데 쓴다. */
    var namesCallCount: Int = 0
        private set

    override suspend fun getPokemonNames(): NetworkPokemonNames {
        namesCallCount++
        error?.let { throw it }
        return pokemonNames
    }

    /** 종 정보를 어떤 번호로 불렀는지. 폼이 종 주소를 따라가는지 보는 데 쓴다. */
    val speciesIds = mutableListOf<Int>()

    override suspend fun getPokemonSpecies(pokeId: Int): NetworkPokemonSpecies {
        delay(callDelayMillis)
        speciesIds += pokeId
        speciesCount++
        error?.let { throw it }
        speciesError?.let { throw it }
        return species
    }

    companion object {
        fun pokemonMove(
            versionGroup: String,
            level: Int,
            name: String,
            korean: String,
            type: String,
            power: Int?,
        ) = NetworkPokemonMove(
            level = level,
            versionGroup = NetworkGraphQlName(versionGroup),
            move = NetworkMove(
                name = name,
                power = power,
                accuracy = 100,
                pp = 30,
                type = NetworkGraphQlName(type),
                damageClass = NetworkGraphQlName(if (power == null) "status" else "special"),
                names = listOf(
                    NetworkGraphQlLanguageName(korean, languageId = 3),
                    NetworkGraphQlLanguageName(name, languageId = 9)
                )
            )
        )

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
            evolutionChain = NetworkNamedResource(
                url = "https://pokeapi.co/api/v2/evolution-chain/1/"
            ),
            captureRate = 45,
            genderRate = 1,
            eggGroups = listOf(NetworkNamedResource(name = "monster")),
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
