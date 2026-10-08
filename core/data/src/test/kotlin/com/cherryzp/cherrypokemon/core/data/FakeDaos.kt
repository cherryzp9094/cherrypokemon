package com.cherryzp.cherrypokemon.core.data

import androidx.paging.PagingSource
import androidx.paging.testing.asPagingSourceFactory
import com.cherryzp.cherrypokemon.core.database.DatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.dao.RemoteKeyDao
import com.cherryzp.cherrypokemon.core.database.model.PokemonDetailEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.database.model.RemoteKeyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** 메모리에만 저장하는 [PokemonDao] 대역. */
class FakePokemonDao : PokemonDao {

    private val pokemonsState = MutableStateFlow(emptyList<PokemonEntity>())
    private val detailsState = MutableStateFlow(emptyMap<Int, PokemonDetailEntity>())

    val pokemons: List<PokemonEntity> get() = pokemonsState.value

    override fun pagingSource(): PagingSource<Int, PokemonEntity> =
        pokemonsState.value.asPagingSourceFactory().invoke()

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
