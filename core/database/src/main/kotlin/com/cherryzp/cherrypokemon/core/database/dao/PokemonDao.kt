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
import com.cherryzp.cherrypokemon.core.database.model.PokemonWithName
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface PokemonDao {
    /** 목록. 받아 둔 언어별 이름을 붙인다. 이름 표가 바뀌면 다시 읽는다. */
    @Query(
        """
        SELECT p.*, n.korean AS korean, n.english AS english
        FROM pokemons AS p
        LEFT JOIN pokemon_names AS n ON n.id = p.id
        ORDER BY p.id
        """
    )
    fun pagingSource(): PagingSource<Int, PokemonWithName>

    /**
     * 이름이나 도감 번호로 찾는다. 번호는 앞에서부터 맞춘다(`25` 는 25, 250 을 찾는다).
     *
     * 이름은 식별자(`pikachu`)와 받아 둔 한국어·영어 이름(`피카츄`, `Pikachu`)에서 찾는다.
     * 로컬에 있는 것만 찾으므로 호출하는 쪽이 먼저 색인을 채운다.
     */
    @Query(
        """
        SELECT p.*, n.korean AS korean, n.english AS english
        FROM pokemons AS p
        LEFT JOIN pokemon_names AS n ON n.id = p.id
        WHERE p.name LIKE '%' || :query || '%'
            OR n.korean LIKE '%' || :query || '%'
            OR n.english LIKE '%' || :query || '%'
            OR CAST(p.id AS TEXT) LIKE :query || '%'
        ORDER BY p.id
        """
    )
    fun searchPagingSource(query: String): PagingSource<Int, PokemonWithName>

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
