package com.iti.careerpilot.ats.background

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CvOptimizationNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != CvOptimizationNotifications.ACTION_FINISHED) return
        val workspaceId = intent.getLongExtra(
            CvOptimizationNotifications.EXTRA_WORKSPACE_ID,
            INVALID_ID,
        )
        val jobId = intent.getLongExtra(CvOptimizationNotifications.EXTRA_JOB_ID, INVALID_ID)
        if (workspaceId == INVALID_ID || jobId == INVALID_ID) return

        val success = intent.getBooleanExtra(CvOptimizationNotifications.EXTRA_SUCCESS, false)
        CvOptimizationNotifications.createChannels(context)
        context.getSystemService(NotificationManager::class.java).notify(
            CvOptimizationNotifications.resultNotificationId(jobId),
            CvOptimizationNotifications.terminal(
                context = context,
                workspaceId = workspaceId,
                jobId = jobId,
                success = success,
            ),
        )
    }

    private companion object {
        const val INVALID_ID = -1L
    }
}
