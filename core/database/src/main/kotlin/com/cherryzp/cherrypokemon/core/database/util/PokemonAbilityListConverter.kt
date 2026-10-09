package com.cherryzp.cherrypokemon.core.database.util

import androidx.room3.ColumnTypeConverter
import com.cherryzp.cherrypokemon.core.model.PokemonAbility

/** 특성 목록을 한 컬럼에 저장한다. 숨겨진 특성은 이름 뒤에 `!` 를 붙인다. */
internal class PokemonAbilityListConverter {
    @ColumnTypeConverter
    fun fromString(value: String): List<PokemonAbility> = value.split(SEPARATOR)
        .filter(String::isNotEmpty)
        .map { entry ->
            PokemonAbility(
                name = entry.removeSuffix(HIDDEN_MARK),
                isHidden = entry.endsWith(HIDDEN_MARK)
            )
        }

    @ColumnTypeConverter
    fun toString(abilities: List<PokemonAbility>): String =
        abilities.joinToString(SEPARATOR) { ability ->
            if (ability.isHidden) ability.name + HIDDEN_MARK else ability.name
        }

    private companion object {
        const val SEPARATOR = ","
        const val HIDDEN_MARK = "!"
    }
}
