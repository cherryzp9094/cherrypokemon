package com.cherryzp.cherrypokemon.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserPreferencesDataSourceTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Test
    fun userData_notChosenYet_hasNoLanguage() = runTest {
        assertNull(dataSource(this).userData.first().language)
    }

    @Test
    fun setLanguage_isReadBack() = runTest {
        val dataSource = dataSource(this)

        dataSource.setLanguage(AppLanguage.KOREAN)

        assertEquals(AppLanguage.KOREAN, dataSource.userData.first().language)
    }

    private fun dataSource(scope: TestScope) = UserPreferencesDataSource(
        PreferenceDataStoreFactory.create(scope = scope) {
            File(temporaryFolder.newFolder(), "user_preferences.preferences_pb")
        }
    )
}
