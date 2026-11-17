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
 * 특성의 언어별 이름과 설명.
 *
 * @param name PokeAPI 식별자(`overgrow`)
 * @param descriptionKorean 최신 게임의 한국어 설명. 8 버전 전에 받은 특성은 비어 있다
 */
@Entity(tableName = "ability_names")
data class AbilityNameEntity(
    @PrimaryKey val name: String,
    @ColumnInfo(defaultValue = "") val korean: String,
    @ColumnInfo(defaultValue = "") val english: String,
    @ColumnInfo(name = "description_korean", defaultValue = "")
    val descriptionKorean: String = "",
    @ColumnInfo(name = "description_english", defaultValue = "")
    val descriptionEnglish: String = "",
)

/**
 * 종과 폼의 언어별 이름. 목록·검색·진화 단계가 함께 쓴다.
 *
 * 상세를 열 때 받는 종 정보와 달리 모든 포켓몬을 한 번에 받아 둔다.
 *
 * @param id 도감 번호. 폼은 10001 번부터다
 */
@Entity(tableName = "pokemon_names")
data class PokemonNameEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(defaultValue = "") val korean: String,
    @ColumnInfo(defaultValue = "") val english: String,
)

/**
 * 진화 도구 같은 아이템의 언어별 이름.
 *
 * @param name PokeAPI 식별자(`thunder-stone`)
 */
@Entity(tableName = "item_names")
data class ItemNameEntity(
    @PrimaryKey val name: String,
    @ColumnInfo(defaultValue = "") val korean: String,
    @ColumnInfo(defaultValue = "") val english: String,
)
