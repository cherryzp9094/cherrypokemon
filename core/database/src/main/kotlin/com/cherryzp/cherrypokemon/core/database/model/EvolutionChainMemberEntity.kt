package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import com.cherryzp.cherrypokemon.core.model.EvolutionCondition
import com.cherryzp.cherrypokemon.core.model.EvolutionStage
import com.cherryzp.cherrypokemon.core.model.LocalizedText

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
)

/**
 * 체인 구성원과, 로컬에 있으면 그 포켓몬의 언어별 이름.
 *
 * 구성원마다 종 정보를 더 받지 않는다. 아직 열어보지 않은 포켓몬은 이름이 비어 있다.
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
    if (minLevel == null && item == null && trigger == null) return null
    return EvolutionCondition(minLevel = minLevel, item = item, trigger = trigger)
}

private fun officialArtworkUrl(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" +
        "other/official-artwork/$id.png"
