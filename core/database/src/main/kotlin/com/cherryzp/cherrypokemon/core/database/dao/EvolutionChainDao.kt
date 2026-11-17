package com.cherryzp.cherrypokemon.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.cherryzp.cherrypokemon.core.database.model.EvolutionChainMemberEntity
import com.cherryzp.cherrypokemon.core.database.model.EvolutionChainMemberWithName
import kotlinx.coroutines.flow.Flow

@Dao
interface EvolutionChainDao {
    /**
     * 체인 구성원과 그 언어별 이름, 진화 도구의 언어별 이름을 함께 읽는다.
     *
     * 포켓몬 이름은 한꺼번에 받아 둔 이름 표에서, 없으면 이미 열어본 상세에서 붙인다.
     * 구성원마다 종 정보를 더 받지 않는다.
     */
    @Query(
        """
        SELECT m.pokemon_id, m.name, m.stage, m.min_level, m.item, m.trigger,
               COALESCE(n.korean, d.localized_name_korean) AS korean,
               COALESCE(n.english, d.localized_name_english) AS english,
               i.korean AS item_korean, i.english AS item_english,
               m.held_item, h.korean AS held_item_korean, h.english AS held_item_english,
               m.min_happiness, m.min_affection, m.time_of_day, m.known_move,
               m.known_move_type, t.korean AS known_move_type_korean,
               t.english AS known_move_type_english, m.location
        FROM evolution_chain_members AS m
        LEFT JOIN pokemon_names AS n ON n.id = m.pokemon_id
        LEFT JOIN pokemon_details AS d ON d.id = m.pokemon_id
        LEFT JOIN item_names AS i ON i.name = m.item
        LEFT JOIN item_names AS h ON h.name = m.held_item
        LEFT JOIN type_names AS t ON t.type = m.known_move_type
        WHERE m.chain_id = :chainId
        ORDER BY m.stage, m.pokemon_id
        """
    )
    fun getChainMembers(chainId: Int): Flow<List<EvolutionChainMemberWithName>>

    /** 체인에 쓰이는 진화 도구와 지니는 도구. 이름을 받아 둘 대상이다. */
    @Query(
        """
        SELECT item FROM evolution_chain_members WHERE chain_id = :chainId AND item IS NOT NULL
        UNION
        SELECT held_item FROM evolution_chain_members
        WHERE chain_id = :chainId AND held_item IS NOT NULL
        """
    )
    suspend fun getChainItems(chainId: Int): List<String>

    /** 체인 조건에 나오는 기술 타입([com.cherryzp.cherrypokemon.core.model.PokemonType] 이름). */
    @Query(
        "SELECT DISTINCT known_move_type FROM evolution_chain_members " +
            "WHERE chain_id = :chainId AND known_move_type IS NOT NULL"
    )
    suspend fun getChainMoveTypes(chainId: Int): List<String>

    @Query("SELECT COUNT(*) FROM evolution_chain_members WHERE chain_id = :chainId")
    suspend fun countChainMembers(chainId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChainMembers(members: List<EvolutionChainMemberEntity>)
}
