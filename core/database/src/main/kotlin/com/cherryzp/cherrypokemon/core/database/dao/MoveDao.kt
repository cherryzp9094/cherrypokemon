package com.cherryzp.cherrypokemon.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.cherryzp.cherrypokemon.core.database.model.MoveEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonMoveEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonMoveWithDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface MoveDao {
    /** 레벨 순. 같은 레벨은 이름 순으로 고정해 갱신 때마다 줄이 바뀌지 않게 한다. */
    @Query(
        """
        SELECT pm.level, m.name, m.korean, m.english, m.type, m.power, m.accuracy, m.pp,
               m.damage_class, t.korean AS type_korean, t.english AS type_english
        FROM pokemon_moves AS pm
        JOIN moves AS m ON m.name = pm.move
        LEFT JOIN type_names AS t ON t.type = m.type
        WHERE pm.pokemon_id = :pokemonId
        ORDER BY pm.level, m.name
        """
    )
    fun getPokemonMoves(pokemonId: Int): Flow<List<PokemonMoveWithDetail>>

    @Query("SELECT COUNT(*) FROM pokemon_moves WHERE pokemon_id = :pokemonId")
    suspend fun countPokemonMoves(pokemonId: Int): Int

    /** 기술 정보에 나오는 타입([com.cherryzp.cherrypokemon.core.model.PokemonType] 이름). */
    @Query(
        "SELECT DISTINCT m.type FROM pokemon_moves AS pm JOIN moves AS m ON m.name = pm.move " +
            "WHERE pm.pokemon_id = :pokemonId"
    )
    suspend fun getPokemonMoveTypes(pokemonId: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoves(moves: List<MoveEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonMoves(moves: List<PokemonMoveEntity>)
}
