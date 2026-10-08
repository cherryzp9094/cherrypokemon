package com.cherryzp.cherrypokemon.core.navigation

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey

/**
 * 화면 이동을 맡는다. 화면(Composable)은 이 객체를 직접 받지 않고 콜백만 노출한다.
 * 콜백과 이 객체를 잇는 곳은 각 feature 의 entry builder 다.
 */
@Stable
class Navigator(private val navigationState: NavigationState) {

    fun navigate(key: NavKey) {
        navigationState.backStack.add(key)
    }

    /** 시작 화면은 지우지 않는다. */
    fun goBack() {
        if (navigationState.backStack.size > 1) {
            navigationState.backStack.removeAt(navigationState.backStack.lastIndex)
        }
    }
}
