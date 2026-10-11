package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * 타입의 언어별 이름. 타입은 20여 개뿐이라 한 번 받으면 모든 포켓몬이 재사용한다.
 *
 * @param type [com.cherryzp.cherrypokemon.core.model.PokemonType] 의 이름
 */
@Entity(tableName = "type_names")
data class TypeNameEntity(
    @PrimaryKey val type: String,
    @ColumnInfo(defaultValue = "") val korean: String,
    @ColumnInfo(defaultValue = "") val english: String,
)

/**
 * 특성의 언어별 이름.
 *
 * @param name PokeAPI 식별자(`overgrow`)
 */
@Entity(tableName = "ability_names")
data class AbilityNameEntity(
    @PrimaryKey val name: String,
    @ColumnInfo(defaultValue = "") val korean: String,
    @ColumnInfo(defaultValue = "") val english: String,
)
