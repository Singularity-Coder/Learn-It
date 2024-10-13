package com.singularitycoder.learnit.subject.view

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.SeekBar
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.learnit.databinding.FragmentSettingsBottomSheetBinding
import com.singularitycoder.learnit.helpers.AppPreferences
import com.singularitycoder.learnit.helpers.constants.BottomSheetTag
import com.singularitycoder.learnit.helpers.constants.remindMeInList
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.setTransparentBackground
import com.singularitycoder.learnit.topic.view.EditTopicBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.sqrt

@AndroidEntryPoint
class SettingsBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val MIN_MILLIS_BETWEEN_SHAKES: Int = 400

        @JvmStatic
        fun newInstance() = SettingsBottomSheetFragment()
    }

    private lateinit var binding: FragmentSettingsBottomSheetBinding

    private var lastShakeTime: Long = 0

    private var isShakeTestGoingOn = false


    private lateinit var sensorManager: SensorManager
    private lateinit var acclerometer: Sensor

    private var sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                val x: Float = event.values.get(0)
                val y: Float = event.values.get(1)
                val z: Float = event.values.get(2)

                val gX = x / SensorManager.GRAVITY_EARTH
                val gY = y / SensorManager.GRAVITY_EARTH
                val gZ = z / SensorManager.GRAVITY_EARTH

                val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

                // gForce will be close to 1 when there is no movement.
                if (gForce >= AppPreferences.getInstance().settingShakeSensitivity) {
                    val currTime = System.currentTimeMillis()
                    if (abs((currTime - lastShakeTime).toDouble()) > MIN_MILLIS_BETWEEN_SHAKES) {
                        lastShakeTime = currTime
                        shakeVibration()
                    }
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    override fun onResume() {
        super.onResume()
        if (isShakeTestGoingOn) {
            sensorManager.registerListener(
                sensorListener, acclerometer,
                SensorManager.SENSOR_DELAY_UI, Handler(Looper.getMainLooper())
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener, acclerometer)
    }

    private fun FragmentSettingsBottomSheetBinding.setupUI() {
        setTransparentBackground()
        tvHeader.text = "Settings"

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acclerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        val min = 1.5
        val max = 6.5
        val stepSize = 0.2
        val steps = ((max - min) / stepSize).toInt()
        layoutShakeSensitivitySlider.apply {
            tvSliderTitle.isVisible = false
            sliderCustom.min = 1
            sliderCustom.max = steps
            sliderCustom.progress = getStepValue(AppPreferences.getInstance().settingShakeSensitivity)
        }

        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        layoutVolumeSlider.apply {
            tvSliderTitle.isVisible = false
            sliderCustom.min = 1
            sliderCustom.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            sliderCustom.progress = AppPreferences.getInstance().settingDefaultAlarmVolume
        }

        etOnShake.editText?.setText(remindMeInList.get(AppPreferences.getInstance().settingRemindMeOnShakePos))
        val onShakeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, remindMeInList)
        (etOnShake.editText as? AutoCompleteTextView)?.setAdapter(onShakeAdapter)

        etPowerBtn.editText?.setText(remindMeInList.get(AppPreferences.getInstance().settingRemindMeOnPowerBtnPressPos))
        val powerBtnAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, remindMeInList)
        (etPowerBtn.editText as? AutoCompleteTextView)?.setAdapter(powerBtnAdapter)

        etAlarmSound.editText?.setText("Default")

        switchDefaultAlarmTone.isChecked = AppPreferences.getInstance().settingDefaultAlarmTone

        setToneTextView(getCurrentToneUri())
    }

    private fun FragmentSettingsBottomSheetBinding.setupUserActionListeners() {
        switchTestShakeSensitivity.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                sensorManager.registerListener(
                    /* listener = */ sensorListener,
                    /* sensor = */ acclerometer,
                    /* samplingPeriodUs = */ SensorManager.SENSOR_DELAY_UI,
                    /* handler = */ Handler(Looper.getMainLooper())
                )
                lastShakeTime = System.currentTimeMillis()
                isShakeTestGoingOn = true
            } else {
                sensorManager.unregisterListener(sensorListener, acclerometer)
                isShakeTestGoingOn = false
            }
        }

        switchDefaultAlarmTone.setOnCheckedChangeListener { buttonView, isChecked ->
            AppPreferences.getInstance().settingDefaultAlarmTone = isChecked
        }

        layoutShakeSensitivitySlider.apply {
            ibReduce.onSafeClick {
                sliderCustom.progress -= 1
                tvShakeSensitivity.text = "Shake Sensitivity: ${sliderCustom.progress}"
            }
            ibIncrease.onSafeClick {
                sliderCustom.progress += 1
                tvShakeSensitivity.text = "Shake Sensitivity: ${sliderCustom.progress}"
            }
            sliderCustom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    tvShakeSensitivity.text = "Shake Sensitivity: ${seekBar.progress}"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    println("seekbar progress: ${seekBar.progress}")
                    tvShakeSensitivity.text = "Shake Sensitivity: ${seekBar.progress}"
                    AppPreferences.getInstance().settingShakeSensitivity = seekBar.progress.toFloat()
                }
            })
        }

        layoutVolumeSlider.apply {
            ibReduce.onSafeClick {
                sliderCustom.progress -= 1
                tvVolume.text = "Volume: ${sliderCustom.progress}"
            }
            ibIncrease.onSafeClick {
                sliderCustom.progress += 1
                tvVolume.text = "Volume: ${sliderCustom.progress}"
            }
            sliderCustom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    tvVolume.text = "Volume: ${seekBar.progress}"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    println("seekbar progress: ${seekBar.progress}")
                    tvVolume.text = "Volume: ${seekBar.progress}"
                }
            })
        }

        tvAlarmSound.onSafeClick {
            RingTonePickerBottomSheetFragment.newInstance(EditTopicBottomSheetFragment::class.java.simpleName).show(
                parentFragmentManager,
                BottomSheetTag.TAG_RINGTONE_PICKER
            )
        }
    }

    /**
     * Creates a vibration for a small period of time, indicating that the app has
     * registered a shake event.
     */
    private fun shakeVibration() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    200,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }

    /**
     * Get the current default alarm tone Uri from SharedPreferences.
     *
     * @return The current default alarm tone Uri.
     */
    private fun getCurrentToneUri(): Uri {
        val tone: String = AppPreferences.getInstance().defaultToneUri

        if (tone.isBlank().not() && doesFileExist(Uri.parse(tone))) {
            return Uri.parse(tone)
        } else {
            AppPreferences.getInstance().defaultToneUri = Settings.System.DEFAULT_ALARM_ALERT_URI.toString()
            return Settings.System.DEFAULT_ALARM_ALERT_URI
        }
    }

    /**
     * Finds whether a file exists or not.
     *
     * @param uri The Uri of the file.
     * @return `true` if the file exists, otherwise `false`.
     */
    private fun doesFileExist(uri: Uri): Boolean {
        try {
            context?.contentResolver?.query(
                uri, null, null, null,
                null
            ).use { cursor ->
                return cursor != null
            }
        } catch (exception: SecurityException) {
            return false
        }
    }

    /**
     * Calculates the steps of seekbar from sensitivity value.
     *
     * @param sensitivity The value of sensitivity, i.e. the minimum gForce required to
     * trigger the detector.
     * @return The corresponding progress value that can be set in the seekbar.
     */
    private fun getStepValue(sensitivity: Float): Int {
        return ((sensitivity - 1.5f) / .2f).toInt()
    }


    /**
     * Given a progress value from the seekbar, this function calculates the sensitivity
     * of shake detector.
     *
     * @param step The value returned by [SeekBar.getProgress]}.
     * @return The sensitivity corresponding to the step.
     */
    private fun getSensitivityValue(step: Int): Float {
        val bigDecimal = BigDecimal((1.5f + step * .2f).toDouble()).setScale(
            1,
            RoundingMode.FLOOR
        )
        return bigDecimal.toFloat()
    }

    /**
     * Displays the name of the currently selected default alarm tone.
     *
     * @param uri The Uri of the tone.
     */
    private fun setToneTextView(uri: Uri) {
        if (uri == Settings.System.DEFAULT_ALARM_ALERT_URI) {
//            toneTextView.setText(R.string.defaultAlarmToneText)
        } else {
            var fileName: String? = null

            context?.contentResolver?.query(
                uri, null, null, null,
                null
            ).use { cursor ->
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        val index: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        fileName = if (index != -1) {
                            cursor.getString(index)
                        } else {
                            cursor.getString(
                                RingtoneManager.TITLE_COLUMN_INDEX
                            )
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
//            if (fileName != null) {
//                toneTextView.setText(fileName)
//            } else {
//                toneTextView.setText(uri.lastPathSegment)
//            }
        }
    }

    fun launchRingtonePickerScreen() {
//        val intent: Intent = Intent(this, Activity_RingtonePicker::class.java)
//        intent.setAction(RingtoneManager.ACTION_RINGTONE_PICKER)
//            .putExtra(
//                RingtoneManager.EXTRA_RINGTONE_TYPE,
//                RingtoneManager.TYPE_ALARM
//            )
//            .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select alarm tone:")
//            .putExtra(
//                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
//                getCurrentToneUri()
//            )
//            .putExtra(
//                RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
//                Settings.System.DEFAULT_ALARM_ALERT_URI
//            )
//            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
//            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
//            .putExtra(ConstantsAndStatics.EXTRA_PLAY_RINGTONE, false)
//
//        ringPickActLauncher.launch(intent)

        // set result uri in shared pref
        AppPreferences.getInstance().defaultToneUri
    }
}