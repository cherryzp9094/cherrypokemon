package com.cherryzp.cherrypokemon.core.data

import com.cherryzp.cherrypokemon.core.database.dao.LocalizedNameDao
import com.cherryzp.cherrypokemon.core.database.model.AbilityNameEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** 메모리에만 담는 이름 캐시 대역. */
class FakeLocalizedNameDao : LocalizedNameDao {

    private val typeNames = MutableStateFlow(emptyList<TypeNameEntity>())
    private val abilityNames = MutableStateFlow(emptyList<AbilityNameEntity>())

    override fun getTypeNames(): Flow<List<TypeNameEntity>> = typeNames

    override fun getAbilityNames(): Flow<List<AbilityNameEntity>> = abilityNames

    override suspend fun getSavedTypes(): List<String> = typeNames.value.map { it.type }

    override suspend fun getSavedAbilities(): List<String> = abilityNames.value.map { it.name }

    override suspend fun insertTypeNames(names: List<TypeNameEntity>) {
        typeNames.value =
            (typeNames.value.filterNot { saved -> names.any { it.type == saved.type } } + names)
    }

    override suspend fun insertAbilityNames(names: List<AbilityNameEntity>) {
        abilityNames.value =
            (abilityNames.value.filterNot { saved -> names.any { it.name == saved.name } } + names)
    }
}
