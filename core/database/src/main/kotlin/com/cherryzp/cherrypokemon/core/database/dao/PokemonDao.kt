package com.cherryzp.cherrypokemon.core.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.cherryzp.cherrypokemon.core.database.model.PokemonDetailEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface PokemonDao {
    @Query("SELECT * FROM pokemons ORDER BY id")
    fun pagingSource(): PagingSource<Int, PokemonEntity>

    /**
     * 이름이나 도감 번호로 찾는다. 번호는 앞에서부터 맞춘다(`25` 는 25, 250 을 찾는다).
     *
     * 로컬에 있는 것만 찾으므로 호출하는 쪽이 먼저 색인을 채운다.
     */
    @Query(
        """
        SELECT * FROM pokemons
        WHERE name LIKE '%' || :query || '%' OR CAST(id AS TEXT) LIKE :query || '%'
        ORDER BY id
        """
    )
    fun searchPagingSource(query: String): PagingSource<Int, PokemonEntity>

    @Query("SELECT COUNT(*) FROM pokemons")
    suspend fun countPokemons(): Int

    @Query("SELECT * FROM pokemons ORDER BY id")
    fun getPokemonEntities(): Flow<List<PokemonEntity>>

    @Query("SELECT * FROM pokemon_details WHERE id = :pokeId")
    fun getPokemonDetailEntity(pokeId: Int): Flow<PokemonDetailEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemons(entities: List<PokemonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonDetail(entity: PokemonDetailEntity)

    @Query("DELETE FROM pokemons")
    suspend fun deleteAllPokemons()
}
