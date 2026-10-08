package com.cherryzp.cherrypokemon.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.cherryzp.cherrypokemon.core.data.FakeDatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.data.FakePokemonDao
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource.Companion.networkPokemon
import com.cherryzp.cherrypokemon.core.data.FakeRemoteKeyDao
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonPage
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediatorTest {

    private lateinit var network: FakePokemonNetworkDataSource
    private lateinit var pokemonDao: FakePokemonDao
    private lateinit var remoteKeyDao: FakeRemoteKeyDao
    private lateinit var mediator: PokemonRemoteMediator

    @Before
    fun setUp() {
        network = FakePokemonNetworkDataSource()
        pokemonDao = FakePokemonDao()
        remoteKeyDao = FakeRemoteKeyDao()
        mediator = PokemonRemoteMediator(
            pokemonDao = pokemonDao,
            remoteKeyDao = remoteKeyDao,
            network = network,
            transaction = FakeDatabaseTransactionRunner()
        )
    }

    @Test
    fun refresh_networkError_returnsError() = runTest {
        network.error = IOException()

        val result = mediator.load(LoadType.REFRESH, pagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    @Test
    fun refresh_morePages_savesEntitiesAndNextOffset() = runTest {
        network.page = NetworkPokemonPage(
            count = 100,
            next = "https://pokeapi.co/api/v2/pokemon?offset=2&limit=2",
            results = listOf(networkPokemon(1, "bulbasaur"), networkPokemon(2, "ivysaur"))
        )

        val result = mediator.load(LoadType.REFRESH, pagingState())

        assertEquals(false, result.endOfPaginationReached())
        assertEquals(listOf(1, 2), pokemonDao.pokemons.map { it.id })
        assertEquals(2, remoteKeyDao.getRemoteKey()?.nextOffset)
    }

    @Test
    fun refresh_lastPage_reportsEndOfPagination() = runTest {
        network.page = NetworkPokemonPage(
            count = 1,
            next = null,
            results = listOf(networkPokemon(1, "bulbasaur"))
        )

        val result = mediator.load(LoadType.REFRESH, pagingState())

        assertEquals(true, result.endOfPaginationReached())
        assertEquals(null, remoteKeyDao.getRemoteKey()?.nextOffset)
    }

    @Test
    fun append_withoutRemoteKey_reportsEndOfPagination() = runTest {
        val result = mediator.load(LoadType.APPEND, pagingState())

        assertEquals(true, result.endOfPaginationReached())
    }

    @Test
    fun prepend_reportsEndOfPagination() = runTest {
        val result = mediator.load(LoadType.PREPEND, pagingState())

        assertEquals(true, result.endOfPaginationReached())
    }

    /** [RemoteMediator.MediatorResult.Success] 는 동등성 비교를 지원하지 않아 값만 꺼낸다. */
    private fun RemoteMediator.MediatorResult.endOfPaginationReached(): Boolean =
        (this as RemoteMediator.MediatorResult.Success).endOfPaginationReached

    private fun pagingState() = PagingState<Int, PokemonEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 2),
        leadingPlaceholderCount = 0
    )
}
