package com.cherryzp.cherrypokemon.core.database

import androidx.room3.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cherryzp.cherrypokemon.core.database.dao.LocalizedNameDao
import com.cherryzp.cherrypokemon.core.database.model.AbilityNameEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalizedNameDaoTest {

    private lateinit var database: CherryPokemonDatabase
    private lateinit var localizedNameDao: LocalizedNameDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            CherryPokemonDatabase::class.java
        ).build()
        localizedNameDao = database.localizedNameDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun insertTypeNames_areReadBackAndListedAsSaved() = runTest {
        localizedNameDao.insertTypeNames(
            listOf(TypeNameEntity(type = "Grass", korean = "풀", english = "Grass"))
        )

        assertEquals("풀", localizedNameDao.getTypeNames().first().single().korean)
        assertEquals(listOf("Grass"), localizedNameDao.getSavedTypes())
    }

    @Test
    fun insertAbilityNames_sameNameTwice_keepsOneRow() = runTest {
        localizedNameDao.insertAbilityNames(
            listOf(AbilityNameEntity(name = "overgrow", korean = "심록", english = "Overgrow"))
        )
        localizedNameDao.insertAbilityNames(
            listOf(AbilityNameEntity(name = "overgrow", korean = "심록", english = "Overgrow"))
        )

        assertEquals(1, localizedNameDao.getAbilityNames().first().size)
    }
}
