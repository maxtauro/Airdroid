package com.maxtauro.airdroid.utils

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.maxtauro.airdroid.bluetooth.services.RssiUpdateService

object RssiUpdateSchedulerUtil {

    private const val TAG = "RssiUpdateSchedulerUtil"
    private const val JOB_ID = 1002

    fun scheduleJob(context: Context) {
        Log.d(TAG, "Attempting to schedule RssiUpdate Job from $context")

        val serviceComponent = ComponentName(context, RssiUpdateService::class.java)
        val builder = JobInfo.Builder(JOB_ID, serviceComponent)

        builder.setMinimumLatency(2000L) // wait 15s to start job
        val jobScheduler = getSystemService(context, JobScheduler::class.java)
        jobScheduler?.schedule(builder.build())

        Log.d(TAG, "RssiUpdate Job Scheduled")
    }

    fun cancelJob(context: Context) {
        val jobScheduler = getSystemService(context, JobScheduler::class.java)
        jobScheduler?.cancel(JOB_ID)

        Log.d(TAG, "RssiUpdate Job Canceled")
    }
}