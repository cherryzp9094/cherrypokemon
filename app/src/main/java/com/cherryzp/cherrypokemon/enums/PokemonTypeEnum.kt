package com.cherryzp.cherrypokemon.enums

import androidx.compose.ui.graphics.Color

enum class PokemonTypeEnum(val color: Color) {
    NORMAL(Color(0xFFA8A878)),
    FIGHTING(Color(0xFFC03028)),
    FLYING(Color(0xFFA890F0)),
    POISON(Color(0xFFA040A0)),
    GROUND(Color(0xFFE0C068)),
    ROCK(Color(0xFFB8A038)),
    BUG(Color(0xFFA8B820)),
    GHOST(Color(0xFF705898)),
    STEEL(Color(0xFFB8B8D0)),
    FIRE(Color(0xFFF08030)),
    WATER(Color(0xFF6890F0)),
    GRASS(Color(0xFF78C850)),
    ELECTRIC(Color(0xFFF8D030)),
    PSYCHIC(Color(0xFFF85888)),
    ICE(Color(0xFF98D8D8)),
    DRAGON(Color(0xFF7038F8)),
    DARK(Color(0xFF705848)),
    FAIRY(Color(0xFFEE99AC)),
    STELLAR(Color(0xFFFFD700)),
    UNKNOWN(Color(0xFF68A090)),
    SHADOW(Color(0xFF606060));

    companion object {
        fun creator(type: String) = entries.find { it.name == type.uppercase() } ?: UNKNOWN
    }
}