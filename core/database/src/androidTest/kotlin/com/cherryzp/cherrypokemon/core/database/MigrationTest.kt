package com.cherryzp.cherrypokemon.core.database

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** 스키마를 바꿀 때마다 이 테스트를 늘린다. */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        file = File(
            InstrumentationRegistry.getInstrumentation().targetContext.filesDir,
            "migration-test.db"
        ),
        driver = AndroidSQLiteDriver(),
        databaseClass = CherryPokemonDatabase::class
    )

    @Test
    fun migrate1To2_keepsSavedDetailAndFillsDefaults() = runTest {
        helper.createDatabase(version = 1).use { connection ->
            connection.execSQL(
                """
                INSERT INTO pokemon_details
                (id, name, image_url, height_meters, weight_kilograms, types)
                VALUES (1, 'bulbasaur', 'https://example.com/1.png', 0.7, 6.9, 'Grass,Poison')
                """.trimIndent()
            )
        }

        helper.runMigrationsAndValidate(version = 2).use { connection ->
            connection.prepare(
                "SELECT name, base_experience, stat_hp, abilities FROM pokemon_details WHERE id = 1"
            ).use { statement ->
                assertTrue(statement.step())
                assertEquals("bulbasaur", statement.getText(0))
                // 새 컬럼은 기본값으로 채워진다. 다음 갱신 때 실제 값이 들어온다.
                assertEquals(0, statement.getInt(1))
                assertEquals(0, statement.getInt(2))
                assertEquals("", statement.getText(3))
            }
        }
    }
}
