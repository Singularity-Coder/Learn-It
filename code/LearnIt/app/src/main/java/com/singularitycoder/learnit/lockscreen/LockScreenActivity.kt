package com.singularitycoder.learnit.lockscreen

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.load
import coil.request.ImageRequest
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.ActivityLockScreenBinding
import com.singularitycoder.learnit.helpers.NotificationsHelper
import com.singularitycoder.learnit.helpers.color
import com.singularitycoder.learnit.helpers.constants.IntentExtraKey
import com.singularitycoder.learnit.helpers.constants.IntentKey
import com.singularitycoder.learnit.helpers.constants.SettingRemindMeIn
import com.singularitycoder.learnit.helpers.currentTimeMillis
import com.singularitycoder.learnit.helpers.drawable
import com.singularitycoder.learnit.helpers.getAlarmUri
import com.singularitycoder.learnit.helpers.nineDayTimeMillis
import com.singularitycoder.learnit.helpers.nineteenDayTimeMillis
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.oneDayTimeMillis
import com.singularitycoder.learnit.helpers.oneHourTimeMillis
import com.singularitycoder.learnit.helpers.showListPopupMenu2
import com.singularitycoder.learnit.helpers.sixDayTimeMillis
import com.singularitycoder.learnit.helpers.sixHourTimeMillis
import com.singularitycoder.learnit.helpers.thirtyMinTimeMillis
import com.singularitycoder.learnit.helpers.threeHourTimeMillis
import com.singularitycoder.learnit.helpers.turnScreenOn
import com.singularitycoder.learnit.helpers.twelveHourTimeMillis
import com.singularitycoder.learnit.topic.model.Topic
import com.singularitycoder.learnit.topic.viewmodel.TopicViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO Add TTS as alarm sound. TTS reads the topic name outloud as an alarm
// TODO Alarm ring n cancel should happen in service since u have shake and power btn cancel
// TODO Send broadcasts to service to cancel or start revision
// TODO get all alarm dates from db, sort them by date descending, remove all dates less than current date - start service


@AndroidEntryPoint
class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding

    private val topicsViewModel by viewModels<TopicViewModel>()

    private var topic: Topic? = null

    private lateinit var alarmManager: AlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.navigationBarColor = color(R.color.black)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupUI()
        binding.setupUserActionListeners()
    }

    override fun onResume() {
        super.onResume()
        NotificationsHelper.clearNotification(this, NotificationsHelper.ALARM_NOTIFICATION_ID)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        binding.btnStartRevision.performClick()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        doOnIntentReceived(intent)
    }

    private fun setupUI() {
        turnScreenOn()
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val topicId = intent.getLongExtra(IntentExtraKey.TOPIC_ID_3, 0L)
//        loadTopicData(topicId)
        doOnIntentReceived(intent)
    }

    private fun ActivityLockScreenBinding.setupUserActionListeners() {
        btnRemindMeIn.onSafeClick {
            showListPopupMenu2(
                anchorView = it.first,
                menuList = SettingRemindMeIn.entries.map { it.value }
            ) { position: Int ->
                btnRemindMeIn.text = "Remind Me ${SettingRemindMeIn.entries[position]}"
                lifecycleScope.launch {
                    val nextSessionDate = when (SettingRemindMeIn.entries[position]) {
                        SettingRemindMeIn._30_MINUTES -> thirtyMinTimeMillis
                        SettingRemindMeIn._1_HOUR -> oneHourTimeMillis
                        SettingRemindMeIn._3_HOURS -> threeHourTimeMillis
                        SettingRemindMeIn._6_HOURS -> sixHourTimeMillis
                        SettingRemindMeIn._12_HOURS -> twelveHourTimeMillis
                        SettingRemindMeIn.TOMORROW -> oneDayTimeMillis
                        else -> 0
                    } + currentTimeMillis
                    topicsViewModel.updateTopic(
                        topic = topic?.copy(nextSessionDate = nextSessionDate)
                    )
                    withContext(Dispatchers.Main) {
                        NotificationsHelper.clearNotification(this@LockScreenActivity, NotificationsHelper.ALARM_NOTIFICATION_ID)
                        onBackPressedDispatcher.onBackPressed()
                            // stopRingtone
                        // TODO start alarm for next session
                    }
                }
            }
        }

        btnStartRevision.onSafeClick {
            lifecycleScope.launch {
                // TODO set next alarm
                val nextSessionDate = when ((topic?.finishedSessions ?: 0) + 1) {
                    2 -> sixDayTimeMillis
                    3 -> nineDayTimeMillis
                    4 -> nineteenDayTimeMillis
                    else -> 0
                } + (topic?.nextSessionDate ?: 0L)
                topicsViewModel.updateTopic(
                    topic = topic?.copy(
                        nextSessionDate = nextSessionDate,
                        finishedSessions = (topic?.finishedSessions ?: 0) + 1
                    )
                )
                withContext(Dispatchers.Main) {
                    // stopRingtone
                    val pendingIntent = PendingIntent.getBroadcast(
                        /* context = */ this@LockScreenActivity,
                        /* requestCode = */ 0,
                        /* intent = */ Intent(IntentKey.REVISION_ALARM),
                        /* flags = */ PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (pendingIntent != null && (topic?.finishedSessions ?: 0) >= 5) {
                        Log.d("myTag", "Alarm is already active")
                        alarmManager.cancel(pendingIntent)
                        // TODO cancel alarm
                    } else {
                        // TODO set next alarm with nextSessionDate
                    }
                    NotificationsHelper.clearNotification(this@LockScreenActivity, NotificationsHelper.ALARM_NOTIFICATION_ID)
                    // If phone locked then navigate to app, else just dismiss activity
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    private fun loadTopicData(topicId: Long) {
        lifecycleScope.launch {
            topic = topicsViewModel.getTopicById(topicId)
            val drawableRes = when (topic?.finishedSessions) {
                1 -> R.drawable.gif1
                2 -> R.drawable.gif2
                3 -> R.drawable.gif3
                4 -> R.drawable.gif4
                else -> R.drawable.gif4
            }

            val imageLoader = ImageLoader.Builder(this@LockScreenActivity)
                .components {
                    add(GifDecoder.Factory())
                }
                .build()
            val imageRequest = ImageRequest.Builder(this@LockScreenActivity)
                .data(this@LockScreenActivity.drawable(drawableRes))
                .build()
            val drawable = imageLoader.execute(imageRequest).drawable

            withContext(Dispatchers.Main) {
                binding.ivGif.load(drawable, imageLoader) {
                    placeholder(R.color.black)
                    error(R.color.md_red_700)
                }
                val session = when ((topic?.finishedSessions ?: 0) + 1) {
                    2 -> 1
                    3 -> 7
                    4 -> 16
                    else -> 35
                }
                binding.tvTopicTitle.text = "Day $session: Time to recollect ${topic?.title}"
            }
        }
    }

    private fun doOnIntentReceived(intent: Intent) {
        val isAlarmAction = intent.action == IntentKey.ALARM_SETTINGS_BROADCAST
        if (isAlarmAction) {
            val topicId = intent.getLongExtra(IntentExtraKey.TOPIC_ID_2, 0L)
            loadTopicData(topicId)
        }
    }
}


