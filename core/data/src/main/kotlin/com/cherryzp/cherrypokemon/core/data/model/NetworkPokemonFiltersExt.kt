package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.PokemonFilterEntity
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonFilters

/** 타입을 모르거나 세대가 없는 포켓몬은 거를 수 없어 뺀다. */
internal fun NetworkPokemonFilters.asEntities(): List<PokemonFilterEntity> =
    pokemons.mapNotNull { pokemon ->
        val types = pokemon.types.sortedBy { it.slot }
            .map { it.type.name.asPokemonType() }
            .filter { it != PokemonType.Unknown }
        val generation = pokemon.species?.generation ?: return@mapNotNull null
        PokemonFilterEntity(
            id = pokemon.id,
            type1 = types.firstOrNull()?.name ?: return@mapNotNull null,
            type2 = types.getOrNull(1)?.name,
            generation = generation
        )
    }
