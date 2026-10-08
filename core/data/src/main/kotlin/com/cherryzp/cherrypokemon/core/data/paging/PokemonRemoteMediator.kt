package com.cherryzp.cherrypokemon.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.cherryzp.cherrypokemon.core.data.model.asEntity
import com.cherryzp.cherrypokemon.core.database.DatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.dao.RemoteKeyDao
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.database.model.RemoteKeyEntity
import com.cherryzp.cherrypokemon.core.network.PokemonNetworkDataSource
import java.io.IOException
import retrofit2.HttpException

/** 로컬 데이터가 떨어지면 네트워크에서 받아 로컬에 저장만 한다. 화면에 주는 데이터는 Room 이 만든다. */
@OptIn(ExperimentalPagingApi::class)
internal class PokemonRemoteMediator(
    private val pokemonDao: PokemonDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val network: PokemonNetworkDataSource,
    private val transaction: DatabaseTransactionRunner,
) : RemoteMediator<Int, PokemonEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>,
    ): MediatorResult {
        val offset = when (loadType) {
            LoadType.REFRESH -> 0

            // 앞쪽 페이지는 따로 받지 않는다.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> remoteKeyDao.getRemoteKey()?.nextOffset
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val page = network.getPokemons(offset = offset, limit = state.config.pageSize)
            val entities = page.results.mapNotNull { it.asEntity() }
            val endOfPaginationReached = page.next == null

            transaction {
                if (loadType == LoadType.REFRESH) {
                    pokemonDao.deleteAllPokemons()
                    remoteKeyDao.deleteAllRemoteKeys()
                }
                pokemonDao.insertPokemons(entities)
                remoteKeyDao.upsertRemoteKey(
                    RemoteKeyEntity(
                        nextOffset = if (endOfPaginationReached) null else offset + entities.size
                    )
                )
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}
