package com.maxtauro.airdroid.utils

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.google.gson.Gson
import com.maxtauro.airdroid.AirpodModel
import com.maxtauro.airdroid.notification.NotificationJobService
import com.maxtauro.airdroid.utils.NotificationUtil.Companion.EXTRA_AIRPOD_MODEL
import com.maxtauro.airdroid.utils.NotificationUtil.Companion.EXTRA_AIRPOD_NAME

@SuppressLint("LongLogTag")
object NotificationJobSchedulerUtil {

    private const val TAG = "NotificationJobSchedulerUtil"
    private const val JOB_ID = 1001

    // schedule the start of the service every 30 seconds
    fun scheduleJob(
        context: Context,
        airpodModel: AirpodModel? = null,
        deviceName: String? = null
    ) {
        Log.d(TAG, "Attempting to schedule Notification Job")

        val serviceComponent = ComponentName(context, NotificationJobService::class.java)
        val builder = JobInfo.Builder(JOB_ID, serviceComponent)

        val bundle = bundleData(airpodModel, deviceName)
        builder.setExtras(bundle)

        builder.setMinimumLatency((1 * 1000).toLong()) // wait at least
        builder.setOverrideDeadline((3 * 1000).toLong()) // maximum delay
        val jobScheduler = getSystemService(context, JobScheduler::class.java)
        jobScheduler?.schedule(builder.build())

        Log.d(TAG, "Notification Job Scheduled")
    }

    fun cancelJob(context: Context) {
        val jobScheduler = getSystemService(context, JobScheduler::class.java)
        jobScheduler?.cancelAll()
        Log.d(TAG, "Notification Job Canceled")
    }

    private fun bundleData(
        airpodModel: AirpodModel? = null,
        deviceName: String? = null
    ): PersistableBundle {
        val gson = Gson()

        return PersistableBundle().apply {
            airpodModel?.let {
                val airPodModelJson = gson.toJson(airpodModel)
                putString(EXTRA_AIRPOD_MODEL, airPodModelJson)
            }
            deviceName?.let {
                putString(EXTRA_AIRPOD_NAME, deviceName)
            }
        }
    }

}