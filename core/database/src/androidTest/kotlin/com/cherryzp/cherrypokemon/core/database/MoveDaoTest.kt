package com.cherryzp.cherrypokemon.core.database

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cherryzp.cherrypokemon.core.database.dao.MoveDao
import com.cherryzp.cherrypokemon.core.database.model.MoveEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonMoveEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import com.cherryzp.cherrypokemon.core.database.model.asExternalModel
import com.cherryzp.cherrypokemon.core.model.PokemonType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoveDaoTest {

    private lateinit var database: CherryPokemonDatabase
    private lateinit var moveDao: MoveDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CherryPokemonDatabase::class.java
        ).setDriver(AndroidSQLiteDriver())
            .build()
        moveDao = database.moveDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun getPokemonMoves_ordersByLevelAndAttachesTypeNames() = runTest {
        moveDao.insertMoves(
            listOf(
                move("thunderbolt", "10만볼트", "Electric", power = 90),
                move("growl", "울음소리", "Normal", power = null),
                move("thunder-shock", "전기쇼크", "Electric", power = 40)
            )
        )
        moveDao.insertPokemonMoves(
            listOf(
                PokemonMoveEntity(pokemonId = 25, move = "thunderbolt", level = 36),
                PokemonMoveEntity(pokemonId = 25, move = "thunder-shock", level = 1),
                PokemonMoveEntity(pokemonId = 25, move = "growl", level = 1),
                // 다른 포켓몬 것은 섞이지 않는다.
                PokemonMoveEntity(pokemonId = 26, move = "thunderbolt", level = 1)
            )
        )
        database.localizedNameDao().insertTypeNames(
            listOf(TypeNameEntity(type = "Electric", korean = "전기", english = "Electric"))
        )

        val moves = moveDao.getPokemonMoves(25).first().asExternalModel()

        // 레벨 순, 같은 레벨은 이름 순이다.
        assertEquals(listOf("growl", "thunder-shock", "thunderbolt"), moves.map { it.name })
        assertEquals(PokemonType.Electric, moves.last().type.type)
        assertEquals("전기", moves.last().type.name.korean)
        assertEquals(null, moves.first().power)
        assertEquals(3, moveDao.countPokemonMoves(25))
    }

    private fun move(name: String, korean: String, type: String, power: Int?) = MoveEntity(
        name = name,
        korean = korean,
        english = name,
        type = type,
        power = power,
        accuracy = 100,
        pp = 30,
        damageClass = if (power == null) "status" else "special"
    )
}
