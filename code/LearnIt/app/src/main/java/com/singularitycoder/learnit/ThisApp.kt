package com.singularitycoder.learnit

import android.app.Application
import com.singularitycoder.learnit.helpers.AppPreferences
import dagger.hilt.android.HiltAndroidApp

// Ask permission in one place for best experience
// learn how koin works
// AlarmAlertFullScreen
// AlarmSetter
// AlarmApplication
// remove code n check -
// hold the start revision to dismiss alarm
// gradual fade in of alarm

@HiltAndroidApp
class ThisApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(applicationContext)
    }
}
