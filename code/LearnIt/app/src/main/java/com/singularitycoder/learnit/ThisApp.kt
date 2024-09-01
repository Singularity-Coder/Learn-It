package com.singularitycoder.learnit

import android.app.Application
import com.singularitycoder.learnit.helpers.AppPreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ThisApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(applicationContext)
    }
}
