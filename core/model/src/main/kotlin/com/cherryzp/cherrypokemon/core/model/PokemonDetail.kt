package com.cherryzp.cherrypokemon.core.model

/**
 * 상세 화면에 보여줄 포켓몬.
 *
 * @param name PokeAPI 의 식별자(`bulbasaur`). 화면에는 [localizedName] 을 쓴다.
 * @param localizedName 언어별 이름(이상해씨 / Bulbasaur)
 * @param genus 분류(씨앗포켓몬 / Seed Pokémon)
 * @param flavorText 도감 설명
 * @param heightMeters 키(m)
 * @param weightKilograms 몸무게(kg)
 * @param baseExperience 쓰러뜨렸을 때 얻는 기초 경험치
 */
data class PokemonDetail(
    val id: Int,
    val name: String,
    val localizedName: LocalizedText,
    val genus: LocalizedText,
    val flavorText: LocalizedText,
    val imageUrl: String,
    val heightMeters: Float,
    val weightKilograms: Float,
    val types: List<PokemonTypeInfo>,
    val baseExperience: Int,
    val stats: PokemonStats,
    val abilities: List<PokemonAbility>,
)
