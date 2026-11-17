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
