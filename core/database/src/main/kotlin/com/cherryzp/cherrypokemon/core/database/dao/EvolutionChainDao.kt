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
     * 체인 구성원과, 이미 열어본 포켓몬이면 그 언어별 이름을 함께 읽는다.
     *
     * 구성원마다 종 정보를 더 받지 않으려고 로컬에 있는 것만 붙인다.
     */
    @Query(
        """
        SELECT m.pokemon_id, m.name, m.stage, m.min_level, m.item, m.trigger,
               d.localized_name_korean AS korean, d.localized_name_english AS english
        FROM evolution_chain_members AS m
        LEFT JOIN pokemon_details AS d ON d.id = m.pokemon_id
        WHERE m.chain_id = :chainId
        ORDER BY m.stage, m.pokemon_id
        """
    )
    fun getChainMembers(chainId: Int): Flow<List<EvolutionChainMemberWithName>>

    @Query("SELECT COUNT(*) FROM evolution_chain_members WHERE chain_id = :chainId")
    suspend fun countChainMembers(chainId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChainMembers(members: List<EvolutionChainMemberEntity>)
}
