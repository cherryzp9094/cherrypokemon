package com.cherryzp.cherrypokemon.core.data

import androidx.paging.PagingSource
import androidx.paging.testing.asPagingSourceFactory
import com.cherryzp.cherrypokemon.core.database.DatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.dao.RemoteKeyDao
import com.cherryzp.cherrypokemon.core.database.model.PokemonDetailEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonFilterEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonNameEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonWithName
import com.cherryzp.cherrypokemon.core.database.model.RemoteKeyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * 메모리에만 저장하는 [PokemonDao] 대역.
 *
 * @param names 목록에 붙일 이름. 실제 DAO 의 이름 표 join 을 흉내 낸다
 */
class FakePokemonDao(private val names: () -> Map<Int, PokemonNameEntity> = { emptyMap() }) :
    PokemonDao {

    private val pokemonsState = MutableStateFlow(emptyList<PokemonEntity>())
    private val detailsState = MutableStateFlow(emptyMap<Int, PokemonDetailEntity>())

    val pokemons: List<PokemonEntity> get() = pokemonsState.value

    override fun pagingSource(): PagingSource<Int, PokemonWithName> =
        withNames(pokemonsState.value).asPagingSourceFactory().invoke()

    private val filtersState = MutableStateFlow(emptyMap<Int, PokemonFilterEntity>())

    override fun searchPagingSource(
        query: String,
        type: String?,
        generation: Int?,
    ): PagingSource<Int, PokemonWithName> {
        val filters = filtersState.value
        return withNames(pokemonsState.value)
            .filter {
                it.pokemon.name.contains(query, ignoreCase = true) ||
                    it.korean.orEmpty().contains(query, ignoreCase = true) ||
                    it.english.orEmpty().contains(query, ignoreCase = true) ||
                    it.pokemon.id.toString().startsWith(query)
            }
            .filter { pokemon ->
                val filter = filters[pokemon.pokemon.id]
                (type == null || filter?.type1 == type || filter?.type2 == type) &&
                    (generation == null || filter?.generation == generation)
            }
            .asPagingSourceFactory()
            .invoke()
    }

    override suspend fun countFilters(): Int = filtersState.value.size

    override suspend fun insertFilters(filters: List<PokemonFilterEntity>) {
        filtersState.value = filtersState.value + filters.associateBy { it.id }
    }

    private fun withNames(pokemons: List<PokemonEntity>): List<PokemonWithName> {
        val saved = names()
        return pokemons.map {
            PokemonWithName(it, korean = saved[it.id]?.korean, english = saved[it.id]?.english)
        }
    }

    override suspend fun countPokemons(): Int = pokemonsState.value.size

    override fun getPokemonEntities(): Flow<List<PokemonEntity>> = pokemonsState

    override fun getPokemonDetailEntity(pokeId: Int): Flow<PokemonDetailEntity?> =
        detailsState.map { it[pokeId] }

    override suspend fun insertPokemons(entities: List<PokemonEntity>) {
        pokemonsState.value = (pokemonsState.value + entities)
            .associateBy(PokemonEntity::id)
            .values
            .sortedBy(PokemonEntity::id)
    }

    override suspend fun insertPokemonDetail(entity: PokemonDetailEntity) {
        detailsState.value = detailsState.value + (entity.id to entity)
    }

    override suspend fun deleteAllPokemons() {
        pokemonsState.value = emptyList()
    }
}

/** 메모리에만 저장하는 [RemoteKeyDao] 대역. */
class FakeRemoteKeyDao : RemoteKeyDao {

    private var remoteKey: RemoteKeyEntity? = null

    override suspend fun getRemoteKey(id: Int): RemoteKeyEntity? = remoteKey

    override suspend fun upsertRemoteKey(entity: RemoteKeyEntity) {
        remoteKey = entity
    }

    override suspend fun deleteAllRemoteKeys() {
        remoteKey = null
    }
}

/** 트랜잭션 없이 그대로 실행한다. */
class FakeDatabaseTransactionRunner : DatabaseTransactionRunner {
    override suspend fun <R> invoke(block: suspend () -> R): R = block()
}
