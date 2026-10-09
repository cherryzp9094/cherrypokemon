package com.cherryzp.cherrypokemon.core.testing.data

import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonStats
import com.cherryzp.cherrypokemon.core.model.PokemonType

/** 테스트에서 쓰는 포켓몬 목록. */
val testPokemons = listOf(
    Pokemon(id = 1, name = "bulbasaur", imageUrl = testImageUrl(1)),
    Pokemon(id = 4, name = "charmander", imageUrl = testImageUrl(4)),
    Pokemon(id = 7, name = "squirtle", imageUrl = testImageUrl(7))
)

/** 테스트에서 쓰는 포켓몬 상세. */
val testPokemonDetail = PokemonDetail(
    id = 1,
    name = "bulbasaur",
    imageUrl = testImageUrl(1),
    heightMeters = 0.7f,
    weightKilograms = 6.9f,
    types = listOf(PokemonType.Grass, PokemonType.Poison),
    baseExperience = 64,
    stats = PokemonStats(
        hp = 45,
        attack = 49,
        defense = 49,
        specialAttack = 65,
        specialDefense = 65,
        speed = 45
    ),
    abilities = listOf(
        PokemonAbility(name = "overgrow", isHidden = false),
        PokemonAbility(name = "chlorophyll", isHidden = true)
    )
)

private fun testImageUrl(id: Int) = "https://example.com/pokemon/$id.png"
