package com.iti.careerpilot.ats.background

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.careerpilot.ats.domain.model.AiJobStatus
import com.iti.careerpilot.ats.domain.usecase.GetAiJobUseCase
import com.iti.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CvOptimizationService : Service() {
    @Inject lateinit var getAiJob: GetAiJobUseCase
    @Inject @Dispatcher(IO) lateinit var ioDispatcher: CoroutineDispatcher

    private val serviceJob = SupervisorJob()
    private lateinit var serviceScope: CoroutineScope
    private val pollingJobs = ConcurrentHashMap<Long, Job>()

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(serviceJob + ioDispatcher)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val workspaceId = intent?.getLongExtra(EXTRA_WORKSPACE_ID, INVALID_ID) ?: INVALID_ID
        val jobId = intent?.getLongExtra(EXTRA_JOB_ID, INVALID_ID) ?: INVALID_ID
        if (workspaceId == INVALID_ID || jobId == INVALID_ID) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
        val progress = intent?.getIntExtra(EXTRA_PROGRESS, 0)?.coerceIn(0, 100) ?: 0
        val currentStep = intent?.getStringExtra(EXTRA_CURRENT_STEP).orEmpty()
        val notification = CvOptimizationNotifications.processing(this, progress, currentStep)
        startForeground(CvOptimizationNotifications.progressNotificationId(jobId), notification)

        pollingJobs.computeIfAbsent(jobId) {
            serviceScope.launch {
                delay(POLL_INTERVAL)
                pollUntilTerminal(workspaceId, jobId)
            }.also { job ->
                job.invokeOnCompletion { pollingJobs.remove(jobId, job) }
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun pollUntilTerminal(
        workspaceId: Long,
        jobId: Long,
    ) {
        while (serviceJob.isActive) {
            when (val result = getAiJob(jobId)) {
                is CareerPilotResult.Success -> when (result.data.status) {
                    AiJobStatus.PENDING,
                    AiJobStatus.PROCESSING,
                    -> {
                        updateProgress(jobId, result.data)
                        delay(POLL_INTERVAL)
                    }
                    AiJobStatus.COMPLETED -> {
                        finishJob(
                            workspaceId = workspaceId,
                            jobId = jobId,
                            success = result.data.result != null,
                        )
                        return
                    }
                    AiJobStatus.FAILED,
                    AiJobStatus.UNKNOWN,
                    -> {
                        finishJob(workspaceId, jobId, success = false)
                        return
                    }
                }
                is CareerPilotResult.Error -> if (result.error.isRetryable()) {
                    updateWaitingForNetwork(jobId)
                    delay(POLL_INTERVAL)
                } else {
                    finishJob(workspaceId, jobId, success = false)
                    return
                }
            }
        }
    }

    private fun updateProgress(jobId: Long, job: AiJob) {
        getSystemService(NotificationManager::class.java).notify(
            CvOptimizationNotifications.progressNotificationId(jobId),
            CvOptimizationNotifications.processing(
                context = this,
                progress = job.progressPercentage,
                currentStep = job.currentStep,
            ),
        )
    }

    private fun updateWaitingForNetwork(jobId: Long) {
        getSystemService(NotificationManager::class.java).notify(
            CvOptimizationNotifications.progressNotificationId(jobId),
            CvOptimizationNotifications.processing(
                context = this,
                progress = 0,
                currentStep = getString(R.string.ats_cv_optimization_waiting_network),
            ),
        )
    }

    private fun finishJob(
        workspaceId: Long,
        jobId: Long,
        success: Boolean,
    ) {
        getSystemService(NotificationManager::class.java).cancel(
            CvOptimizationNotifications.progressNotificationId(jobId),
        )
        sendBroadcast(
            Intent(this, CvOptimizationNotificationReceiver::class.java)
                .setAction(CvOptimizationNotifications.ACTION_FINISHED)
                .putExtra(CvOptimizationNotifications.EXTRA_WORKSPACE_ID, workspaceId)
                .putExtra(CvOptimizationNotifications.EXTRA_JOB_ID, jobId)
                .putExtra(CvOptimizationNotifications.EXTRA_SUCCESS, success),
        )
        pollingJobs.remove(jobId)
        val remainingJobId = pollingJobs.keys.firstOrNull()
        if (remainingJobId == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } else {
            startForeground(
                CvOptimizationNotifications.progressNotificationId(remainingJobId),
                CvOptimizationNotifications.processing(
                    context = this,
                    progress = 0,
                    currentStep = getString(R.string.ats_cv_optimization_processing),
                ),
            )
        }
    }

    private fun NetworkError.isRetryable() = this == NetworkError.NO_INTERNET ||
        this == NetworkError.TIME_OUT ||
        this == NetworkError.SERVER ||
        this == NetworkError.TOO_MANY_REQUESTS ||
        this == NetworkError.ADDRESS_ERROR

    companion object {
        private const val EXTRA_WORKSPACE_ID = "workspace_id"
        private const val EXTRA_JOB_ID = "job_id"
        private const val EXTRA_PROGRESS = "progress"
        private const val EXTRA_CURRENT_STEP = "current_step"
        private const val INVALID_ID = -1L
        private val POLL_INTERVAL = 2.minutes

        fun start(context: Context, job: AiJob) {
            val intent = Intent(context, CvOptimizationService::class.java)
                .putExtra(EXTRA_WORKSPACE_ID, job.workspaceId)
                .putExtra(EXTRA_JOB_ID, job.id)
                .putExtra(EXTRA_PROGRESS, job.progressPercentage)
                .putExtra(EXTRA_CURRENT_STEP, job.currentStep)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
