package com.cherryzp.cherrypokemon.core.database

import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.cherryzp.cherrypokemon.core.database.dao.EvolutionChainDao
import com.cherryzp.cherrypokemon.core.database.dao.LocalizedNameDao
import com.cherryzp.cherrypokemon.core.database.dao.MoveDao
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.dao.RemoteKeyDao
import com.cherryzp.cherrypokemon.core.database.model.AbilityNameEntity
import com.cherryzp.cherrypokemon.core.database.model.EggGroupNameEntity
import com.cherryzp.cherrypokemon.core.database.model.EvolutionChainMemberEntity
import com.cherryzp.cherrypokemon.core.database.model.ItemNameEntity
import com.cherryzp.cherrypokemon.core.database.model.MoveEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonDetailEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonFilterEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonMoveEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonNameEntity
import com.cherryzp.cherrypokemon.core.database.model.RemoteKeyEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import com.cherryzp.cherrypokemon.core.database.util.PokemonAbilityListConverter
import com.cherryzp.cherrypokemon.core.database.util.PokemonTypeListConverter

@Database(
    entities = [
        PokemonEntity::class,
        PokemonDetailEntity::class,
        RemoteKeyEntity::class,
        TypeNameEntity::class,
        AbilityNameEntity::class,
        EvolutionChainMemberEntity::class,
        PokemonNameEntity::class,
        ItemNameEntity::class,
        EggGroupNameEntity::class,
        MoveEntity::class,
        PokemonMoveEntity::class,
        PokemonFilterEntity::class
    ],
    version = 15,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8, spec = ClearEvolutionChainsSpec::class),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15, spec = ClearPokemonNamesSpec::class)
    ],
    exportSchema = true
)
@ColumnTypeConverters(PokemonTypeListConverter::class, PokemonAbilityListConverter::class)
internal abstract class CherryPokemonDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao

    abstract fun remoteKeyDao(): RemoteKeyDao

    abstract fun localizedNameDao(): LocalizedNameDao

    abstract fun evolutionChainDao(): EvolutionChainDao

    abstract fun moveDao(): MoveDao
}

/**
 * 7 → 8: 진화 조건 칸을 늘렸다. 이미 받아 둔 체인에는 새 칸이 비어 있어 교환·친밀도 같은 조건이
 * 영영 안 보인다. 체인은 캐시이므로 지워서 다음에 열 때 다시 받게 한다.
 */
internal class ClearEvolutionChainsSpec : AutoMigrationSpec {
    override suspend fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL("DELETE FROM evolution_chain_members")
    }
}

/**
 * 14 → 15: 한국어 이름이 없는 폼(거다이맥스·메가·리전폼)에 이름을 만들어 넣게 됐다. 이미 받아 둔
 * 이름에는 종 이름만 들어 있으므로 지워서 목록을 열 때 다시 받게 한다.
 */
internal class ClearPokemonNamesSpec : AutoMigrationSpec {
    override suspend fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL("DELETE FROM pokemon_names")
    }
}
