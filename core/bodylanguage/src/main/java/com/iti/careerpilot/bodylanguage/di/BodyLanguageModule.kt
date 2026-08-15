package com.iti.careerpilot.bodylanguage.di

import com.iti.careerpilot.bodylanguage.BodyLanguageAnalyzer
import com.iti.careerpilot.bodylanguage.BodyLanguageAnalyzerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class BodyLanguageModule {

    @Binds
    @ActivityRetainedScoped
    internal abstract fun bindBodyLanguageAnalyzer(
        impl: BodyLanguageAnalyzerImpl
    ): BodyLanguageAnalyzer
}
