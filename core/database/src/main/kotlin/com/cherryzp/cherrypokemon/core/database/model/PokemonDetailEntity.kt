package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.cherryzp.cherrypokemon.core.model.EggGroup
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
    /** 진화 체인 ID. 아직 받지 못했으면 0 이다. */
    @ColumnInfo(name = "evolution_chain_id", defaultValue = "0") val evolutionChainId: Int = 0,
    /** 색이 다른 모습의 일러스트. 없으면 빈 문자열이다. */
    @ColumnInfo(name = "shiny_image_url", defaultValue = "") val shinyImageUrl: String = "",
    /** 울음소리 OGG 파일. 없으면 빈 문자열이다. */
    @ColumnInfo(name = "cry_url", defaultValue = "") val cryUrl: String = "",
    /** 포획률(0~255). 받기 전이면 null. */
    @ColumnInfo(name = "capture_rate") val captureRate: Int? = null,
    /** 암컷일 확률을 8 분의 몇으로. 성별이 없으면 -1, 받기 전이면 null. */
    @ColumnInfo(name = "gender_rate") val genderRate: Int? = null,
    /** 알 그룹 식별자. 순서를 유지해 쉼표로 잇는다. */
    @ColumnInfo(name = "egg_groups", defaultValue = "") val eggGroups: String = "",
    @ColumnInfo(name = "is_legendary", defaultValue = "0") val isLegendary: Boolean = false,
    @ColumnInfo(name = "is_mythical", defaultValue = "0") val isMythical: Boolean = false,
    /** 같은 종의 모든 모습. `번호:식별자` 를 순서대로 쉼표로 잇는다. */
    @ColumnInfo(defaultValue = "") val varieties: String = "",
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
 * @param eggGroupNames 알 그룹 이름 캐시
 */
fun PokemonDetailEntity.asExternalModel(
    typeNames: Map<String, TypeNameEntity> = emptyMap(),
    abilityNames: Map<String, AbilityNameEntity> = emptyMap(),
    eggGroupNames: Map<String, EggGroupNameEntity> = emptyMap(),
) = PokemonDetail(
    id = id,
    name = name,
    localizedName = localizedName.asExternalModel(),
    genus = genus.asExternalModel(),
    flavorText = flavorText.asExternalModel(),
    imageUrl = imageUrl,
    shinyImageUrl = shinyImageUrl,
    cryUrl = cryUrl,
    captureRate = captureRate,
    genderRate = genderRate,
    eggGroups = eggGroups.split(',').filter { it.isNotEmpty() }.map { group ->
        EggGroup(
            name = group,
            localizedName = eggGroupNames[group]
                ?.let { LocalizedText(korean = it.korean, english = it.english) }
                ?: LocalizedText.Empty
        )
    },
    isLegendary = isLegendary,
    isMythical = isMythical,
    heightMeters = heightMeters,
    weightKilograms = weightKilograms,
    types = types.map { type ->
        PokemonTypeInfo(
            type = type,
            name =
                typeNames[type.name]?.asLocalizedText() ?: LocalizedText.Empty
        )
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
        val saved = abilityNames[ability.name]
        ability.copy(
            localizedName = saved?.let { LocalizedText(it.korean, it.english) }
                ?: LocalizedText.Empty,
            description = saved?.let { LocalizedText(it.descriptionKorean, it.descriptionEnglish) }
                ?: LocalizedText.Empty
        )
    },
    typeMatchups = typeMatchups(defending = types, relations = typeNames)
)

private fun LocalizedTextEntity.asExternalModel() =
    LocalizedText(korean = korean, english = english)

fun TypeNameEntity.asLocalizedText() = LocalizedText(korean = korean, english = english)
