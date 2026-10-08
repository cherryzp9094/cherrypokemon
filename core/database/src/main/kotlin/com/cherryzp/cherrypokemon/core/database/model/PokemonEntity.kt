package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
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
