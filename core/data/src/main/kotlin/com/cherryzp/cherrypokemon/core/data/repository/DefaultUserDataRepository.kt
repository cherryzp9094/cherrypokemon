package com.cherryzp.cherrypokemon.core.data.repository

import com.cherryzp.cherrypokemon.core.datastore.UserPreferencesDataSource
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.model.UserData
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class DefaultUserDataRepository @Inject constructor(
    private val userPreferences: UserPreferencesDataSource,
) : UserDataRepository {

    override val userData: Flow<UserData> = userPreferences.userData

    override suspend fun setLanguage(language: AppLanguage) = userPreferences.setLanguage(language)
}
