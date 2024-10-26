package com.singularitycoder.learnit

import android.app.Service
import android.content.Intent
import android.os.IBinder

// Service_RingAlarm
class RingAlarmService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}