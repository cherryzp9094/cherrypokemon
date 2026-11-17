package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class CardColorsSaverTest {

    private val scope = SaverScope { true }

    @Test
    fun saveAndRestore_keepsEveryColor() {
        val colors = mutableStateMapOf(
            1 to Color(0xFF78C850),
            4 to Color(0xFFF08030),
            25 to Color(0xFFF8D030)
        )

        val saved = with(CardColorsSaver) { scope.save(colors) }!!
        val restored = CardColorsSaver.restore(saved)!!

        assertEquals(colors.toMap(), restored.toMap())
    }

    @Test
    fun saveAndRestore_empty_staysEmpty() {
        val saved = with(CardColorsSaver) { scope.save(mutableStateMapOf()) }!!

        assertEquals(emptyMap<Int, Color>(), CardColorsSaver.restore(saved)!!.toMap())
    }
}
