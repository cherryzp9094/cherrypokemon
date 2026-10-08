package com.cherryzp.cherrypokemon.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

/**
 * 앱의 back stack. 프로세스가 죽어도 복원된다.
 *
 * 시작 화면은 고정이고 항상 back stack 바닥에 있다.
 */
@Stable
class NavigationState internal constructor(val backStack: NavBackStack<NavKey>) {
    val currentKey: NavKey? get() = backStack.lastOrNull()
}

@Composable
fun rememberNavigationState(startKey: NavKey): NavigationState {
    val backStack = rememberNavBackStack(startKey)
    return remember(backStack) { NavigationState(backStack) }
}
