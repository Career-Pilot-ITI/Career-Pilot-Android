package com.iti.common.snackbar.di

import com.iti.common.snackbar.DefaultSnackbarController
import com.iti.common.snackbar.SnackbarController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SnackbarModule {

    @Binds
    @Singleton
    abstract fun bindSnackbarController(
        implementation: DefaultSnackbarController,
    ): SnackbarController
}
