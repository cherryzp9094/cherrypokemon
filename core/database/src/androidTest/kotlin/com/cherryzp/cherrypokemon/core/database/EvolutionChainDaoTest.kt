package com.cherryzp.cherrypokemon.core.database

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cherryzp.cherrypokemon.core.database.dao.EvolutionChainDao
import com.cherryzp.cherrypokemon.core.database.dao.LocalizedNameDao
import com.cherryzp.cherrypokemon.core.database.model.EvolutionChainMemberEntity
import com.cherryzp.cherrypokemon.core.database.model.ItemNameEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonNameEntity
import com.cherryzp.cherrypokemon.core.database.model.asExternalModel
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EvolutionChainDaoTest {

    private lateinit var database: CherryPokemonDatabase
    private lateinit var chainDao: EvolutionChainDao
    private lateinit var nameDao: LocalizedNameDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CherryPokemonDatabase::class.java
        ).setDriver(AndroidSQLiteDriver())
            .build()
        chainDao = database.evolutionChainDao()
        nameDao = database.localizedNameDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun getChainMembers_attachesPokemonAndItemNames() = runTest {
        chainDao.insertChainMembers(
            listOf(
                member(pokemonId = 25, name = "pikachu", stage = 1),
                member(pokemonId = 26, name = "raichu", stage = 2, item = "thunder-stone")
            )
        )
        // 라이츄는 열어본 적이 없지만 이름 표에는 있다.
        nameDao.insertPokemonNames(
            listOf(
                PokemonNameEntity(id = 25, korean = "피카츄", english = "Pikachu"),
                PokemonNameEntity(id = 26, korean = "라이츄", english = "Raichu")
            )
        )
        nameDao.insertItemNames(
            listOf(
                ItemNameEntity(name = "thunder-stone", korean = "천둥의돌", english = "Thunder Stone")
            )
        )

        val stages = chainDao.getChainMembers(CHAIN_ID).first().asExternalModel()

        assertEquals(listOf("피카츄", "라이츄"), stages.map { it.localizedName.korean })
        assertEquals(
            LocalizedText(korean = "천둥의돌", english = "Thunder Stone"),
            stages.last().condition?.itemName
        )
    }

    @Test
    fun getChainItems_returnsEachItemOnce() = runTest {
        chainDao.insertChainMembers(
            listOf(
                member(pokemonId = 133, name = "eevee", stage = 1),
                member(pokemonId = 134, name = "vaporeon", stage = 2, item = "water-stone"),
                member(pokemonId = 135, name = "jolteon", stage = 2, item = "thunder-stone"),
                member(pokemonId = 136, name = "flareon", stage = 2, item = "fire-stone"),
                member(pokemonId = 470, name = "leafeon", stage = 2, item = "leaf-stone"),
                member(pokemonId = 471, name = "glaceon", stage = 2, item = "leaf-stone")
            )
        )

        assertEquals(
            setOf("water-stone", "thunder-stone", "fire-stone", "leaf-stone"),
            chainDao.getChainItems(CHAIN_ID).toSet()
        )
        assertEquals(4, chainDao.getChainItems(CHAIN_ID).size)
    }

    private fun member(pokemonId: Int, name: String, stage: Int, item: String? = null) =
        EvolutionChainMemberEntity(
            chainId = CHAIN_ID,
            pokemonId = pokemonId,
            name = name,
            stage = stage,
            minLevel = null,
            item = item,
            trigger = null
        )

    private companion object {
        const val CHAIN_ID = 10
    }
}
