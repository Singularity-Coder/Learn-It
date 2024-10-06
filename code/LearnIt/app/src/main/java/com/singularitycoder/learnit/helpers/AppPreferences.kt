package com.singularitycoder.learnit.helpers

import android.content.Context
import android.media.AudioManager
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.singularitycoder.learnit.helpers.constants.DEFAULT_SHAKE_SENSITIVITY

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
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val defaultVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) - 2

    var hasCompletedTutorial: Boolean
        get() = sharedPreferences.getBoolean("COMPLETED_TUTORIAL", false)
        set(value) = sharedPreferences.edit { putBoolean("COMPLETED_TUTORIAL", value) }

    var hasGrantedAllPermissions: Boolean
        get() = sharedPreferences.getBoolean("GRANTED_ALL_PERMISSIONS", false)
        set(value) = sharedPreferences.edit { putBoolean("GRANTED_ALL_PERMISSIONS", value) }

    var notifPermissionDeniedCount: Int
        get() = sharedPreferences.getInt("NOTIF_PERMISSION_DENIED_COUNT", 0)
        set(value) = sharedPreferences.edit { putInt("NOTIF_PERMISSION_DENIED_COUNT", value) }

    var readAudioFilesPermissionDeniedCount: Int
        get() = sharedPreferences.getInt("READ_AUDIO_FILES_PERMISSION_DENIED_COUNT", 0)
        set(value) = sharedPreferences.edit { putInt("READ_AUDIO_FILES_PERMISSION_DENIED_COUNT", value) }

    /** SETTINGS ------------------------------------------------------ */

    var settingRemindMeOnShakePos: Int
        get() = sharedPreferences.getInt("SETTING_REMIND_ME_ON_SHAKE_POS", 0)
        set(value) = sharedPreferences.edit { putInt("SETTING_REMIND_ME_ON_SHAKE_POS", value) }

    var settingRemindMeOnPowerBtnPressPos: Int
        get() = sharedPreferences.getInt("SETTING_REMIND_ME_ON_POWER_BTN_PRESS_POS", 0)
        set(value) = sharedPreferences.edit { putInt("SETTING_REMIND_ME_ON_POWER_BTN_PRESS_POS", value) }

    var settingShakeSensitivity: Float
        get() = sharedPreferences.getFloat("SETTING_SHAKE_SENSITIVITY", DEFAULT_SHAKE_SENSITIVITY)
        set(value) = sharedPreferences.edit { putFloat("SETTING_SHAKE_SENSITIVITY", value) }

    var settingDefaultAlarmTone: Boolean
        get() = sharedPreferences.getBoolean("SETTING_DEFAULT_ALARM_TONE", false)
        set(value) = sharedPreferences.edit { putBoolean("SETTING_DEFAULT_ALARM_TONE", value) }

    var settingDefaultAlarmVolume: Int
        get() = sharedPreferences.getInt("SETTING_DEFAULT_ALARM_VOLUME", defaultVolume)
        set(value) = sharedPreferences.edit { putInt("SETTING_DEFAULT_ALARM_VOLUME", value) }

    var defaultToneUri: String
        get() = sharedPreferences.getString("DEFAULT_TONE_URI", "") ?: ""
        set(value) = sharedPreferences.edit { putString("DEFAULT_TONE_URI", value) }

//    var ttsSpeechRate: Int
//        get() = sharedPreferences.getInt("TTS_SPEECH_RATE_PREF", 0)
//        set(value) = sharedPreferences.edit { putInt("TTS_SPEECH_RATE_PREF", value) }
//
//    var ttsPitch: Int
//        get() = sharedPreferences.getInt("TTS_PITCH_PREF", 0)
//        set(value) = sharedPreferences.edit { putInt("TTS_PITCH_PREF", value) }
//
//    var ttsLanguage: String
//        get() = sharedPreferences.getString("TTS_LANGUAGE", Locale.getDefault().displayName) ?: Locale.getDefault().displayName
//        set(value) = sharedPreferences.edit { putString("TTS_LANGUAGE", value) }
}
