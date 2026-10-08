package com.cherryzp.cherrypokemon.core.database

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.model.PokemonDetailEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.database.model.asExternalModel
import com.cherryzp.cherrypokemon.core.model.PokemonType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PokemonDaoTest {

    private lateinit var database: CherryPokemonDatabase
    private lateinit var pokemonDao: PokemonDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CherryPokemonDatabase::class.java
        ).setDriver(AndroidSQLiteDriver())
            .build()
        pokemonDao = database.pokemonDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertPokemons_getPokemonEntities_returnsSortedById() = runTest {
        pokemonDao.insertPokemons(
            listOf(
                pokemonEntity(id = 4, name = "charmander"),
                pokemonEntity(id = 1, name = "bulbasaur")
            )
        )

        val saved = pokemonDao.getPokemonEntities().first()

        assertEquals(listOf(1, 4), saved.map { it.id })
        assertEquals("bulbasaur", saved.first().name)
    }

    @Test
    fun insertPokemons_sameId_replacesRow() = runTest {
        pokemonDao.insertPokemons(listOf(pokemonEntity(id = 1, name = "bulbasaur")))
        pokemonDao.insertPokemons(listOf(pokemonEntity(id = 1, name = "fushigidane")))

        val saved = pokemonDao.getPokemonEntities().first()

        assertEquals(1, saved.size)
        assertEquals("fushigidane", saved.first().name)
    }

    @Test
    fun deleteAllPokemons_emptiesTable() = runTest {
        pokemonDao.insertPokemons(listOf(pokemonEntity(id = 1, name = "bulbasaur")))

        pokemonDao.deleteAllPokemons()

        assertEquals(emptyList<PokemonEntity>(), pokemonDao.getPokemonEntities().first())
    }

    @Test
    fun getPokemonDetailEntity_missingId_emitsNull() = runTest {
        assertNull(pokemonDao.getPokemonDetailEntity(pokeId = 1).first())
    }

    @Test
    fun insertPokemonDetail_keepsTypeOrder() = runTest {
        pokemonDao.insertPokemonDetail(
            PokemonDetailEntity(
                id = 1,
                name = "bulbasaur",
                imageUrl = "https://example.com/1.png",
                heightMeters = 0.7f,
                weightKilograms = 6.9f,
                types = listOf(PokemonType.Grass, PokemonType.Poison)
            )
        )

        val saved = pokemonDao.getPokemonDetailEntity(pokeId = 1).first()

        assertEquals(
            listOf(PokemonType.Grass, PokemonType.Poison),
            saved?.asExternalModel()?.types
        )
    }

    private fun pokemonEntity(id: Int, name: String) = PokemonEntity(
        id = id,
        name = name,
        imageUrl = "https://example.com/$id.png"
    )
}
