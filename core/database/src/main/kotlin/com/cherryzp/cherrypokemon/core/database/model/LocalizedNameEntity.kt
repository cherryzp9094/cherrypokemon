package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.cherryzp.cherrypokemon.core.model.PokemonType

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
    /** 이 타입이 **받을 때** 2배가 되는 공격 타입들. 공격하는 쪽 상성은 쓰지 않는다. */
    @ColumnInfo(name = "double_damage_from", defaultValue = "")
    val doubleDamageFrom: List<PokemonType> = emptyList(),
    @ColumnInfo(name = "half_damage_from", defaultValue = "")
    val halfDamageFrom: List<PokemonType> = emptyList(),
    @ColumnInfo(name = "no_damage_from", defaultValue = "")
    val noDamageFrom: List<PokemonType> = emptyList(),
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
