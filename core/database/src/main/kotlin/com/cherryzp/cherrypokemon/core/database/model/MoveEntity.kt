package com.cherryzp.cherrypokemon.core.database.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.PokemonMove
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.model.PokemonTypeInfo

/**
 * 기술. 여러 포켓몬이 같은 기술을 배우므로 한 번 받으면 재사용한다.
 *
 * @param name PokeAPI 식별자(`thunder-shock`)
 * @param type [PokemonType] 의 이름
 * @param power 변화 기술은 null
 * @param accuracy 반드시 맞는 기술은 null
 * @param damageClass `physical` / `special` / `status`
 */
@Entity(tableName = "moves")
data class MoveEntity(
    @PrimaryKey val name: String,
    @ColumnInfo(defaultValue = "") val korean: String,
    @ColumnInfo(defaultValue = "") val english: String,
    @ColumnInfo(defaultValue = "Unknown") val type: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    @ColumnInfo(name = "damage_class") val damageClass: String?,
)

/**
 * 포켓몬이 레벨업으로 배우는 기술. 최신 본편 한 게임 기준이다.
 *
 * @param level 0 이면 진화할 때 배운다
 */
@Entity(tableName = "pokemon_moves", primaryKeys = ["pokemon_id", "move", "level"])
data class PokemonMoveEntity(
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    val move: String,
    val level: Int,
)

/** 배우는 레벨과 기술 정보, 받아 두었으면 타입의 언어별 이름. */
data class PokemonMoveWithDetail(
    val level: Int,
    val name: String,
    val korean: String,
    val english: String,
    val type: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    @ColumnInfo(name = "damage_class") val damageClass: String?,
    @ColumnInfo(name = "type_korean") val typeKorean: String?,
    @ColumnInfo(name = "type_english") val typeEnglish: String?,
)

fun List<PokemonMoveWithDetail>.asExternalModel(): List<PokemonMove> = map { move ->
    PokemonMove(
        name = move.name,
        localizedName = LocalizedText(korean = move.korean, english = move.english),
        type = PokemonTypeInfo(
            type = PokemonType.entries.firstOrNull { it.name == move.type } ?: PokemonType.Unknown,
            name = LocalizedText(
                korean = move.typeKorean.orEmpty(),
                english = move.typeEnglish.orEmpty()
            )
        ),
        power = move.power,
        accuracy = move.accuracy,
        pp = move.pp,
        damageClass = move.damageClass,
        level = move.level
    )
}
