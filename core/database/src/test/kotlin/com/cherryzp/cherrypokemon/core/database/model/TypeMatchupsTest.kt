package com.cherryzp.cherrypokemon.core.database.model

import com.cherryzp.cherrypokemon.core.model.PokemonType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TypeMatchupsTest {

    private val relations = mapOf(
        "Grass" to typeEntity(
            type = PokemonType.Grass,
            doubleDamageFrom = listOf(
                PokemonType.Flying,
                PokemonType.Poison,
                PokemonType.Bug,
                PokemonType.Fire,
                PokemonType.Ice
            ),
            halfDamageFrom = listOf(
                PokemonType.Ground,
                PokemonType.Water,
                PokemonType.Grass,
                PokemonType.Electric
            )
        ),
        "Poison" to typeEntity(
            type = PokemonType.Poison,
            doubleDamageFrom = listOf(PokemonType.Ground, PokemonType.Psychic),
            halfDamageFrom = listOf(
                PokemonType.Fighting,
                PokemonType.Poison,
                PokemonType.Bug,
                PokemonType.Grass,
                PokemonType.Fairy
            )
        ),
        "Flying" to typeEntity(
            type = PokemonType.Flying,
            doubleDamageFrom = listOf(PokemonType.Rock, PokemonType.Electric, PokemonType.Ice),
            halfDamageFrom = listOf(PokemonType.Fighting, PokemonType.Bug, PokemonType.Grass),
            noDamageFrom = listOf(PokemonType.Ground)
        )
    )

    @Test
    fun singleType_usesRelationsAsIs() {
        val matchups = typeMatchups(listOf(PokemonType.Grass), relations)

        assertEquals(2f, matchups.multiplierOf(PokemonType.Fire))
        assertEquals(0.5f, matchups.multiplierOf(PokemonType.Water))
        // 1 배는 보여줄 게 없어 빠진다.
        assertNull(matchups.multiplierOf(PokemonType.Normal))
    }

    @Test
    fun dualType_multipliesBothRelations() {
        val matchups = typeMatchups(listOf(PokemonType.Grass, PokemonType.Poison), relations)

        // 풀 2배 × 독 0.5배
        assertEquals(1f, matchups.multiplierOf(PokemonType.Bug) ?: 1f)
        // 풀 0.5배 × 독 0.5배
        assertEquals(0.25f, matchups.multiplierOf(PokemonType.Grass))
        // 풀 0.5배 × 독 2배
        assertEquals(1f, matchups.multiplierOf(PokemonType.Ground) ?: 1f)
        assertEquals(2f, matchups.multiplierOf(PokemonType.Psychic))
    }

    @Test
    fun immunity_beatsEverything() {
        val matchups = typeMatchups(listOf(PokemonType.Flying, PokemonType.Poison), relations)

        // 비행이 땅을 무효로 만들면 독의 2배와 곱해도 0 이다.
        assertEquals(0f, matchups.multiplierOf(PokemonType.Ground))
        assertTrue(matchups.single { it.type.type == PokemonType.Ground }.isImmunity)
    }

    @Test
    fun unknownRelations_leaveEverythingAtOne() {
        assertTrue(typeMatchups(listOf(PokemonType.Dragon), emptyMap()).isEmpty())
        assertTrue(typeMatchups(emptyList(), relations).isEmpty())
    }

    @Test
    fun matchups_areSortedByMultiplier() {
        val matchups = typeMatchups(listOf(PokemonType.Grass, PokemonType.Poison), relations)

        assertEquals(
            matchups.map {
                it.multiplier
            }.sortedDescending(),
            matchups.map { it.multiplier }
        )
    }

    private fun List<com.cherryzp.cherrypokemon.core.model.TypeMatchup>.multiplierOf(
        type: PokemonType,
    ): Float? = firstOrNull { it.type.type == type }?.multiplier

    private fun typeEntity(
        type: PokemonType,
        doubleDamageFrom: List<PokemonType> = emptyList(),
        halfDamageFrom: List<PokemonType> = emptyList(),
        noDamageFrom: List<PokemonType> = emptyList(),
    ) = TypeNameEntity(
        type = type.name,
        korean = "",
        english = type.name,
        doubleDamageFrom = doubleDamageFrom,
        halfDamageFrom = halfDamageFrom,
        noDamageFrom = noDamageFrom
    )
}
