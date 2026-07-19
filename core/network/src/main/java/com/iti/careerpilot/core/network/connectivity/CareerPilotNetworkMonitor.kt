package com.iti.careerpilot.core.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import com.iti.common.network.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CareerPilotNetworkMonitor @Inject constructor(
    @ApplicationContext context: Context,
) : NetworkMonitor {

    private val connectivityManager = requireNotNull(
        context.getSystemService<ConnectivityManager>(),
    ) {
        "ConnectivityManager is unavailable on this device."
    }

    private var currentNetwork: Network? = connectivityManager.activeNetwork
    private var currentCapabilities: NetworkCapabilities? =
        currentNetwork?.let(connectivityManager::getNetworkCapabilities)
    private var isCurrentNetworkBlocked: Boolean = false

    private val _isOnline = MutableStateFlow(
        value = currentCapabilities.hasValidatedInternetConnection(),
    )

    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            currentNetwork = network
            currentCapabilities = null
            isCurrentNetworkBlocked = false
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            if (network == currentNetwork) {
                currentCapabilities = networkCapabilities
                updateNetworkStatus()
            }
        }

        override fun onBlockedStatusChanged(
            network: Network,
            blocked: Boolean,
        ) {
            if (network == currentNetwork) {
                isCurrentNetworkBlocked = blocked
                updateNetworkStatus()
            }
        }

        override fun onLost(network: Network) {
            if (network == currentNetwork) {
                clearCurrentNetwork()
            }
        }

        override fun onUnavailable() {
            clearCurrentNetwork()
        }
    }

    init {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun updateNetworkStatus() {
        _isOnline.value =
            !isCurrentNetworkBlocked && currentCapabilities.hasValidatedInternetConnection()
    }

    private fun clearCurrentNetwork() {
        currentNetwork = null
        currentCapabilities = null
        isCurrentNetworkBlocked = false
        _isOnline.value = false
    }
}

private fun NetworkCapabilities?.hasValidatedInternetConnection(): Boolean {
    if (this == null) return false

    return hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
