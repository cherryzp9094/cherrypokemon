package com.cherryzp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cherryzp.domain.model.Pokemon
import com.cherryzp.domain.repository.PokemonRepository
import javax.inject.Inject

class PokemonPagingSource @Inject constructor(
    private val pokemonRepository: PokemonRepository
): PagingSource<Int, Pokemon>() {
    private val limit = 20

    override fun getRefreshKey(state: PagingState<Int, Pokemon>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> {
        val page = params.key ?: 1
        val response = pokemonRepository.fetchPokemonList(limit, page)

        return try {
            val nextPage = if (response.count() == limit) page + 1 else null
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