package com.cherryzp.cherrypokemon.core.database

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
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
        // 테스트마다 다른 파일을 쓴다. 앞 테스트의 파일이 남아 있으면 createDatabase 가 실패한다.
        file = File(
            InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
            "migration-test-${UUID.randomUUID()}.db"
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

    @Test
    fun migrate2To3_fillsLocalizedColumnsWithEmptyText() = runTest {
        helper.createDatabase(version = 2).use { connection ->
            connection.execSQL(
                """
                INSERT INTO pokemon_details
                (id, name, image_url, height_meters, weight_kilograms, types, base_experience,
                 stat_hp, stat_attack, stat_defense, stat_special_attack, stat_special_defense,
                 stat_speed, abilities)
                VALUES (1, 'bulbasaur', 'https://example.com/1.png', 0.7, 6.9, 'Grass,Poison', 64,
                        45, 49, 49, 65, 65, 45, 'overgrow')
                """.trimIndent()
            )
        }

        helper.runMigrationsAndValidate(version = 3).use { connection ->
            connection.prepare(
                """
                SELECT name, stat_hp, localized_name_korean, genus_english, flavor_text_korean
                FROM pokemon_details WHERE id = 1
                """.trimIndent()
            ).use { statement ->
                assertTrue(statement.step())
                assertEquals("bulbasaur", statement.getText(0))
                assertEquals(45, statement.getInt(1))
                // 다음 갱신 때 실제 값이 들어온다.
                assertEquals("", statement.getText(2))
                assertEquals("", statement.getText(3))
                assertEquals("", statement.getText(4))
            }
        }
    }

    @Test
    fun migrate3To4_addsEmptyNameTables() = runTest {
        helper.createDatabase(version = 3).use { connection ->
            connection.execSQL(
                "INSERT INTO pokemons (id, name, image_url) VALUES (1, 'bulbasaur', '')"
            )
        }

        helper.runMigrationsAndValidate(version = 4).use { connection ->
            // 이름 캐시는 비어 있는 채로 생기고, 다음 갱신 때 채워진다.
            connection.prepare("SELECT COUNT(*) FROM type_names").use { statement ->
                assertTrue(statement.step())
                assertEquals(0, statement.getInt(0))
            }
            connection.prepare("SELECT COUNT(*) FROM ability_names").use { statement ->
                assertTrue(statement.step())
                assertEquals(0, statement.getInt(0))
            }
            connection.prepare("SELECT name FROM pokemons WHERE id = 1").use { statement ->
                assertTrue(statement.step())
                assertEquals("bulbasaur", statement.getText(0))
            }
        }
    }

    @Test
    fun migrate4To5_keepsTypeNamesAndAddsEmptyRelations() = runTest {
        helper.createDatabase(version = 4).use { connection ->
            connection.execSQL(
                "INSERT INTO type_names (type, korean, english) VALUES ('Grass', '풀', 'Grass')"
            )
        }

        helper.runMigrationsAndValidate(version = 5).use { connection ->
            connection.prepare(
                "SELECT korean, double_damage_from FROM type_names WHERE type = 'Grass'"
            ).use { statement ->
                assertTrue(statement.step())
                assertEquals("풀", statement.getText(0))
                // 상성은 비어 있고 다음 갱신 때 채워진다.
                assertEquals("", statement.getText(1))
            }
        }
    }

    @Test
    fun migrate5To6_addsEvolutionTableAndChainId() = runTest {
        helper.createDatabase(version = 5).use { connection ->
            connection.execSQL(
                "INSERT INTO pokemons (id, name, image_url) VALUES (1, 'bulbasaur', '')"
            )
        }

        helper.runMigrationsAndValidate(version = 6).use { connection ->
            connection.prepare("SELECT COUNT(*) FROM evolution_chain_members").use { statement ->
                assertTrue(statement.step())
                assertEquals(0, statement.getInt(0))
            }
            connection.prepare("SELECT name FROM pokemons WHERE id = 1").use { statement ->
                assertTrue(statement.step())
                assertEquals("bulbasaur", statement.getText(0))
            }
        }
    }

    @Test
    fun migrate6To7_addsEmptyNameTablesAndKeepsList() = runTest {
        helper.createDatabase(version = 6).use { connection ->
            connection.execSQL(
                "INSERT INTO pokemons (id, name, image_url) VALUES (25, 'pikachu', '')"
            )
        }

        helper.runMigrationsAndValidate(version = 7).use { connection ->
            for (table in listOf("pokemon_names", "item_names")) {
                connection.prepare("SELECT COUNT(*) FROM $table").use { statement ->
                    assertTrue(statement.step())
                    assertEquals(0, statement.getInt(0))
                }
            }
            connection.prepare("SELECT name FROM pokemons WHERE id = 25").use { statement ->
                assertTrue(statement.step())
                assertEquals("pikachu", statement.getText(0))
            }
        }
    }

    @Test
    fun migrate7To8_clearsCachedChainsSoTheyAreFetchedAgain() = runTest {
        helper.createDatabase(version = 7).use { connection ->
            connection.execSQL(
                "INSERT INTO evolution_chain_members " +
                    "(chain_id, pokemon_id, name, stage, min_level, item, trigger) " +
                    "VALUES (1, 1, 'bulbasaur', 1, NULL, NULL, NULL)"
            )
            connection.execSQL(
                "INSERT INTO pokemons (id, name, image_url) VALUES (25, 'pikachu', '')"
            )
        }

        helper.runMigrationsAndValidate(version = 8).use { connection ->
            // 예전 체인에는 교환·친밀도 칸이 비어 있다. 지워야 다음에 열 때 새 칸까지 받는다.
            connection.prepare("SELECT COUNT(*) FROM evolution_chain_members").use { statement ->
                assertTrue(statement.step())
                assertEquals(0, statement.getInt(0))
            }
            connection.prepare("SELECT name FROM pokemons WHERE id = 25").use { statement ->
                assertTrue(statement.step())
                assertEquals("pikachu", statement.getText(0))
            }
        }
    }

    @Test
    fun migrate8To9_keepsAbilityNamesWithEmptyDescriptions() = runTest {
        helper.createDatabase(version = 8).use { connection ->
            connection.execSQL(
                "INSERT INTO ability_names (name, korean, english) " +
                    "VALUES ('static', '정전기', 'Static')"
            )
        }

        helper.runMigrationsAndValidate(version = 9).use { connection ->
            connection.prepare(
                "SELECT korean, description_korean FROM ability_names WHERE name = 'static'"
            ).use { statement ->
                assertTrue(statement.step())
                assertEquals("정전기", statement.getText(0))
                // 설명이 비어 있으면 다음 갱신 때 다시 받는다.
                assertEquals("", statement.getText(1))
            }
        }
    }
}
