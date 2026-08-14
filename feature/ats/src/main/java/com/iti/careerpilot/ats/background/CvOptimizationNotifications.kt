package com.iti.careerpilot.ats.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.iti.careerpilot.ats.R

object CvOptimizationNotifications {
    internal const val ACTION_FINISHED = "com.iti.careerpilot.ats.CV_OPTIMIZATION_FINISHED"
    internal const val EXTRA_WORKSPACE_ID = "workspace_id"
    internal const val EXTRA_JOB_ID = "job_id"
    internal const val EXTRA_SUCCESS = "success"

    private const val PROGRESS_CHANNEL_ID = "cv_optimization_progress"
    private const val RESULT_CHANNEL_ID = "cv_optimization_results"
    private const val PROGRESS_NOTIFICATION_BASE = 20_000
    private const val RESULT_NOTIFICATION_BASE = 30_000

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    PROGRESS_CHANNEL_ID,
                    context.getString(R.string.ats_cv_optimization_channel),
                    NotificationManager.IMPORTANCE_LOW,
                ),
                NotificationChannel(
                    RESULT_CHANNEL_ID,
                    context.getString(R.string.ats_cv_optimization_result_channel),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            ),
        )
    }

    internal fun processing(
        context: Context,
        progress: Int,
        currentStep: String,
    ): Notification {
        val safeProgress = progress.coerceIn(0, 100)
        val step = currentStep.ifBlank {
            context.getString(R.string.ats_cv_optimization_processing)
        }
        return NotificationCompat.Builder(context, PROGRESS_CHANNEL_ID)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(context.getString(R.string.ats_cv_optimization_processing_title))
            .setContentText(
                context.getString(
                    R.string.ats_cv_optimization_progress,
                    safeProgress,
                    step,
                ),
            )
            .setProgress(100, safeProgress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }

    internal fun terminal(
        context: Context,
        workspaceId: Long,
        jobId: Long,
        success: Boolean,
    ): Notification = NotificationCompat.Builder(context, RESULT_CHANNEL_ID)
        .setSmallIcon(context.applicationInfo.icon)
        .setContentTitle(
            context.getString(
                if (success) {
                    R.string.ats_cv_optimization_complete_title
                } else {
                    R.string.ats_cv_optimization_failed_title
                },
            ),
        )
        .setContentText(
            context.getString(
                if (success) {
                    R.string.ats_cv_optimization_complete_message
                } else {
                    R.string.ats_cv_optimization_failed_message
                },
            ),
        )
        .setAutoCancel(success)
        .setCategory(NotificationCompat.CATEGORY_STATUS)
        .setContentIntent(
            if (success) resultPendingIntent(context, workspaceId, jobId) else null,
        )
        .build()

    internal fun progressNotificationId(jobId: Long) =
        PROGRESS_NOTIFICATION_BASE + (jobId % 10_000).toInt()

    internal fun resultNotificationId(jobId: Long) =
        RESULT_NOTIFICATION_BASE + (jobId % 10_000).toInt()

    private fun resultPendingIntent(
        context: Context,
        workspaceId: Long,
        jobId: Long,
    ): PendingIntent {
        val uri = Uri.Builder()
            .scheme("careerpilot")
            .authority("ats")
            .appendPath("optimized-cv")
            .appendPath(workspaceId.toString())
            .appendPath(jobId.toString())
            .build()
        val intent = Intent(Intent.ACTION_VIEW, uri).setPackage(context.packageName)
        return PendingIntent.getActivity(
            context,
            jobId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
