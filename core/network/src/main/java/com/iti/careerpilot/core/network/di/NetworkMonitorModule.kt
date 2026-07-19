package com.iti.careerpilot.core.network.di

import com.iti.careerpilot.core.network.connectivity.CareerPilotNetworkMonitor
import com.iti.common.network.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkMonitorModule {

    @Binds
    abstract fun bindNetworkMonitor(
        implementation: CareerPilotNetworkMonitor,
    ): NetworkMonitor
}
