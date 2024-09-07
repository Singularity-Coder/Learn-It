package com.singularitycoder.learnit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Vibrator
import android.widget.Toast


class ThisBroadcastReceiver : BroadcastReceiver() {

    /** Data sent to [TopicFragment] */
    override fun onReceive(context: Context, intent: Intent) {
//        when (intent.action) {
//            IntentKey.NOTIF_BTN_CLICK_BROADCAST -> {
//                val intent2 = Intent().apply {
//                    action = IntentKey.NOTIF_BTN_CLICK_BROADCAST_2
//                    putExtra(IntentExtraKey.NOTIF_BTN_CLICK_TYPE_2, intent.getStringExtra(IntentExtraKey.NOTIF_BTN_CLICK_TYPE))
//                }
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent2)
//            }
//
//            else -> Unit
//        }


        // we will use vibrator first
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(4000)

        Toast.makeText(context, "Alarm! Wake up! Wake up!", Toast.LENGTH_LONG).show()
        var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        // setting default ringtone
        val ringtone = RingtoneManager.getRingtone(context, alarmUri)

        // play ringtone
        ringtone.play()
    }
}