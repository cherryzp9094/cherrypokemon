package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonStats
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.model.PokemonTypeInfo

/** 상세 화면에 쓰는 포켓몬. */
@Entity(tableName = "pokemon_details")
data class PokemonDetailEntity(
    @PrimaryKey val id: Int,
    /** PokeAPI 식별자. 화면에는 언어별 이름을 쓴다. */
    val name: String,
    @Embedded(prefix = "localized_name_") val localizedName: LocalizedTextEntity,
    @Embedded(prefix = "genus_") val genus: LocalizedTextEntity,
    @Embedded(prefix = "flavor_text_") val flavorText: LocalizedTextEntity,
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

/** 언어별 문자열. 언어를 바꿔도 다시 받지 않도록 둘 다 저장한다. */
data class LocalizedTextEntity(
    @ColumnInfo(defaultValue = "") val korean: String,
    @ColumnInfo(defaultValue = "") val english: String,
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

/**
 * @param typeNames 타입 이름 캐시. 아직 받지 못한 타입은 이름이 빈 채로 나간다
 * @param abilityNames 특성 이름 캐시
 */
fun PokemonDetailEntity.asExternalModel(
    typeNames: Map<String, LocalizedText> = emptyMap(),
    abilityNames: Map<String, LocalizedText> = emptyMap(),
) = PokemonDetail(
    id = id,
    name = name,
    localizedName = localizedName.asExternalModel(),
    genus = genus.asExternalModel(),
    flavorText = flavorText.asExternalModel(),
    imageUrl = imageUrl,
    heightMeters = heightMeters,
    weightKilograms = weightKilograms,
    types = types.map { type ->
        PokemonTypeInfo(type = type, name = typeNames[type.name] ?: LocalizedText.Empty)
    },
    baseExperience = baseExperience,
    stats = PokemonStats(
        hp = stats.hp,
        attack = stats.attack,
        defense = stats.defense,
        specialAttack = stats.specialAttack,
        specialDefense = stats.specialDefense,
        speed = stats.speed
    ),
    abilities = abilities.map { ability ->
        ability.copy(localizedName = abilityNames[ability.name] ?: LocalizedText.Empty)
    }
)

private fun LocalizedTextEntity.asExternalModel() =
    LocalizedText(korean = korean, english = english)

fun TypeNameEntity.asExternalModel() = type to LocalizedText(korean = korean, english = english)

fun AbilityNameEntity.asExternalModel() = name to LocalizedText(korean = korean, english = english)
