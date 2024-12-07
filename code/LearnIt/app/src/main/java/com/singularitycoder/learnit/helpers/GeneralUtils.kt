package com.singularitycoder.learnit.helpers

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.PowerManager
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.reflect.TypeToken
import com.singularitycoder.learnit.helpers.LearnItUtils.gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.reflect.Type

// https://stackoverflow.com/questions/3160447/how-to-show-up-the-settings-for-text-to-speech-in-my-app
fun Activity.showTtsSettings() {
    val intent = Intent().apply {
        action = "com.android.settings.TTS_SETTINGS"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
}

fun View.showSnackBar(
    message: String,
    anchorView: View? = null,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionBtnText: String = "NA",
    isAnimated: Boolean = true,
    action: () -> Unit = {},
) {
    Snackbar.make(this, message, duration).apply {
        if (isAnimated) this.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
//        this.setBackgroundTint(this.context.color(R.color.black_50))
//        this.setTextColor(this.context.color(R.color.title_color))
        if (null != anchorView) this.anchorView = anchorView
        if ("NA" != actionBtnText) setAction(actionBtnText) { action.invoke() }
        this.show()
    }
}

fun deviceWidth() = Resources.getSystem().displayMetrics.widthPixels

fun deviceHeight() = Resources.getSystem().displayMetrics.heightPixels

fun Context.showToast(
    message: String,
    duration: Int = Toast.LENGTH_LONG,
) = Toast.makeText(this, message, duration).show()

fun Number.dpToPx(): Float = this.toFloat() * Resources.getSystem().displayMetrics.density

// Credit: Philip Lackner
fun <T> AppCompatActivity.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun Context?.clipboard(): ClipboardManager? =
    this?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

/** Request focus before hiding keyboard - editText.requestFocus() */
fun EditText?.hideKeyboard(isClearFocus: Boolean = true) {
    this?.requestFocus()
    if (this?.hasFocus() == true) {
        val imm = this.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(this.windowToken, 0)
    }
    if (isClearFocus) this?.clearFocus()
}

// https://medium.com/@saishaddai/how-to-know-when-youre-using-dark-mode-programmatically-9be83fded4b0
fun Context.isDarkModeOn(): Boolean {
    val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return currentNightMode == Configuration.UI_MODE_NIGHT_YES
}

/** Request focus before showing keyboard - editText.requestFocus() */
fun EditText?.showKeyboard() {
    this?.requestFocus()
    if (this?.hasFocus() == true) {
        val imm = this.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun getAlarmUri(): Uri? {
    return RingtoneManager.getDefaultUri(
        RingtoneManager.TYPE_ALARM
    ) ?: RingtoneManager.getDefaultUri(
        RingtoneManager.TYPE_NOTIFICATION
    )
}

/**
 * ## Turns the screen on
 *
 * See https://github.com/yuriykulikov/AlarmClock/issues/360 It seems that on some devices with
 * API>=27 calling `setTurnScreenOn(true)` is not enough, so we will just add all flags regardless
 * of the API level, and call `setTurnScreenOn(true)` if API level is 27+
 *
 * ### 3.07.01 reference In `3.07.01` we added these 4 flags:
 * ```
 * final Window win = getWindow();
 * win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
 * win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
 *         | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
 *         | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
 * ```
 */
fun Activity.turnScreenOn() {
    setShowWhenLocked(true)
    setTurnScreenOn(true)
    // Deprecated flags are required on some devices, even with API>=27
    window.addFlags(
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    )
}

fun pendingIntentUpdateCurrentFlag(): Int {
//    val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    return PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
}

fun Context.canScheduleAlarms(): Boolean {
    return AndroidVersions.isTiramisu() && (getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
}

fun Context.hasNotificationPolicyAccess(): Boolean {
    return (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted
}

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    return (getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)
}

fun Activity.setStatusBarColor(@ColorRes color: Int) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.insetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
    window.statusBarColor = ContextCompat.getColor(this, color)
}

fun Context?.playSound(@RawRes sound: Int) {
    var mediaPlayer: MediaPlayer? = null
    try {
        mediaPlayer = MediaPlayer.create(this, sound)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        mediaPlayer?.setOnCompletionListener { it: MediaPlayer? ->
            try {
                it?.reset()
                it?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    try {
        mediaPlayer?.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

inline fun <reified T : Any> listToString(list: ArrayList<T>?): String? {
    if (null == list) return null
    val type: Type = object : TypeToken<ArrayList<T>?>() {}.type
    return gson.toJson(list, type)
}

inline fun <reified T : Any> stringToList(string: String?): ArrayList<T>? {
    if (null == string) return null
    val type: Type = object : TypeToken<ArrayList<T>?>() {}.type
    return gson.fromJson<ArrayList<T>?>(string, type)
}

inline fun <reified T : Any> objectToString(obj: T?): String? {
    obj ?: return null
    return gson.toJson(obj)
}

inline fun <reified T : Any> stringToObject(string: String?): T? {
    string ?: return null
    return gson.fromJson(string, T::class.java)
}

fun Context.isScreenOn(): Boolean {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isInteractive
}
