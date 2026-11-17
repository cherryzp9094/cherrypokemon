package com.cherryzp.cherrypokemon.core.data

import com.cherryzp.cherrypokemon.core.database.dao.MoveDao
import com.cherryzp.cherrypokemon.core.database.model.MoveEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonMoveEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonMoveWithDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/** 메모리에만 담는 기술 대역. 타입 이름 join 은 비워 둔다. */
class FakeMoveDao : MoveDao {

    private val moves = MutableStateFlow(emptyMap<String, MoveEntity>())

    val savedMoves: Map<String, MoveEntity> get() = moves.value
    private val pokemonMoves = MutableStateFlow(emptySet<PokemonMoveEntity>())

    override fun getPokemonMoves(pokemonId: Int): Flow<List<PokemonMoveWithDetail>> =
        combine(moves, pokemonMoves) { moves, pokemonMoves ->
            pokemonMoves.filter { it.pokemonId == pokemonId }
                .mapNotNull { learned ->
                    val move = moves[learned.move] ?: return@mapNotNull null
                    PokemonMoveWithDetail(
                        level = learned.level,
                        name = move.name,
                        korean = move.korean,
                        english = move.english,
                        type = move.type,
                        power = move.power,
                        accuracy = move.accuracy,
                        pp = move.pp,
                        damageClass = move.damageClass,
                        typeKorean = null,
                        typeEnglish = null
                    )
                }
                .sortedWith(compareBy({ it.level }, { it.name }))
        }

    override suspend fun countPokemonMoves(pokemonId: Int): Int =
        pokemonMoves.value.count { it.pokemonId == pokemonId }

    override suspend fun getPokemonMoveTypes(pokemonId: Int): List<String> =
        pokemonMoves.value.filter { it.pokemonId == pokemonId }
            .mapNotNull { moves.value[it.move]?.type }
            .distinct()

    override suspend fun insertMoves(moves: List<MoveEntity>) {
        this.moves.value = this.moves.value + moves.associateBy { it.name }
    }

    override suspend fun insertPokemonMoves(moves: List<PokemonMoveEntity>) {
        pokemonMoves.value = pokemonMoves.value + moves
    }
}
