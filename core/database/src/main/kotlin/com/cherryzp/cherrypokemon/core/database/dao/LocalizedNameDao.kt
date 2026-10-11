package com.cherryzp.cherrypokemon.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.cherryzp.cherrypokemon.core.database.model.AbilityNameEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import kotlinx.coroutines.flow.Flow

/** 타입·특성의 언어별 이름 캐시. 한 번 받으면 모든 포켓몬이 재사용한다. */
@Dao
interface LocalizedNameDao {
    @Query("SELECT * FROM type_names")
    fun getTypeNames(): Flow<List<TypeNameEntity>>

    @Query("SELECT * FROM ability_names")
    fun getAbilityNames(): Flow<List<AbilityNameEntity>>

    @Query("SELECT type FROM type_names")
    suspend fun getSavedTypes(): List<String>

    @Query("SELECT name FROM ability_names")
    suspend fun getSavedAbilities(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypeNames(names: List<TypeNameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbilityNames(names: List<AbilityNameEntity>)
}
