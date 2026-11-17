package com.cherryzp.cherrypokemon.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.cherryzp.cherrypokemon.core.data.model.asEntity
import com.cherryzp.cherrypokemon.core.database.DatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.dao.RemoteKeyDao
import com.cherryzp.cherrypokemon.core.database.model.PokemonWithName
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
) : RemoteMediator<Int, PokemonWithName>() {

    /**
     * 로컬에 이미 있으면 처음부터 다시 받지 않는다.
     *
     * 검색어를 지울 때마다 새 Pager 가 만들어진다. 그때마다 REFRESH 로 표를 비우면 검색용으로 받아 둔
     * 전체 이름 색인까지 지워져, 다음 검색에서 아무것도 찾지 못한다.
     */
    override suspend fun initialize(): InitializeAction = if (pokemonDao.countPokemons() > 0) {
        InitializeAction.SKIP_INITIAL_REFRESH
    } else {
        InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonWithName>,
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
