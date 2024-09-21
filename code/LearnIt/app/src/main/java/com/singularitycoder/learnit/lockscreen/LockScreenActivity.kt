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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.load
import coil.request.ImageRequest
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.ActivityLockScreenBinding
import com.singularitycoder.learnit.helpers.color
import com.singularitycoder.learnit.helpers.constants.IntentExtraKey
import com.singularitycoder.learnit.helpers.constants.IntentKey
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


@AndroidEntryPoint
class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding

    private val topicsViewModel by viewModels<TopicViewModel>()

    private var topic: Topic? = null

    private lateinit var alarmManager: AlarmManager

    private lateinit var ringtone: Ringtone

    private lateinit var vibrator: Vibrator

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
        observeForData()
    }

    override fun onStop() {
        super.onStop()
        stopRingtone()
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
        /** Since its repeating alarm u should stop previous ringtone to avoid multiple tracks playing simultaneously */
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        ringtone = RingtoneManager.getRingtone(this@LockScreenActivity, getAlarmUri())
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val topicId = intent.getLongExtra(IntentExtraKey.TOPIC_ID_3, 0L)
//        loadTopicData(topicId)
        doOnIntentReceived(intent)
        startRingtone()
    }

    private fun ActivityLockScreenBinding.setupUserActionListeners() {
        btnRemindMeIn.onSafeClick {
            val items = listOf(
                "30 Minutes",
                "1 Hour",
                "3 Hours",
                "6 Hours",
                "12 Hours",
                "Tomorrow"
            )
            showListPopupMenu2(
                anchorView = it.first,
                menuList = items
            ) { position: Int ->
                btnRemindMeIn.text = "Remind Me ${items[position]}"
                lifecycleScope.launch {
                    val nextSessionDate = when (items[position]) {
                        "30 Minutes" -> thirtyMinTimeMillis
                        "1 Hour" -> oneHourTimeMillis
                        "3 Hours" -> threeHourTimeMillis
                        "6 Hours" -> sixHourTimeMillis
                        "12 Hours" -> twelveHourTimeMillis
                        "Tomorrow" -> oneDayTimeMillis
                        else -> 0
                    } + currentTimeMillis
                    topicsViewModel.updateTopic(
                        topic = topic?.copy(nextSessionDate = nextSessionDate)
                    )
                    withContext(Dispatchers.Main) {
                        onBackPressedDispatcher.onBackPressed()
                        stopRingtone()
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
                    stopRingtone()
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
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    private fun observeForData() {
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

    private fun startRingtone() {
        vibrator.vibrate(4000)
        ringtone.play()
    }

    private fun stopRingtone() {
        vibrator.cancel()
        ringtone.stop()
    }

    private fun doOnIntentReceived(intent: Intent) {
        val isAlarmAction = intent.action == IntentKey.ALARM_SETTINGS_BROADCAST
        if (isAlarmAction) {
            val topicId = intent.getLongExtra(IntentExtraKey.TOPIC_ID_2, 0L)
            loadTopicData(topicId)
        }
    }
}


