package com.cherryzp.cherrypokemon.core.database.util

import androidx.room3.ColumnTypeConverter
import com.cherryzp.cherrypokemon.core.model.PokemonType

/** [PokemonType] 목록을 한 컬럼에 저장한다. */
internal class PokemonTypeListConverter {
    @ColumnTypeConverter
    fun fromString(value: String): List<PokemonType> = value.split(SEPARATOR)
        .filter(String::isNotEmpty)
        .map { name ->
            PokemonType.entries.firstOrNull { it.name == name } ?: PokemonType.Unknown
        }

    @ColumnTypeConverter
    fun toString(types: List<PokemonType>): String = types.joinToString(SEPARATOR) { it.name }

    private companion object {
        const val SEPARATOR = ","
    }
}
