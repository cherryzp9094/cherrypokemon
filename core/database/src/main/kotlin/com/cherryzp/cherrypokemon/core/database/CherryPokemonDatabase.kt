package com.cherryzp.cherrypokemon.core.database

import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.dao.RemoteKeyDao
import com.cherryzp.cherrypokemon.core.database.model.PokemonDetailEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.database.model.RemoteKeyEntity
import com.cherryzp.cherrypokemon.core.database.util.PokemonAbilityListConverter
import com.cherryzp.cherrypokemon.core.database.util.PokemonTypeListConverter

@Database(
    entities = [
        PokemonEntity::class,
        PokemonDetailEntity::class,
        RemoteKeyEntity::class
    ],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
    exportSchema = true
)
@ColumnTypeConverters(PokemonTypeListConverter::class, PokemonAbilityListConverter::class)
internal abstract class CherryPokemonDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao

    abstract fun remoteKeyDao(): RemoteKeyDao
}
