package com.singularitycoder.learnit

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.singularitycoder.learnit.helpers.AppPreferences
import com.singularitycoder.learnit.helpers.NotificationsHelper
import com.singularitycoder.learnit.helpers.constants.AlarmType
import com.singularitycoder.learnit.helpers.constants.IntentKey
import com.singularitycoder.learnit.lockscreen.LockScreenActivity
import com.singularitycoder.learnit.topic.model.Topic
import `in`.basulabs.audiofocuscontroller.AudioFocusController
import java.io.IOException
import java.util.Date
import kotlin.math.abs
import kotlin.math.sqrt

class RingAlarmService : Service() {

    companion object {
        /**
         * Indicates whether this service is running or not.
         */
        var isThisServiceRunning: Boolean = false

        /**
         * Intent extra: Number of times this alarm has been snoozed.
         */
        val EXTRA_NO_OF_TIMES_SNOOZED = "${BuildConfig.APPLICATION_ID}NO_OF_TIMES_SNOOZED"
        private val MINIMUM_MILLIS_BETWEEN_SHAKES: Int = 600

        /**
         * Get a unique notification ID.
         *
         *
         * [Courtesy](https://stackoverflow.com/questions/12978184/android-get-unique-id-of-notification#comment51322954_28251192)
         *
         * @return A unique notification ID.
         */
        fun getUniqueNotifID(): Int {
            return ((Date().time / 1000L) % Int.MAX_VALUE).toInt()
        }

    }

    private var topicId: Bundle? = null
    private var topic: Topic? = null

    private var mediaPlayer: MediaPlayer? = null

    private var ringTimer: CountDownTimer? = null

    private lateinit var snsMgr: SensorManager
    private lateinit var vibrator: Vibrator
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager

    private var lastShakeTime: Long = 0


    private var initialAlarmStreamVolume = 0

    private var numberOfTimesTheAlarmHasBeenSnoozed = 0

    private var alarmToneUri: Uri? = null

    /**
     * The unique ID of the currently ringing alarm.
     */
    var alarmID: Int = -1

    /**
     * Indicates whether this service is running or not.
     */
    var isThisServiceRunning: Boolean = false

    private var sharedPreferences: SharedPreferences? = null

    private var isShakeActive = false

    private var preMatureDeath = false

    private var repeatDays: ArrayList<Int>? = null

    private lateinit var audioFocusController: AudioFocusController

    /**
     * Indicates whether alarm ringing has already started, and prevents
     * [ringAlarm] to be called more than once by
     * [AudioFocusController.OnAudioFocusChangeListener.resume].
     */
    private var alarmRingingStarted = false

    private var notifID = 0

    private var powerBtnAction = 0

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val gX = x / SensorManager.GRAVITY_EARTH
                val gY = y / SensorManager.GRAVITY_EARTH
                val gZ = z / SensorManager.GRAVITY_EARTH

                val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

                // gForce will be close to 1 when there is no movement.
                if (gForce >= AppPreferences.getInstance().settingShakeSensitivity) {
                    val currTime = System.currentTimeMillis()
                    if (abs((currTime - lastShakeTime).toDouble()) > MINIMUM_MILLIS_BETWEEN_SHAKES) {
                        lastShakeTime = currTime
                        shakeVibration()
                        // TODO set next alarm using AppPreferences.getInstance().settingRemindMeOnShakePos
                        dismissAlarm()
                    }
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit
    }

    /** These events should come from [LockScreenActivity]*/
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.action) {
//                ConstantsAndStatics.ACTION_SNOOZE_ALARM -> {
//                    // TODO set next alarm using AppPreferences.getInstance().settingRemindMeOnShakePos
//                }
//                ConstantsAndStatics.ACTION_CANCEL_ALARM -> {
//                    dismissAlarm()
//                }
//                Intent.ACTION_SCREEN_OFF -> {
//                    if (powerBtnAction == ConstantsAndStatics.DISMISS) {
//                        dismissAlarm()
//                    } else if (powerBtnAction == ConstantsAndStatics.SNOOZE) {
//                        snoozeAlarm()
//                    }
//                }
//            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notifID = getUniqueNotifID()

        // Do NOT move this!!!!
        topicId = intent.extras?.getBundle("BUNDLE_KEY_ALARM_DETAILS") ?: bundleOf()
        topic = Topic() // get from db

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                notifID, buildRingNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                notifID, buildRingNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            )
        }
        isThisServiceRunning = true
        preMatureDeath = true
        alarmRingingStarted = false

//        ConstantsAndStatics.cancelScheduledPeriodicWork(this)

        val audioFocusChangeListener = object : AudioFocusController.OnAudioFocusChangeListener {
            override fun decreaseVolume() = Unit
            override fun increaseVolume() = Unit
            override fun pause() = Unit
            override fun resume() {
                if (!alarmRingingStarted) {
                    alarmRingingStarted = true
                    ringAlarm()
                }
            }
        }
        audioFocusController = AudioFocusController.Builder(this)
            .setAcceptsDelayedFocus(true)
            .setAudioFocusChangeListener(audioFocusChangeListener)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setPauseWhenAudioIsNoisy(false)
            .setStream(AudioManager.STREAM_ALARM)
            .setDurationHint(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            .build()

        isShakeActive = AppPreferences.getInstance().settingRemindMeOnShakePos != 0

        alarmID = 0

        // TODO Kill Service_SnoozeAlarm if it is running for a different alarm.

        val chosenToneUri = Uri.parse(topic?.alarmTone)
        try {
            contentResolver.openInputStream(
                chosenToneUri ?: Uri.EMPTY
            ).use { ignored ->
                // Alarm tone file exists.
                alarmToneUri = chosenToneUri
            }
        } catch (ex: Exception) {
            // Tone file can either not be accessed, or not available in the file system.
            // Fall back to default tone.
            alarmToneUri = Settings.System.DEFAULT_ALARM_ALERT_URI
        }

        numberOfTimesTheAlarmHasBeenSnoozed = intent.extras?.getInt(EXTRA_NO_OF_TIMES_SNOOZED, 0) ?: 0

        ringTimer = object : CountDownTimer(60_000, 1000) {
            override fun onTick(millisUntilFinished: Long) = Unit
            override fun onFinish() {
                // TODO set next alarm using AppPreferences.getInstance().settingRemindMeOnShakePos
            }
        }

        snsMgr = getSystemService(SENSOR_SERVICE) as SensorManager
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        initialAlarmStreamVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        powerBtnAction = AppPreferences.getInstance().settingRemindMeOnPowerBtnPressPos

        val intentFilter = IntentFilter().apply {
            addAction("ACTION_SNOOZE_ALARM")
            addAction("ACTION_CANCEL_ALARM")
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        ContextCompat.registerReceiver(
            /* context = */ this,
            /* receiver = */ broadcastReceiver,
            /* filter = */ intentFilter,
            /* flags = */ ContextCompat.RECEIVER_NOT_EXPORTED
        )

        audioFocusController.requestFocus()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        if (preMatureDeath) {
            dismissAlarm()
        }

        try {
            ringTimer!!.cancel()
            vibrator.cancel()
            if (mediaPlayer != null) {
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
            }
        } catch (ignored: Exception) {
        }
        if (isShakeActive) {
            snsMgr.unregisterListener(sensorEventListener)
        }
        if (notificationManager.isNotificationPolicyAccessGranted) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                initialAlarmStreamVolume, 0
            )
        }
        unregisterReceiver(broadcastReceiver)
        isThisServiceRunning = false
        alarmID = -1
    }

    private fun initShakeSensor() {
        if (isShakeActive) {
            val accelerometer = snsMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            snsMgr.registerListener(
                sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI,
                Handler()
            )
            lastShakeTime = System.currentTimeMillis()
        }
    }


    /**
     * Creates a notification that can be shown when the alarm is ringing.
     *
     *
     * Has a full screen intent to [LockScreenActivity]. The content intent points
     * to [Activity_AlarmsList].
     *
     *
     * @return A [Notification] instance that can be displayed to the user.
     */
    private fun buildRingNotification(): Notification {
        NotificationsHelper.createNotificationChannel(this)

        val fullScreenIntent = Intent(this, LockScreenActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            .putExtras(topicId!!)

        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val fullScreenPendingIntent = PendingIntent.getActivity(
            /* context = */ this,
            /* requestCode = */ 3054,
            /* intent = */ fullScreenIntent,
            /* flags = */ flags
        )

        val alarmMessage = topic?.title

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            NotificationsHelper.NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle(resources.getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(fullScreenPendingIntent)
            .setOnlyAlertOnce(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)

        if (alarmMessage != null) {
            builder.setContentTitle(getString(R.string.app_name))
                .setContentText(alarmMessage)
                .setStyle(NotificationCompat.BigTextStyle().bigText(alarmMessage))
        } else {
            builder.setContentText(getString(R.string.notifContent_ring))
        }

        return builder.build()
    }


    /**
     * Initialises the [MediaPlayer], and starts ringing the alarm.
     */
    private fun ringAlarm() {
        notificationManager.notify(notifID, buildRingNotification())
        initShakeSensor()

        if (topic?.alarmType != AlarmType.VIBRATE.ordinal) {
            mediaPlayer = MediaPlayer()
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            if (notificationManager.isNotificationPolicyAccessGranted) {
                audioManager.setStreamVolume(
                    /* streamType = */ AudioManager.STREAM_ALARM,
                    /* index = */ topic?.alarmVolume ?: 1,
                    /* flags = */ 0
                )
            }

            try {
                mediaPlayer!!.setDataSource(this, alarmToneUri!!)
                mediaPlayer!!.setAudioAttributes(attributes)
                mediaPlayer!!.isLooping = true
                mediaPlayer!!.prepare()
            } catch (ignored: IOException) {
            }

            if (topic?.alarmType == AlarmType.SOUND_VIBRATE.ordinal) {
                alarmVibration()
            }
            mediaPlayer!!.start()
        } else {
            alarmVibration()
        }

        ringTimer!!.start()
    }


    private fun alarmVibration() {
        val vibrationPattern = longArrayOf(0, 600, 200, 600, 200, 800, 200, 1000)
        val vibrationAmplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)

        /** -1 : Play exactly once */
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    /* timings = */ vibrationPattern,
                    /* amplitudes = */ vibrationAmplitudes,
                    /* repeat = */ 0
                )
            )
        }
    }


    /**
     * Snoozes the alarm. If snooze is off, or the snooze frequency has been reached, the
     * alarm will be cancelled by calling [.dismissAlarm].
     */
//    private fun snoozeAlarm() {
//        stopRinging()
//
//        if (alarmDetails!!.getBoolean(ConstantsAndStatics.BUNDLE_KEY_IS_SNOOZE_ON)) {
//            if (numberOfTimesTheAlarmHasBeenSnoozed <
//                alarmDetails!!.getInt(ConstantsAndStatics.BUNDLE_KEY_SNOOZE_FREQUENCY)
//            ) {
//                numberOfTimesTheAlarmHasBeenSnoozed++
//
//                val intent = Intent(this, Service_SnoozeAlarm::class.java)
//                    .putExtra(ConstantsAndStatics.BUNDLE_KEY_ALARM_DETAILS, alarmDetails)
//                    .putExtra(
//                        EXTRA_NO_OF_TIMES_SNOOZED,
//                        numberOfTimesTheAlarmHasBeenSnoozed
//                    )
//                ContextCompat.startForegroundService(this, intent)
//
//                preMatureDeath = false
//                stopForeground(true)
//                stopSelf()
//            } else {
//                dismissAlarm()
//            }
//        } else {
//            dismissAlarm()
//        }
//    }


    /**
     * Dismisses the current alarm, and sets the next alarm if repeat is enabled.
     */
    private fun dismissAlarm() {
        stopRinging()
        cancelPendingIntent()

        // If repeat is on, set another alarm. Otherwise toggle alarm state in database.
//            val alarmTime = LocalTime.of(
//                alarmDetails!!.getInt(ConstantsAndStatics.BUNDLE_KEY_ALARM_HOUR),
//                alarmDetails!!.getInt(ConstantsAndStatics.BUNDLE_KEY_ALARM_MINUTE)
//            )
//
//            Collections.sort(repeatDays)
//
//            var alarmDateTime = LocalDateTime.of(LocalDate.now(), alarmTime)
//            val dayOfWeek = alarmDateTime.dayOfWeek.value
//
//            for (i in repeatDays!!.indices) {
//                if (repeatDays!![i] == dayOfWeek) {
//                    if (alarmTime.isAfter(LocalTime.now())) {
//                        // Alarm possible today, nothing more to do, break out of loop.
//                        break
//                    }
//                } else if (repeatDays!![i] > dayOfWeek) {
//                    // There is a day available in the same week for the alarm to ring;
//                    // select that day and
//                    // break from loop.
//                    alarmDateTime = alarmDateTime.with(
//                        TemporalAdjusters.next(DayOfWeek.of(repeatDays!![i]))
//                    )
//                    break
//                }
//                if (i == repeatDays!!.size - 1) {
//                    // No day possible in this week. Select the first available date
//                    // from next week.
//                    alarmDateTime = alarmDateTime.with(
//                        TemporalAdjusters.next(DayOfWeek.of(repeatDays!![0]))
//                    )
//                }
//            }
//        setAlarm(alarmDateTime)

//        ConstantsAndStatics.schedulePeriodicWork(this)
        preMatureDeath = false
        stopForeground(true)
        stopSelf()
    }


    /**
     * Stops the ringing alarm. Also sends a broadcast to [Activity_RingAlarm] to
     * finish itsef.
     */
    private fun stopRinging() {
        try {
            ringTimer!!.cancel()

            if ((topic?.alarmType == AlarmType.VIBRATE.ordinal) || (topic?.alarmType == AlarmType.SOUND_VIBRATE.ordinal)) {
                vibrator.cancel()
            }
            if (mediaPlayer != null) {
                mediaPlayer!!.stop()
            }
        } catch (ignored: Exception) {
        } finally {
            if (isShakeActive) {
                snsMgr.unregisterListener(sensorEventListener)
            }
            val intent = Intent("ACTION_DESTROY_RING_ALARM_ACTIVITY")
            intent.setPackage(packageName)
            sendBroadcast(intent)
        }
        audioFocusController.abandonFocus()
    }


    //----------------------------------------------------------------------------------
    /**
     * Sets the next alarn in case of a repeat alarm.
     *
     * @param alarmDateTime The date and time when the alarm is to be set.
     */
//    private fun setAlarm(alarmDateTime: LocalDateTime) {
//        val alarmManager = getSystemService(
//            ALARM_SERVICE
//        ) as AlarmManager
//
//        val intent = Intent(applicationContext, AlarmBroadcastReceiver::class.java)
//            .setAction(ConstantsAndStatics.ACTION_DELIVER_ALARM)
//            .setFlags(Intent.FLAG_RECEIVER_FOREGROUND)
//            .putExtra(ConstantsAndStatics.BUNDLE_KEY_ALARM_DETAILS, alarmDetails)
//
//        val flags = PendingIntent.FLAG_IMMUTABLE
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            applicationContext,
//            alarmID, intent, flags
//        )
//
//        val zonedDateTime = ZonedDateTime.of(
//            alarmDateTime.withSecond(0),
//            ZoneId.systemDefault()
//        )
//
//        alarmManager.setAlarmClock(
//            AlarmClockInfo(
//                zonedDateTime.toEpochSecond() * 1000,
//                pendingIntent
//            ), pendingIntent
//        )
//    }


    /**
     * While testing, we found that sometimes, the alarm was being reset at a later date
     * unintentionally. This function cancels such an unintentional alarm.
     */
    private fun cancelPendingIntent() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val intent = Intent(applicationContext, ThisBroadcastReceiver::class.java)
            .setAction(IntentKey.DELIVER_ALARM)
            .setFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            .putExtra(IntentKey.ALARM_DETAILS, topicId)

        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE

        val pendingIntent = PendingIntent.getBroadcast(
            /* context = */ applicationContext,
            /* requestCode = */ alarmID,
            /* intent = */ intent,
            /* flags = */ flags
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    /**
     * Creates a vibration for a small period of time, indicating that the app has
     * registered a shake event.
     */
    private fun shakeVibration() {
        if (vibrator.hasVibrator()) {
            vibrator.cancel()
            SystemClock.sleep(100)
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    /* milliseconds = */ 200,
                    /* amplitude = */ VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
            SystemClock.sleep(200)
        }
    }
}