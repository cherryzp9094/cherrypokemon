package com.cherryzp.cherrypokemon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherryzp.cherrypokemon.core.data.repository.UserDataRepository
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * 앱 전체가 쓰는 언어를 들고 있다.
 *
 * 고른 적이 없으면 기기 언어로 떨어뜨린다. 이 ViewModel 은 화면이 아니라 Activity 에 붙는다.
 */
@HiltViewModel
class MainViewModel @Inject constructor(userDataRepository: UserDataRepository) : ViewModel() {

    val language: StateFlow<AppLanguage?> = userDataRepository.userData
        .map { it.language }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            // 읽기 전에는 아직 모른다. 화면은 이때 아무것도 그리지 않는다.
            initialValue = null
        )
}
