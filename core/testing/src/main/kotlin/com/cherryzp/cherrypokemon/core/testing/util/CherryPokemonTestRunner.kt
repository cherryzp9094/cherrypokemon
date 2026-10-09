package com.cherryzp.cherrypokemon.core.testing.util

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/** Hilt 계측 테스트가 쓰는 Application 으로 바꾼다. */
class CherryPokemonTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application =
        super.newApplication(classLoader, HiltTestApplication::class.java.name, context)
}
