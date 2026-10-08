package com.cherryzp.cherrypokemon.core.common.network

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

/** 주입할 [kotlinx.coroutines.CoroutineDispatcher] 를 고른다. */
@Qualifier
@Retention(BINARY)
annotation class Dispatcher(val dispatcher: CherryPokemonDispatchers)

enum class CherryPokemonDispatchers {
    Default,
    IO,
}
