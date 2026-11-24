package com.cherryzp.cherrypokemon.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** PokeAPI GraphQL 요청 본문. [variables] 는 질의의 `$id` 같은 자리에 들어간다. */
@Serializable
data class NetworkGraphQlRequest(val query: String, val variables: Map<String, Int> = emptyMap())

/**
 * 모든 종과 폼의 언어별 이름. GraphQL 한 번으로 받는다.
 *
 * REST 로는 종마다 `/pokemon-species/{id}` 를 불러야 해서 목록 전체에 천 번이 넘게 든다.
 * 질의가 틀리면 `data` 없이 `errors` 만 온다.
 */
@Serializable
data class NetworkPokemonNamesResponse(val data: NetworkPokemonNames? = null)

@Serializable
data class NetworkPokemonNames(
    @SerialName("pokemonspeciesname")
    val speciesNames: List<NetworkLanguageName> = emptyList(),
    /** 10000 번 이후의 폼(메가진화, 거다이맥스 등). 종 번호와 폼 이름을 함께 받는다. */
    @SerialName("pokemon")
    val forms: List<NetworkPokemonForm> = emptyList(),
    /** 폼 낱말과 그 한국어 이름. 한국어 이름이 빠진 폼을 이 낱말로 구분한다. */
    @SerialName("pokemonform")
    val formWords: List<NetworkFormWord> = emptyList(),
)

/** 폼 낱말(`female`, `single-strike`)과 그 한국어 이름(`암컷의 모습`). */
@Serializable
data class NetworkFormWord(
    @SerialName("form_name") val formName: String = "",
    @SerialName("pokemonformnames") val names: List<NetworkFormName> = emptyList(),
)

/** 한 언어의 종 이름. 언어는 PokeAPI 언어 ID 다(한국어 3, 영어 9). */
@Serializable
data class NetworkLanguageName(
    val name: String = "",
    @SerialName("pokemon_species_id") val speciesId: Int = 0,
    @SerialName("language_id") val languageId: Int = 0,
)

@Serializable
data class NetworkPokemonForm(
    val id: Int = 0,
    val name: String = "",
    @SerialName("pokemon_species_id") val speciesId: Int = 0,
    @SerialName("pokemonforms") val forms: List<NetworkFormNames> = emptyList(),
)

@Serializable
data class NetworkFormNames(
    @SerialName("pokemonformnames") val names: List<NetworkFormName> = emptyList(),
)

/**
 * 폼의 언어별 이름.
 *
 * @param pokemonName 종 이름까지 붙은 이름(`Mega Charizard X`). 비어 있는 경우가 많다
 * @param name 폼 이름만(`어택폼`) 이거나 전체 이름(`메가리자몽X`)
 */
@Serializable
data class NetworkFormName(
    @SerialName("pokemon_name") val pokemonName: String = "",
    val name: String = "",
    @SerialName("language_id") val languageId: Int = 0,
)
