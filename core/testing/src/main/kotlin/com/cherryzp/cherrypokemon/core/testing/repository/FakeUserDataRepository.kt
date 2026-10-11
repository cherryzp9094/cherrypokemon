package com.cherryzp.cherrypokemon.core.testing.repository

import com.cherryzp.cherrypokemon.core.data.repository.UserDataRepository
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.model.UserData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** 계측 테스트와 ViewModel 테스트에서 쓰는 [UserDataRepository] 대역. */
@Singleton
class FakeUserDataRepository @Inject constructor() : UserDataRepository {

    private val state = MutableStateFlow(UserData(language = null))

    override val userData: Flow<UserData> = state

    override suspend fun setLanguage(language: AppLanguage) {
        state.value = UserData(language = language)
    }
}
