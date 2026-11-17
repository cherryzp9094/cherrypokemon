package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.Pokemon

/** 목록에 쓰는 포켓몬. 네트워크에서 받아 저장한 캐시다. */
@Entity(tableName = "pokemons")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
)

fun PokemonEntity.asExternalModel() = Pokemon(
    id = id,
    name = name,
    imageUrl = imageUrl
)

/** 목록 한 줄과, 받아 두었으면 그 언어별 이름. */
data class PokemonWithName(
    @Embedded val pokemon: PokemonEntity,
    @ColumnInfo(name = "korean") val korean: String?,
    @ColumnInfo(name = "english") val english: String?,
)

fun PokemonWithName.asExternalModel() = pokemon.asExternalModel().copy(
    localizedName = LocalizedText(korean = korean.orEmpty(), english = english.orEmpty())
)

/**
 * 목록을 거를 때 쓰는 포켓몬의 타입과 세대. 목록 표와 따로 한꺼번에 받아 둔다.
 *
 * @param type1 첫 번째 타입([com.cherryzp.cherrypokemon.core.model.PokemonType] 이름)
 * @param generation 처음 나온 세대. 폼은 원래 종의 세대
 */
@Entity(tableName = "pokemon_filters")
data class PokemonFilterEntity(
    @PrimaryKey val id: Int,
    val type1: String,
    val type2: String?,
    val generation: Int,
)
