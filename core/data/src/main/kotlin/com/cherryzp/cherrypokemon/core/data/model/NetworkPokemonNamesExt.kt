package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.ItemNameEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonNameEntity
import com.cherryzp.cherrypokemon.core.network.model.NetworkFormName
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedEntry
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonForm
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonNames

/** PokeAPI 언어 ID. */
private const val KOREAN = 3
private const val ENGLISH = 9

/**
 * 종 이름과 폼 이름을 도감 번호별 한 줄로 만든다.
 *
 * 종(1~1025 번)은 받은 이름 그대로다. 폼(10001 번부터)은 데이터가 고르지 않아 규칙으로 맞춘다.
 * [formName] 참고.
 */
internal fun NetworkPokemonNames.asEntities(): List<PokemonNameEntity> {
    val korean = speciesNames.filter {
        it.languageId == KOREAN
    }.associate { it.speciesId to it.name }
    val english = speciesNames.filter { it.languageId == ENGLISH }.associate {
        it.speciesId to
            it.name
    }

    val species = (korean.keys + english.keys).map { id ->
        PokemonNameEntity(id = id, korean = korean[id].orEmpty(), english = english[id].orEmpty())
    }
    val forms = forms.map { form ->
        PokemonNameEntity(
            id = form.id,
            korean = form.formName(KOREAN, speciesName = korean[form.speciesId].orEmpty())
                .ifEmpty { form.koreanNameByRule(korean[form.speciesId].orEmpty()) },
            english = form.formName(ENGLISH, speciesName = english[form.speciesId].orEmpty())
                .ifEmpty { english[form.speciesId].orEmpty() }
        )
    }
    return species + forms
}

/**
 * 폼의 한 언어 이름.
 *
 * - 종 이름까지 붙은 이름(`Mega Charizard X`)이 있으면 그것.
 * - 폼 이름만 있으면 종 이름을 앞에 붙인다(`테오키스 어택폼`). 이미 들어 있으면(`메가리자몽X`) 그대로.
 * - 그 언어 이름이 없으면 빈 문자열. 한국어 폼 이름은 절반쯤 비어 있다(`록스타 피카츄`).
 */
private fun NetworkPokemonForm.formName(languageId: Int, speciesName: String): String {
    val name: NetworkFormName? = forms.flatMap { it.names }.firstOrNull {
        it.languageId ==
            languageId
    }
    return when {
        name == null -> ""
        name.pokemonName.isNotBlank() -> name.pokemonName
        name.name.isBlank() -> ""
        speciesName.isEmpty() || name.name.contains(speciesName) -> name.name
        else -> "$speciesName ${name.name}"
    }
}

/**
 * 한국어 폼 이름이 없을 때 식별자(`charizard-gmax`)로 만든 이름.
 *
 * 거다이맥스·메가·리전폼은 한국어 이름이 통째로 빠져 있어, 종 이름만 쓰면 같은 종의 다른 모습과
 * 구분되지 않는다. 규칙이 없는 폼은 종 이름 그대로다.
 */
private fun NetworkPokemonForm.koreanNameByRule(speciesName: String): String {
    val form = name.split('-').drop(1)
    return when {
        "gmax" in form -> "거다이맥스 $speciesName"

        "mega" in form ->
            "메가$speciesName" +
                form.last().takeIf { it.length == 1 }?.uppercase().orEmpty()

        "totem" in form -> "주인 $speciesName"

        else -> REGION_NAMES.entries.firstOrNull {
            it.key in form
        }?.let { "${it.value} $speciesName" }
            ?: speciesName
    }
}

private val REGION_NAMES = mapOf(
    "alola" to "알로라",
    "galar" to "가라르",
    "hisui" to "히스이",
    "paldea" to "팔데아"
)

internal fun NetworkNamedEntry.asItemEntity() = ItemNameEntity(
    name = name,
    korean = names.firstOrNull { it.language.name == "ko" }?.name.orEmpty(),
    english = names.firstOrNull { it.language.name == "en" }?.name.orEmpty()
)
