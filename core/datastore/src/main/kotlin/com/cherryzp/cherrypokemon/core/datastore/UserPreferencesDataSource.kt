package com.cherryzp.cherrypokemon.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.model.UserData
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 사용자가 고른 설정을 읽고 쓴다.
 *
 * 고른 적이 없으면 [UserData.language] 가 null 이다. 기기 언어로 떨어뜨릴지는 읽는 쪽이 정한다.
 */
class UserPreferencesDataSource @Inject constructor(
    private val preferences: DataStore<Preferences>,
) {
    val userData: Flow<UserData> = preferences.data.map { it.toUserData() }

    suspend fun setLanguage(language: AppLanguage) {
        preferences.edit { it[LANGUAGE] = language.name }
    }

    private fun Preferences.toUserData() = UserData(
        language = this[LANGUAGE]?.let { saved ->
            AppLanguage.entries.firstOrNull { it.name == saved }
        }
    )

    private companion object {
        val LANGUAGE = stringPreferencesKey("language")
    }
}
