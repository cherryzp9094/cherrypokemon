package com.cherryzp.cherrypokemon.core.data

import com.cherryzp.cherrypokemon.core.database.dao.LocalizedNameDao
import com.cherryzp.cherrypokemon.core.database.model.AbilityNameEntity
import com.cherryzp.cherrypokemon.core.database.model.ItemNameEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonNameEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** 메모리에만 담는 이름 캐시 대역. */
class FakeLocalizedNameDao : LocalizedNameDao {

    private val typeNames = MutableStateFlow(emptyList<TypeNameEntity>())
    private val abilityNames = MutableStateFlow(emptyList<AbilityNameEntity>())

    override fun getTypeNames(): Flow<List<TypeNameEntity>> = typeNames

    override fun getAbilityNames(): Flow<List<AbilityNameEntity>> = abilityNames

    override suspend fun getSavedTypes(): List<String> = typeNames.value.map { it.type }

    override suspend fun getSavedAbilities(): List<String> = abilityNames.value
        .filter { it.descriptionKorean.isNotEmpty() || it.descriptionEnglish.isNotEmpty() }
        .map { it.name }

    override suspend fun insertTypeNames(names: List<TypeNameEntity>) {
        typeNames.value =
            (typeNames.value.filterNot { saved -> names.any { it.type == saved.type } } + names)
    }

    override suspend fun insertAbilityNames(names: List<AbilityNameEntity>) {
        abilityNames.value =
            (abilityNames.value.filterNot { saved -> names.any { it.name == saved.name } } + names)
    }

    /** 받아 둔 포켓몬 이름. [FakePokemonDao] 가 목록에 붙일 때 읽는다. */
    val pokemonNames = MutableStateFlow(emptyMap<Int, PokemonNameEntity>())

    private val itemNames = MutableStateFlow(emptyMap<String, ItemNameEntity>())

    val savedItemNames: Map<String, ItemNameEntity> get() = itemNames.value

    override fun getPokemonName(id: Int): Flow<PokemonNameEntity?> = pokemonNames.map { it[id] }

    override suspend fun countPokemonNames(): Int = pokemonNames.value.size

    override suspend fun insertPokemonNames(names: List<PokemonNameEntity>) {
        pokemonNames.value = pokemonNames.value + names.associateBy { it.id }
    }

    override suspend fun getSavedItems(): List<String> = itemNames.value.keys.toList()

    override suspend fun insertItemNames(names: List<ItemNameEntity>) {
        itemNames.value = itemNames.value + names.associateBy { it.name }
    }
}
