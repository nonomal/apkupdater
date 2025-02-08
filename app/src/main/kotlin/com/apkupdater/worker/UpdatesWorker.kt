package com.apkupdater.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.apkupdater.prefs.Prefs
import com.apkupdater.repository.UpdatesRepository
import com.apkupdater.util.UpdatesNotification
import com.apkupdater.util.millisUntilHour
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class UpdatesWorker(
    context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams), KoinComponent {

    companion object: KoinComponent {
        private const val TAG = "UpdatesWorker"
        private val prefs: Prefs by inject()

        fun cancel(workManager: WorkManager) = workManager.cancelUniqueWork(TAG)

        fun launch(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<UpdatesWorker>(getDays(), TimeUnit.DAYS)
                .setInitialDelay(
                    millisUntilHour(prefs.alarmHour.get()) + randomDelay(),
                    TimeUnit.MILLISECONDS
                ).build()
            workManager.enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        private fun randomDelay() = if (prefs.useApkMirror.get())
            Random.nextLong(0, 59 * 60 * 1_000)
        else
            Random.nextLong(-5 * 60 * 1_000, 5 * 60 * 1_000)

        private fun getDays() = when(prefs.alarmFrequency.get()) {
            0 -> 1L
            1 -> 3L
            2 -> 7L
            else -> 1L
        }
    }

    private val updatesRepository: UpdatesRepository by inject()
    private val notification: UpdatesNotification by inject()

    override suspend fun doWork(): Result {
        updatesRepository.updates().collect {
            if (it.isNotEmpty()) {
                notification.showUpdateNotification(it.size)
            }
        }
        return Result.success()
    }

}
