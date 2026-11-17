package com.cherryzp.cherrypokemon.core.database

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cherryzp.cherrypokemon.core.database.dao.EvolutionChainDao
import com.cherryzp.cherrypokemon.core.database.dao.LocalizedNameDao
import com.cherryzp.cherrypokemon.core.database.model.EvolutionChainMemberEntity
import com.cherryzp.cherrypokemon.core.database.model.ItemNameEntity
import com.cherryzp.cherrypokemon.core.database.model.MoveEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonNameEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import com.cherryzp.cherrypokemon.core.database.model.asExternalModel
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.PokemonType
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
    fun getChainMembers_attachesHeldItemAndMoveTypeNames() = runTest {
        chainDao.insertChainMembers(
            listOf(
                member(pokemonId = 79, name = "slowpoke", stage = 1),
                member(pokemonId = 199, name = "slowking", stage = 2).copy(
                    trigger = "trade",
                    heldItem = "kings-rock"
                ),
                member(pokemonId = 700, name = "sylveon", stage = 2).copy(
                    minAffection = 2,
                    knownMoveType = "Fairy"
                )
            )
        )
        nameDao.insertItemNames(
            listOf(ItemNameEntity(name = "kings-rock", korean = "왕의징표석", english = "King's Rock"))
        )
        nameDao.insertTypeNames(
            listOf(TypeNameEntity(type = "Fairy", korean = "페어리", english = "Fairy"))
        )

        val stages = chainDao.getChainMembers(CHAIN_ID).first().asExternalModel()
        val slowking = stages.single { it.pokemonId == 199 }.condition
        val sylveon = stages.single { it.pokemonId == 700 }.condition

        assertEquals("trade", slowking?.trigger)
        assertEquals("왕의징표석", slowking?.heldItemName?.korean)
        assertEquals(PokemonType.Fairy, sylveon?.knownMoveType)
        assertEquals("페어리", sylveon?.knownMoveTypeName?.korean)
        assertEquals(2, sylveon?.minAffection)
    }

    @Test
    fun getChainItems_includesHeldItems() = runTest {
        chainDao.insertChainMembers(
            listOf(
                member(pokemonId = 79, name = "slowpoke", stage = 1),
                member(pokemonId = 80, name = "slowbro", stage = 2),
                member(pokemonId = 199, name = "slowking", stage = 2).copy(heldItem = "kings-rock")
            )
        )

        assertEquals(listOf("kings-rock"), chainDao.getChainItems(CHAIN_ID))
    }

    @Test
    fun getChainMembers_attachesKnownMoveName_andListsMissingMoves() = runTest {
        chainDao.insertChainMembers(
            listOf(
                member(pokemonId = 114, name = "tangela", stage = 1),
                member(
                    pokemonId = 465,
                    name = "tangrowth",
                    stage = 2
                ).copy(knownMove = "ancient-power")
            )
        )
        assertEquals(listOf("ancient-power"), chainDao.getMissingChainMoves(CHAIN_ID))

        database.moveDao().insertMoves(
            listOf(
                MoveEntity(
                    name = "ancient-power",
                    korean = "원시의힘",
                    english = "Ancient Power",
                    type = "Rock",
                    power = 60,
                    accuracy = 100,
                    pp = 5,
                    damageClass = "special"
                )
            )
        )

        val condition = chainDao.getChainMembers(CHAIN_ID).first().asExternalModel()
            .single { it.pokemonId == 465 }.condition
        assertEquals("원시의힘", condition?.knownMoveName?.korean)
        // 기술 표에 들어가면 더 받을 것이 없다.
        assertEquals(emptyList<String>(), chainDao.getMissingChainMoves(CHAIN_ID))
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
