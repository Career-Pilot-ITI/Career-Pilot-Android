package com.iti.careerpilot

import android.app.Application
import com.iti.careerpilot.ats.background.CvOptimizationNotifications
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CareerPilotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CvOptimizationNotifications.createChannels(this)
    }
}
