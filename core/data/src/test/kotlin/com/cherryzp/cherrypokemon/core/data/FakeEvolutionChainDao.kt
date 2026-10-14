package com.cherryzp.cherrypokemon.core.data

import com.cherryzp.cherrypokemon.core.database.dao.EvolutionChainDao
import com.cherryzp.cherrypokemon.core.database.model.EvolutionChainMemberEntity
import com.cherryzp.cherrypokemon.core.database.model.EvolutionChainMemberWithName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** 메모리에만 담는 진화 체인 대역. 이름 join 은 비워 둔다. */
class FakeEvolutionChainDao : EvolutionChainDao {

    private val members = MutableStateFlow(emptyList<EvolutionChainMemberEntity>())

    override fun getChainMembers(chainId: Int): Flow<List<EvolutionChainMemberWithName>> =
        members.map { all ->
            all.filter { it.chainId == chainId }
                .sortedWith(compareBy({ it.stage }, { it.pokemonId }))
                .map {
                    EvolutionChainMemberWithName(
                        pokemonId = it.pokemonId,
                        name = it.name,
                        stage = it.stage,
                        minLevel = it.minLevel,
                        item = it.item,
                        trigger = it.trigger,
                        korean = null,
                        english = null
                    )
                }
        }

    override suspend fun countChainMembers(chainId: Int): Int =
        members.value.count { it.chainId == chainId }

    override suspend fun insertChainMembers(members: List<EvolutionChainMemberEntity>) {
        this.members.value = this.members.value.filterNot { saved ->
            members.any { it.chainId == saved.chainId && it.pokemonId == saved.pokemonId }
        } + members
    }
}
