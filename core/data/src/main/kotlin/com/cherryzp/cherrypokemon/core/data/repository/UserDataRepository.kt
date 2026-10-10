package com.cherryzp.cherrypokemon.core.data.repository

import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.model.UserData
import kotlinx.coroutines.flow.Flow

/** 사용자가 고른 설정. */
interface UserDataRepository {
    val userData: Flow<UserData>

    suspend fun setLanguage(language: AppLanguage)
}
