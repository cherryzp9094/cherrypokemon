package com.cherryzp.cherrypokemon.core.database

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.model.LocalizedTextEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonDetailEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonStatsEntity
import com.cherryzp.cherrypokemon.core.database.model.asExternalModel
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
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
                localizedName = LocalizedTextEntity(korean = "이상해씨", english = "Bulbasaur"),
                genus = LocalizedTextEntity(korean = "씨앗포켓몬", english = "Seed Pokémon"),
                flavorText = LocalizedTextEntity(korean = "", english = ""),
                imageUrl = "https://example.com/1.png",
                heightMeters = 0.7f,
                weightKilograms = 6.9f,
                types = listOf(PokemonType.Grass, PokemonType.Poison),
                baseExperience = 64,
                stats = PokemonStatsEntity(
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
        )

        val saved = pokemonDao.getPokemonDetailEntity(pokeId = 1).first()

        assertEquals(
            listOf(PokemonType.Grass, PokemonType.Poison),
            saved?.asExternalModel()?.types?.map { it.type }
        )
    }

    private fun pokemonEntity(id: Int, name: String) = PokemonEntity(
        id = id,
        name = name,
        imageUrl = "https://example.com/$id.png"
    )

    @Test
    fun insertPokemonDetail_keepsStatsAndAbilities() = runTest {
        pokemonDao.insertPokemonDetail(bulbasaurDetail())

        val saved = pokemonDao.getPokemonDetailEntity(pokeId = 1).first()?.asExternalModel()

        assertEquals(45, saved?.stats?.hp)
        assertEquals(318, saved?.stats?.total)
        assertEquals(64, saved?.baseExperience)
        assertEquals(listOf("overgrow", "chlorophyll"), saved?.abilities?.map { it.name })
        assertEquals(listOf(false, true), saved?.abilities?.map { it.isHidden })
    }

    @Test
    fun insertPokemonDetail_keepsBothLanguages() = runTest {
        pokemonDao.insertPokemonDetail(bulbasaurDetail())

        val saved = pokemonDao.getPokemonDetailEntity(pokeId = 1).first()?.asExternalModel()

        assertEquals("이상해씨", saved?.localizedName?.korean)
        assertEquals("Bulbasaur", saved?.localizedName?.english)
        assertEquals("씨앗포켓몬", saved?.genus?.korean)
        assertEquals("A strange seed.", saved?.flavorText?.english)
    }

    @Test
    fun searchPagingSource_findsByNameAndNumber() = runTest {
        pokemonDao.insertPokemons(
            listOf(
                PokemonEntity(id = 25, name = "pikachu", imageUrl = ""),
                PokemonEntity(id = 150, name = "mewtwo", imageUrl = ""),
                PokemonEntity(id = 1500, name = "nothing", imageUrl = "")
            )
        )

        // 이름은 가운데 글자도 찾는다.
        assertEquals(listOf("pikachu"), pokemonDao.searchPagingSource("kach").names())
        // 번호는 앞에서부터 맞춘다. 150 은 1500 도 찾는다.
        assertEquals(listOf("mewtwo", "nothing"), pokemonDao.searchPagingSource("150").names())
        assertEquals(emptyList<String>(), pokemonDao.searchPagingSource("없는이름").names())
    }

    @Test
    fun searchPagingSource_isCaseInsensitive() = runTest {
        pokemonDao.insertPokemons(listOf(PokemonEntity(id = 25, name = "pikachu", imageUrl = "")))

        assertEquals(listOf("pikachu"), pokemonDao.searchPagingSource("PIKA").names())
    }

    @Test
    fun countPokemons_returnsSavedCount() = runTest {
        assertEquals(0, pokemonDao.countPokemons())

        pokemonDao.insertPokemons(listOf(PokemonEntity(id = 1, name = "bulbasaur", imageUrl = "")))

        assertEquals(1, pokemonDao.countPokemons())
    }

    /** PagingSource 를 한 번 읽어 이름만 꺼낸다. */
    private suspend fun PagingSource<Int, PokemonEntity>.names(): List<String> {
        val page = load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )
        return (page as PagingSource.LoadResult.Page).data.map { it.name }
    }

    private fun bulbasaurDetail() = PokemonDetailEntity(
        id = 1,
        name = "bulbasaur",
        localizedName = LocalizedTextEntity(korean = "이상해씨", english = "Bulbasaur"),
        genus = LocalizedTextEntity(korean = "씨앗포켓몬", english = "Seed Pokémon"),
        flavorText = LocalizedTextEntity(korean = "씨앗이 자란다.", english = "A strange seed."),
        imageUrl = "https://example.com/1.png",
        heightMeters = 0.7f,
        weightKilograms = 6.9f,
        types = listOf(PokemonType.Grass, PokemonType.Poison),
        baseExperience = 64,
        stats = PokemonStatsEntity(
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
}
