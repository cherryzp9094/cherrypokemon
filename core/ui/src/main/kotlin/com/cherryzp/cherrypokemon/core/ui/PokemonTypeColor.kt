package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.ui.graphics.Color
import com.cherryzp.cherrypokemon.core.model.PokemonType

/** 타입을 화면에 표시할 색으로 바꾼다. 도메인 모델은 색을 모른다. */
val PokemonType.color: Color
    get() = when (this) {
        PokemonType.Normal -> Color(0xFFA8A878)
        PokemonType.Fighting -> Color(0xFFC03028)
        PokemonType.Flying -> Color(0xFFA890F0)
        PokemonType.Poison -> Color(0xFFA040A0)
        PokemonType.Ground -> Color(0xFFE0C068)
        PokemonType.Rock -> Color(0xFFB8A038)
        PokemonType.Bug -> Color(0xFFA8B820)
        PokemonType.Ghost -> Color(0xFF705898)
        PokemonType.Steel -> Color(0xFFB8B8D0)
        PokemonType.Fire -> Color(0xFFF08030)
        PokemonType.Water -> Color(0xFF6890F0)
        PokemonType.Grass -> Color(0xFF78C850)
        PokemonType.Electric -> Color(0xFFF8D030)
        PokemonType.Psychic -> Color(0xFFF85888)
        PokemonType.Ice -> Color(0xFF98D8D8)
        PokemonType.Dragon -> Color(0xFF7038F8)
        PokemonType.Dark -> Color(0xFF705848)
        PokemonType.Fairy -> Color(0xFFEE99AC)
        PokemonType.Stellar -> Color(0xFFFFD700)
        PokemonType.Shadow -> Color(0xFF606060)
        PokemonType.Unknown -> Color(0xFF68A090)
    }
