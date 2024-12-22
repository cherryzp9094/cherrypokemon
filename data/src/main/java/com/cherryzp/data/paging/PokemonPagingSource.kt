package com.cherryzp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cherryzp.data.api.PokemonApi
import com.cherryzp.data.extend.default
import com.cherryzp.data.mapper.toDomain
import com.cherryzp.domain.model.Pokemon
import com.cherryzp.domain.repository.PokemonRepository
import javax.inject.Inject

class PokemonPagingSource @Inject constructor(
    private val pokemonApi: PokemonApi
): PagingSource<Int, Pokemon>() {
    private val limit = 20

    override fun getRefreshKey(state: PagingState<Int, Pokemon>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> {
        val page = params.key ?: 0
        val response = pokemonApi.fetchPokemonList(limit, page)
            .results?.map { it.toDomain() }.default()

        return try {
            val nextPage = if (response.count() == limit) page + limit else null
            LoadResult.Page(
                data = response,
                nextKey = nextPage,
                prevKey = null,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}