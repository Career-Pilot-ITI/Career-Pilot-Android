package com.iti.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton
import com.iti.common.dispatcher.di.ApplicationScope
import com.iti.core.datastore.EncryptedProfileSerializer
import com.iti.core.datastore.EncryptedTokensSerializer
import com.iti.core.datastore.UserTokens
import com.iti.core.datastore.models.UserProfile

@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserProfileDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        serializer: EncryptedProfileSerializer,
    ): DataStore<UserProfile> = DataStoreFactory.create(
        serializer = serializer,
        scope = scope,
    ) {
        context.dataStoreFile("user_profile.enc.json")
    }

    @Provides
    @Singleton
    fun provideUserTokensDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        serializer: EncryptedTokensSerializer,
    ): DataStore<UserTokens> = DataStoreFactory.create(
        serializer = serializer,
        scope = scope,
    ) {
        context.dataStoreFile("user_tokens.enc.json")
    }
}
