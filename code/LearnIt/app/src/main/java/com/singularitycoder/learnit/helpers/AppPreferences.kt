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

    var hasNotificationPermission: Boolean
        get() = sharedPreferences.getBoolean("NOTIFICATION_PERMISSION_PREF", false)
        set(value) = sharedPreferences.edit { putBoolean("NOTIFICATION_PERMISSION_PREF", value) }

    var hasAlarmPermission: Boolean
        get() = sharedPreferences.getBoolean("ALARM_PERMISSION_PREF", false)
        set(value) = sharedPreferences.edit { putBoolean("ALARM_PERMISSION_PREF", value) }

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
