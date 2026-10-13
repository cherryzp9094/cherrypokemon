package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.EvolutionChainMemberEntity
import com.cherryzp.cherrypokemon.core.network.model.NetworkChainLink
import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionChain

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
            // 조건은 버전마다 여럿 올 수 있어 첫 번째만 쓴다.
            val detail = link.evolutionDetails.firstOrNull()
            members += EvolutionChainMemberEntity(
                chainId = id,
                pokemonId = pokemonId,
                name = link.species.name,
                stage = stage,
                minLevel = detail?.minLevel,
                item = detail?.item?.name,
                trigger = detail?.trigger?.name
            )
        }
        link.evolvesTo.forEach { walk(it, stage + 1) }
    }

    walk(chain, stage = 1)
    return members
}

/** `https://pokeapi.co/api/v2/evolution-chain/1/` 처럼 끝의 숫자를 꺼낸다. */
internal fun String.chainId(): Int? = trimEnd('/').substringAfterLast('/').toIntOrNull()

private fun String.pokemonId(): Int? = chainId()
