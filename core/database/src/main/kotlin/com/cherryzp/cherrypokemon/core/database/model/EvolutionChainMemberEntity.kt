package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import com.cherryzp.cherrypokemon.core.model.EvolutionCondition
import com.cherryzp.cherrypokemon.core.model.EvolutionStage
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.PokemonType

/**
 * 진화 체인을 평탄화해 담는다. 트리 그대로 담으면 읽을 때마다 다시 펼쳐야 한다.
 *
 * @param chainId 체인 ID. 같은 체인의 포켓몬이 이 값을 공유한다
 * @param stage 1 부터 시작하는 단계. 분기는 같은 단계에 여럿이다
 */
@Entity(tableName = "evolution_chain_members", primaryKeys = ["chain_id", "pokemon_id"])
data class EvolutionChainMemberEntity(
    @ColumnInfo(name = "chain_id") val chainId: Int,
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    val name: String,
    val stage: Int,
    @ColumnInfo(name = "min_level") val minLevel: Int?,
    val item: String?,
    val trigger: String?,
    @ColumnInfo(name = "held_item") val heldItem: String? = null,
    @ColumnInfo(name = "min_happiness") val minHappiness: Int? = null,
    @ColumnInfo(name = "min_affection") val minAffection: Int? = null,
    /** `day` / `night`. */
    @ColumnInfo(name = "time_of_day") val timeOfDay: String? = null,
    @ColumnInfo(name = "known_move") val knownMove: String? = null,
    /** [PokemonType] 의 이름. */
    @ColumnInfo(name = "known_move_type") val knownMoveType: String? = null,
    val location: String? = null,
)

/**
 * 체인 구성원과, 받아 두었으면 그 포켓몬과 진화 도구의 언어별 이름.
 *
 * 구성원마다 종 정보를 더 받지 않는다. 이름은 한꺼번에 받아 둔 이름 표에서 붙인다.
 */
data class EvolutionChainMemberWithName(
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    val name: String,
    val stage: Int,
    @ColumnInfo(name = "min_level") val minLevel: Int?,
    val item: String?,
    val trigger: String?,
    @ColumnInfo(name = "korean") val korean: String?,
    @ColumnInfo(name = "english") val english: String?,
    @ColumnInfo(name = "item_korean") val itemKorean: String? = null,
    @ColumnInfo(name = "item_english") val itemEnglish: String? = null,
    @ColumnInfo(name = "held_item") val heldItem: String? = null,
    @ColumnInfo(name = "held_item_korean") val heldItemKorean: String? = null,
    @ColumnInfo(name = "held_item_english") val heldItemEnglish: String? = null,
    @ColumnInfo(name = "min_happiness") val minHappiness: Int? = null,
    @ColumnInfo(name = "min_affection") val minAffection: Int? = null,
    @ColumnInfo(name = "time_of_day") val timeOfDay: String? = null,
    @ColumnInfo(name = "known_move") val knownMove: String? = null,
    @ColumnInfo(name = "known_move_type") val knownMoveType: String? = null,
    @ColumnInfo(name = "known_move_type_korean") val knownMoveTypeKorean: String? = null,
    @ColumnInfo(name = "known_move_type_english") val knownMoveTypeEnglish: String? = null,
    val location: String? = null,
)

fun List<EvolutionChainMemberWithName>.asExternalModel(): List<EvolutionStage> = map { member ->
    EvolutionStage(
        pokemonId = member.pokemonId,
        name = member.name,
        localizedName = LocalizedText(
            korean = member.korean.orEmpty(),
            english = member.english.orEmpty()
        ),
        imageUrl = officialArtworkUrl(member.pokemonId),
        stage = member.stage,
        condition = member.condition()
    )
}

/** 1 단계는 조건이 없다. */
private fun EvolutionChainMemberWithName.condition(): EvolutionCondition? {
    if (stage == 1) return null
    return EvolutionCondition(
        minLevel = minLevel,
        item = item,
        itemName = LocalizedText(korean = itemKorean.orEmpty(), english = itemEnglish.orEmpty()),
        trigger = trigger,
        heldItem = heldItem,
        heldItemName = LocalizedText(
            korean = heldItemKorean.orEmpty(),
            english = heldItemEnglish.orEmpty()
        ),
        minHappiness = minHappiness,
        minAffection = minAffection,
        timeOfDay = timeOfDay,
        knownMove = knownMove,
        knownMoveType = PokemonType.entries.firstOrNull { it.name == knownMoveType },
        knownMoveTypeName = LocalizedText(
            korean = knownMoveTypeKorean.orEmpty(),
            english = knownMoveTypeEnglish.orEmpty()
        ),
        location = location
    )
}

private fun officialArtworkUrl(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" +
        "other/official-artwork/$id.png"
