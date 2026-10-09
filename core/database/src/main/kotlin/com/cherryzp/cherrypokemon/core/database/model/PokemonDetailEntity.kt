package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonStats
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
    @ColumnInfo(name = "base_experience", defaultValue = "0") val baseExperience: Int,
    @Embedded(prefix = "stat_") val stats: PokemonStatsEntity,
    /** 특성 목록. 순서를 유지해 쉼표로 잇고 숨겨진 특성은 이름 뒤에 표시를 붙인다. */
    @ColumnInfo(defaultValue = "") val abilities: List<PokemonAbility>,
)

/** 종족값은 종류가 고정이라 테이블을 나누지 않고 컬럼으로 넣는다. */
data class PokemonStatsEntity(
    @ColumnInfo(defaultValue = "0") val hp: Int,
    @ColumnInfo(defaultValue = "0") val attack: Int,
    @ColumnInfo(defaultValue = "0") val defense: Int,
    @ColumnInfo(name = "special_attack", defaultValue = "0") val specialAttack: Int,
    @ColumnInfo(name = "special_defense", defaultValue = "0") val specialDefense: Int,
    @ColumnInfo(defaultValue = "0") val speed: Int,
)

fun PokemonDetailEntity.asExternalModel() = PokemonDetail(
    id = id,
    name = name,
    imageUrl = imageUrl,
    heightMeters = heightMeters,
    weightKilograms = weightKilograms,
    types = types,
    baseExperience = baseExperience,
    stats = PokemonStats(
        hp = stats.hp,
        attack = stats.attack,
        defense = stats.defense,
        specialAttack = stats.specialAttack,
        specialDefense = stats.specialDefense,
        speed = stats.speed
    ),
    abilities = abilities
)
