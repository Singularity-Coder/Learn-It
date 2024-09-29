package com.singularitycoder.learnit.helpers

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import java.util.Locale

// https://github.com/enricocid/Music-Player-GO
class AppPreferences(context: Context) {

    companion object {
        /** Singleton prevents multiple instances of AppPreferences opening at the same time. */
        @Volatile
        private var INSTANCE: AppPreferences? = null

        fun init(context: Context): AppPreferences {
            /** if the INSTANCE is not null, then return it, if it is, then create the preferences */
            return INSTANCE ?: synchronized(this) {
                val instance = AppPreferences(context)
                INSTANCE = instance
                instance
            }
        }

        fun getInstance(): AppPreferences {
            return INSTANCE ?: error("Preferences not initialized!")
        }
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var hasCompletedTutorial: Boolean
        get() = sharedPreferences.getBoolean("COMPLETED_TUTORIAL", false)
        set(value) = sharedPreferences.edit { putBoolean("COMPLETED_TUTORIAL", value) }

    var hasGrantedAllPermissions: Boolean
        get() = sharedPreferences.getBoolean("GRANTED_ALL_PERMISSIONS", false)
        set(value) = sharedPreferences.edit { putBoolean("GRANTED_ALL_PERMISSIONS", value) }

    var hasNotificationPermission: Boolean
        get() = sharedPreferences.getBoolean("NOTIFICATION_PERMISSION", false)
        set(value) = sharedPreferences.edit { putBoolean("NOTIFICATION_PERMISSION", value) }

    var hasAlarmPermission: Boolean
        get() = sharedPreferences.getBoolean("ALARM_PERMISSION", false)
        set(value) = sharedPreferences.edit { putBoolean("ALARM_PERMISSION", value) }

    var hasBatteryOptimisePermission: Boolean
        get() = sharedPreferences.getBoolean("BATTERY_OPTIMISE_PERMISSION", false)
        set(value) = sharedPreferences.edit { putBoolean("BATTERY_OPTIMISE_PERMISSION", value) }

    var hasDndPermission: Boolean
        get() = sharedPreferences.getBoolean("DND_PERMISSION", false)
        set(value) = sharedPreferences.edit { putBoolean("DND_PERMISSION", value) }

    var hasStoragePermission: Boolean
        get() = sharedPreferences.getBoolean("STORAGE_PERMISSION", false)
        set(value) = sharedPreferences.edit { putBoolean("STORAGE_PERMISSION", value) }

    var notifPermissionDeniedCount: Int
        get() = sharedPreferences.getInt("NOTIF_PERMISSION_DENIED_COUNT_PREF", 0)
        set(value) = sharedPreferences.edit { putInt("NOTIF_PERMISSION_DENIED_COUNT_PREF", value) }

    var ttsSpeechRate: Int
        get() = sharedPreferences.getInt("TTS_SPEECH_RATE_PREF", 0)
        set(value) = sharedPreferences.edit { putInt("TTS_SPEECH_RATE_PREF", value) }

    var ttsPitch: Int
        get() = sharedPreferences.getInt("TTS_PITCH_PREF", 0)
        set(value) = sharedPreferences.edit { putInt("TTS_PITCH_PREF", value) }

    var ttsLanguage: String
        get() = sharedPreferences.getString("TTS_LANGUAGE", Locale.getDefault().displayName) ?: Locale.getDefault().displayName
        set(value) = sharedPreferences.edit { putString("TTS_LANGUAGE", value) }
}
