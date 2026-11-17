package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.EvolutionChainMemberEntity
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.model.NetworkChainLink
import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionChain
import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionDetail

/**
 * 트리를 단계 목록으로 펼친다.
 *
 * 분기(이브이)는 같은 단계에 여럿으로 들어간다. ID 를 못 꺼내는 구성원은 버린다.
 */
internal fun NetworkEvolutionChain.asEntities(): List<EvolutionChainMemberEntity> {
    val members = mutableListOf<EvolutionChainMemberEntity>()

    fun walk(link: NetworkChainLink, stage: Int) {
        val pokemonId = link.species.url.pokemonId()
        if (pokemonId != null) {
            val detail = link.evolutionDetails.representative()
            members += EvolutionChainMemberEntity(
                chainId = id,
                pokemonId = pokemonId,
                name = link.species.name,
                stage = stage,
                minLevel = detail?.minLevel,
                item = detail?.item?.name,
                trigger = detail?.trigger?.name,
                heldItem = detail?.heldItem?.name,
                minHappiness = detail?.minHappiness,
                minAffection = detail?.minAffection,
                timeOfDay = detail?.timeOfDay?.ifEmpty { null },
                knownMove = detail?.knownMove?.name,
                knownMoveType = detail?.knownMoveType?.name
                    ?.asPokemonType()
                    ?.takeIf { it != PokemonType.Unknown }
                    ?.name,
                location = detail?.location?.name
            )
        }
        link.evolvesTo.forEach { walk(it, stage + 1) }
    }

    walk(chain, stage = 1)
    return members
}

/**
 * 버전마다 여럿 오는 조건 중 하나를 고른다.
 *
 * - 지역 폼으로 진화하는 조건(갈라르 야도킹)은 기본 모습 체인과 맞지 않아 뺀다.
 * - 장소 조건은 게임마다 장소가 달라 여럿 온다. 최신 게임에서 도구로 바뀐 경우가 많아
 *   (리피아의 리프의돌, 자포코일의 천둥의돌) 장소가 아닌 조건이 있으면 그것을 쓴다.
 */
private fun List<NetworkEvolutionDetail>.representative(): NetworkEvolutionDetail? {
    val sameForm = filter { it.evolvedPokemonForm == null }.ifEmpty { this }
    return sameForm.firstOrNull { it.location == null } ?: sameForm.firstOrNull()
}

/** `https://pokeapi.co/api/v2/evolution-chain/1/` 처럼 끝의 숫자를 꺼낸다. */
internal fun String.urlId(): Int? = trimEnd('/').substringAfterLast('/').toIntOrNull()

internal fun String.chainId(): Int? = urlId()

private fun String.pokemonId(): Int? = urlId()
