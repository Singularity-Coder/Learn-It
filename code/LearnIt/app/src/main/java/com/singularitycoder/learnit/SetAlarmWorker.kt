package com.singularitycoder.learnit

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.singularitycoder.learnit.helpers.constants.IntentKey
import com.singularitycoder.learnit.helpers.constants.WorkerData
import com.singularitycoder.learnit.helpers.db.LearnItDatabase
import com.singularitycoder.learnit.topic.model.Topic
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class SetAlarmWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ThisEntryPoint {
        fun db(): LearnItDatabase
    }

    override suspend fun doWork(): Result {
        return withContext(IO) {
            val appContext = context.applicationContext ?: throw IllegalStateException()
            val dbEntryPoint = EntryPointAccessors.fromApplication(appContext, ThisEntryPoint::class.java)
            val topicDao = dbEntryPoint.db().topicDao()
            val topicId = inputData.getLong(WorkerData.TOPIC_ID, 0L)

            setAlarm(topic = topicDao.getItemById(topicId))
            Result.success()
        }
    }

    //--------------------------------------------------------------------------------
    /**
     * Check permissions before worker starts
     *
     * Activates the alarms that are ON, but inactive because [AlarmManager] has
     * cancelled them for no reason.
     */
    private fun setAlarm(topic: Topic) {
        val intent = Intent(
            context.applicationContext,
            ThisBroadcastReceiver::class.java
        ).apply {
            action = IntentKey.DELIVER_ALARM
            flags = Intent.FLAG_RECEIVER_FOREGROUND
            putExtra(IntentKey.ALARM_DETAILS, topic.id)
        }

        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(
            /* context = */ context.applicationContext,
            /* requestCode = */ topic.id.toInt(),
            /* intent = */ intent,
            /* flags = */ flags
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmClockInfo = AlarmClockInfo(
            topic.nextSessionDate,
            pendingIntent
        )
        alarmManager.setAlarmClock(
            alarmClockInfo,
            pendingIntent
        )
    }
}