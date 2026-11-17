package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.MoveEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonMoveEntity
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonMoves

/** 최신 본편부터. 이 포켓몬 기술이 있는 가장 앞 게임 것만 쓴다. 게임마다 레벨과 기술이 다르다. */
private val MAIN_VERSION_GROUPS = listOf(
    "scarlet-violet",
    "brilliant-diamond-shining-pearl",
    "sword-shield",
    "ultra-sun-ultra-moon",
    "sun-moon",
    "omega-ruby-alpha-sapphire",
    "x-y",
    "black-2-white-2",
    "black-white",
    "heartgold-soulsilver",
    "platinum",
    "diamond-pearl",
    "emerald",
    "firered-leafgreen",
    "ruby-sapphire",
    "crystal",
    "gold-silver",
    "yellow",
    "red-blue"
)

private const val KOREAN = 3
private const val ENGLISH = 9

/** 고른 게임의 기술 정보와, 이 포켓몬이 몇 레벨에 배우는지. */
internal data class MoveEntities(
    val moves: List<MoveEntity>,
    val pokemonMoves: List<PokemonMoveEntity>,
)

internal fun NetworkPokemonMoves.asEntities(pokemonId: Int): MoveEntities {
    val versionGroup = MAIN_VERSION_GROUPS.firstOrNull { group ->
        moves.any { it.versionGroup.name == group }
    } ?: return MoveEntities(emptyList(), emptyList())
    val chosen = moves.filter { it.versionGroup.name == versionGroup }

    return MoveEntities(
        moves = chosen.map { it.move }.distinctBy { it.name }.map { move ->
            MoveEntity(
                name = move.name,
                korean = move.names.firstOrNull { it.languageId == KOREAN }?.name.orEmpty(),
                english = move.names.firstOrNull { it.languageId == ENGLISH }?.name.orEmpty(),
                type = move.type?.name?.asPokemonType()?.name ?: PokemonType.Unknown.name,
                power = move.power,
                accuracy = move.accuracy,
                pp = move.pp,
                damageClass = move.damageClass?.name
            )
        },
        pokemonMoves = chosen
            .map { PokemonMoveEntity(pokemonId = pokemonId, move = it.move.name, level = it.level) }
            .distinct()
    )
}
