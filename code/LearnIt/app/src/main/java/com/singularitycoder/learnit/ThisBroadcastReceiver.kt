package com.singularitycoder.learnit

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.singularitycoder.learnit.helpers.constants.IntentExtraKey
import com.singularitycoder.learnit.helpers.constants.IntentKey
import com.singularitycoder.learnit.lockscreen.LockScreenActivity
import com.singularitycoder.learnit.subject.view.MainActivity


class ThisBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                if ((context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms().not()) {
                    val alarmIntent = Intent(context, MainActivity::class.java).apply {
                        action = IntentKey.ALARM_SETTINGS_BROADCAST
                        putExtra(IntentExtraKey.CANNOT_SET_ALARM, true)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(alarmIntent)
                }
            }

            IntentKey.NOTIF_BTN_CLICK_BROADCAST -> {
//                val intent2 = Intent().apply {
//                    action = IntentKey.NOTIF_BTN_CLICK_BROADCAST_2
//                    putExtra(IntentExtraKey.NOTIF_BTN_CLICK_TYPE_2, intent.getStringExtra(IntentExtraKey.NOTIF_BTN_CLICK_TYPE))
//                }
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent2)
            }

            IntentKey.REVISION_ALARM -> {
                val alarmIntent = Intent(context, LockScreenActivity::class.java).apply {
                    action = IntentKey.ALARM_SETTINGS_BROADCAST
                    putExtra(IntentExtraKey.TOPIC_ID_2, intent.getLongExtra(IntentExtraKey.TOPIC_ID, 0L))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(alarmIntent)
                /** Data sent to [MainActivity] */
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(alarmIntent)
            }

            else -> Unit
        }
    }
}