package com.cherryzp.cherrypokemon.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.cherryzp.cherrypokemon.core.database.model.RemoteKeyEntity

@Dao
interface RemoteKeyDao {
    @Query("SELECT * FROM remote_keys WHERE id = :id")
    suspend fun getRemoteKey(id: Int = RemoteKeyEntity.SINGLE_ROW_ID): RemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRemoteKey(entity: RemoteKeyEntity)

    @Query("DELETE FROM remote_keys")
    suspend fun deleteAllRemoteKeys()
}
