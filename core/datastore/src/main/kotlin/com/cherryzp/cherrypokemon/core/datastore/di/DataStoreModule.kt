package com.cherryzp.cherrypokemon.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.cherryzp.cherrypokemon.core.common.network.ApplicationScope
import com.cherryzp.cherrypokemon.core.common.network.CherryPokemonDispatchers.IO
import com.cherryzp.cherrypokemon.core.common.network.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

/** DataStore 는 파일 하나에 하나만 있어야 한다. 두 개를 만들면 같은 파일을 두고 다툰다. */
@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(IO) ioDispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = scope + ioDispatcher
    ) {
        context.preferencesDataStoreFile("user_preferences")
    }
}
