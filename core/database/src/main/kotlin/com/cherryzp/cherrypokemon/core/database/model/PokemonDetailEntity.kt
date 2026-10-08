package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonType

/** 상세 화면에 쓰는 포켓몬. */
@Entity(tableName = "pokemon_details")
data class PokemonDetailEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "height_meters") val heightMeters: Float,
    @ColumnInfo(name = "weight_kilograms") val weightKilograms: Float,
    /** 타입 목록. 순서를 유지해 쉼표로 잇는다. */
    val types: List<PokemonType>,
)

fun PokemonDetailEntity.asExternalModel() = PokemonDetail(
    id = id,
    name = name,
    imageUrl = imageUrl,
    heightMeters = heightMeters,
    weightKilograms = weightKilograms,
    types = types
)
