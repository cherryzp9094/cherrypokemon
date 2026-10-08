package com.cherryzp.cherrypokemon.core.data

import com.cherryzp.cherrypokemon.core.network.PokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedResource
import com.cherryzp.cherrypokemon.core.network.model.NetworkOfficialArtwork
import com.cherryzp.cherrypokemon.core.network.model.NetworkOtherSprites
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonPage
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonTypeSlot
import com.cherryzp.cherrypokemon.core.network.model.NetworkSprites

/** 테스트가 응답과 실패를 지정할 수 있는 네트워크 대역. */
class FakePokemonNetworkDataSource : PokemonNetworkDataSource {

    var error: Throwable? = null
    var page: NetworkPokemonPage = NetworkPokemonPage()
    var detail: NetworkPokemonDetail = NetworkPokemonDetail()

    override suspend fun getPokemons(offset: Int, limit: Int): NetworkPokemonPage {
        error?.let { throw it }
        return page
    }

    override suspend fun getPokemonDetail(pokeId: Int): NetworkPokemonDetail {
        error?.let { throw it }
        return detail
    }

    companion object {
        fun networkPokemon(id: Int, name: String) = NetworkNamedResource(
            name = name,
            url = "https://pokeapi.co/api/v2/pokemon/$id/"
        )

        fun networkPokemonDetail(
            id: Int,
            name: String,
            typeNames: List<String> = listOf("grass"),
        ) = NetworkPokemonDetail(
            id = id,
            name = name,
            height = 7,
            weight = 69,
            types = typeNames.mapIndexed { index, typeName ->
                NetworkPokemonTypeSlot(
                    slot = index + 1,
                    type = NetworkNamedResource(name = typeName)
                )
            },
            sprites = NetworkSprites(
                other = NetworkOtherSprites(
                    officialArtwork = NetworkOfficialArtwork(
                        frontDefault = "https://example.com/$id.png"
                    )
                )
            )
        )
    }
}
