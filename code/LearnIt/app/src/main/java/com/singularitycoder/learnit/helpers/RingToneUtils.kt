package com.singularitycoder.learnit.helpers

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Objects

class RingToneUtils(val activity: AppCompatActivity) {
    private lateinit var mediaPlayer: MediaPlayer

//    companion object {
//        private val DEFAULT_RADIO_BTN_ID = View.generateViewId()
//        private val SILENT_RADIO_BTN_ID = View.generateViewId()
//    }
//
//    private var fileActLauncher: ActivityResultLauncher<Intent> = activity.registerForActivityResult<Intent, ActivityResult>(StartActivityForResult()) { result: ActivityResult ->
//        result.data ?: return@registerForActivityResult
//        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
//
//        val toneUri = checkNotNull(result.data?.data)
//        activity.contentResolver.query(
//            /* uri = */ toneUri,
//            /* projection = */ null,
//            /* selection = */ null,
//            /* selectionArgs = */ null,
//            /* sortOrder = */ null
//        ).use { cursor ->
//            if (cursor != null) {
//                if (viewModel.getToneUriList().contains(toneUri)) {
//                    val index: Int = viewModel.getToneUriList().indexOf(toneUri)
//                    (findViewById(
//                        viewModel.getToneIdList().get(index)
//                    ) as RadioButton).isChecked = true
//                } else {
//                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                    cursor.moveToFirst()
//
//                    var fileName = cursor.getString(nameIndex)
//
//                    val indexOfDot = fileName.lastIndexOf(".")
//                    if (indexOfDot != -1) {
//                        fileName = fileName.substring(0, indexOfDot)
//                    }
//                    val toneId = View.generateViewId()
//
//                    viewModel.getToneNameList().add(fileName)
//                    viewModel.getToneUriList().add(toneUri)
//                    viewModel.getToneIdList().add(toneId)
//
//                    createOneRadioButton(toneId, fileName)
//                }
//                viewModel.setPickedUri(toneUri)
//                playChosenTone()
//            }
//        }
//    }
//
//    fun init() {
//        mediaPlayer = MediaPlayer()
//
//        try {
//            if (mediaPlayer.isPlaying) {
//                mediaPlayer.pause()
//            }
//        } catch (ignored: Exception) {
//        }
//
//        try {
//            mediaPlayer.start()
//        } catch (ignored: Exception) {
//        }
//    }
//
//    /**
//     * Fires an implicit Intent to open a file browser and let the user choose an alarm tone.
//     */
//    private fun openFileBrowser() {
//        val mimeTypes = arrayOf("audio/mpeg", "audio/ogg", "audio/aac", "audio/x-matroska")
//
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//            .setType("*/*")
//            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
//
//        fileActLauncher.launch(intent)
//    }
//
//    fun playTone() {
//        viewModel.getPlayTone()
//        viewModel.setPlayTone(isChecked)
//        if (!isChecked) {
//            try {
//                mediaPlayer.stop()
//            } catch (ignored: IllegalStateException) {
//            }
//        }
//    }
//
//    fun onResume() {
//        if (isPermissionAvailable) {
//            viewModel.setWerePermsRequested(false)
//            if (!viewModel.getIsInitialised()) {
//                initialise()
//            }
//        } else {
//            if (viewModel.werePermsRequested()) {
//                onPermissionDenied()
//            } else {
//                requestPermission()
//            }
//        }
//    }
//
//
//    fun onPause() {
//        try {
//            if (mediaPlayer.isPlaying) {
//                mediaPlayer.stop()
//            }
//        } catch (ignored: Exception) {
//        }
//    }
//
//    /**
//     * Populate [.radioGroup] by creating and adding appropriate
//     * [RadioButton].
//     */
//    private fun populateRadioGroup() {
//        if (viewModel.getExistingUri() != null) {
//            ////////////////////////////////////////////////////////////////////
//            // As existingUri is not null, we are required to pre-select
//            // a specific RadioButton.
//            ///////////////////////////////////////////////////////////////////
//
//            if (viewModel.getShowDefault() &&
//                viewModel.getExistingUri().equals(viewModel.getDefaultUri())
//            ) {
//                ///////////////////////////////////////////////////////////////////////////
//                // The existingUri is same as defaultUri, and showDefault is true.
//                // So, we check the "Default" RadioButton.
//                //////////////////////////////////////////////////////////////////////////
//
//                setPickedUri(viewModel.getDefaultUri())
//            } else {
//                // Find index of existingUri in toneUriList
//
//                val index: Int = viewModel.getToneUriList()
//                    .indexOf(viewModel.getExistingUri())
//
//                if (index != -1) {
//                    // toneUriList has existingUri. Check the corresponding RadioButton.
//
//                    viewModel.getToneIdList().get(index)
//                    setPickedUri(viewModel.getExistingUri())
//                } else {
//                    ///////////////////////////////////////////////////////////////////////
//                    // toneUriList does NOT have existingUri. It is a custom Uri
//                    // provided to us. We have to first check whether the file exists
//                    // or not. If it exists, we shall add that file to our RadioGroup.
//                    // If the file does not exist, we do not select any Radiogroup.
//                    ///////////////////////////////////////////////////////////////////////
//                    activity.contentResolver.query(
//                        viewModel.getExistingUri(), null, null, null, null
//                    ).use { cursor ->
//                        if (cursor != null && cursor.count > 0 &&
//                            cursor.moveToFirst()
//                        ) {
//                            // existingUri is a valid Uri.
//
//                            val fileNameWithExt: String
//                            val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                            fileNameWithExt = if (columnIndex != -1) {
//                                cursor.getString(columnIndex)
//                            } else {
//                                cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
//                            }
//
//                            val toneId = View.generateViewId()
//
//                            viewModel.getToneNameList().add(fileNameWithExt)
//                            viewModel.getToneUriList().add(viewModel.getExistingUri())
//                            viewModel.getToneIdList().add(toneId)
//
//                            createOneRadioButton(toneId, fileNameWithExt)
//
//                            setPickedUri(viewModel.getExistingUri())
//                        }
//                    }
//                }
//            }
//        } else {
//            if (viewModel.getWasExistingUriGiven()) {
//                //////////////////////////////////////////////////////////////////////////
//                // existingUri was specifically passed as a null value. If showSilent
//                // is true, we pre-select the "Silent" RadioButton. Otherwise
//                // we do not select any specific RadioButton.
//                /////////////////////////////////////////////////////////////////////////
//                if (viewModel.getShowSilent()) {
//                    (findViewById<View>(SILENT_RADIO_BTN_ID) as RadioButton).isChecked = true
//                }
//            }
//            setPickedUri(null)
//        }
//    }
//
//    private fun setPickedUri(newUri: Uri?) {
//        if (savedInstanceState == null) {
//            viewModel.setPickedUri(newUri)
//        }
//    }
//
//    /**
//     * Creates one [RadioButton] and adds it to [.radioGroup].
//     *
//     * @param id The id to be assigned to the [RadioButton].
//     * @param text The text to be set in the [RadioButton]. Cannot be
//     * `null`.
//     */
//    private fun createOneRadioButton(id: Int, text: String) {
//        val params = RadioGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//        params.setMargins(5, 24, 5, 24)
//
//        val radioButton = RadioButton(this)
//        radioButton.id = id
//        radioButton.setTextColor(resources.getColor(R.color.defaultLabelColor))
//        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
//        radioButton.layoutParams = params
//        radioButton.text = text
//        radioButton.setOnClickListener(this)
//        radioGroup!!.addView(radioButton)
//    }
//
//    fun onClick(view: View) {
//        if (view.id == DEFAULT_RADIO_BTN_ID) {
//            viewModel.setPickedUri(viewModel.getDefaultUri())
//            playChosenTone()
//        } else if (view.id == SILENT_RADIO_BTN_ID) {
//            viewModel.setPickedUri(null)
//        } else if (view.id == R.id.chooseCustomToneConstarintLayout) {
//            openFileBrowser()
//        } else {
//            viewModel.setPickedUri(
//                viewModel.getToneUriList()
//                    .get(viewModel.getToneIdList().indexOf(view.id))
//            )
//            playChosenTone()
//        }
//    }
//
//    fun onBackPressed() {
//        if (viewModel.getPickedUri() == null) {
//            if (viewModel.getShowSilent()) {
//                viewModel.getPickedUri()
//            }
//        }
//    }
//
//    private val isPermissionAvailable: Boolean
//        /**
//         * Checks whether [Manifest.permission.READ_MEDIA_AUDIO] (for >= Tiramisu) or
//         * [Manifest.permission.READ_EXTERNAL_STORAGE] permission is available or not.
//         *
//         * @return `true` if the permission is available, otherwise `false`.
//         */
//        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_MEDIA_AUDIO
//            ) == PackageManager.PERMISSION_GRANTED
//        } else {
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED
//        }
//
//    private fun requestPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val permAndroidString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                Manifest.permission.READ_MEDIA_AUDIO
//            } else {
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            }
//
//            val perm = ArrayList<String>()
//            perm.add(permAndroidString)
//
//            val permsLevelBundle = Bundle()
//            permsLevelBundle.putInt(
//                permAndroidString,
//                ConstantsAndStatics.PERMISSION_LEVEL_ESSENTIAL
//            )
//
//            val intent = Intent(this, Activity_ListReqPerm::class.java)
//            intent.putStringArrayListExtra(
//                ConstantsAndStatics.EXTRA_PERMS_REQUESTED,
//                perm
//            )
//            intent.putExtra(
//                ConstantsAndStatics.EXTRA_PERMS_REQUESTED_LEVEL,
//                permsLevelBundle
//            )
//
//            viewModel.setWerePermsRequested(true)
//            startActivity(intent)
//        }
//    }
//
//    private fun initialise() {
//        if (!viewModel.getIsInitialised()) {
//            val ringtoneManager = RingtoneManager(this)
//
//            val intent = intent
//            val type = if (intent.hasExtra(RingtoneManager.EXTRA_RINGTONE_TYPE)) {
//                Objects.requireNonNull(intent.extras)
//                    .getInt(RingtoneManager.EXTRA_RINGTONE_TYPE)
//            } else {
//                RingtoneManager.TYPE_ALL
//            }
//            ringtoneManager.setType(type)
//            val allTonesCursor = ringtoneManager.cursor
//
//            val thread = Thread {
//                if (allTonesCursor.moveToFirst()) {
//                    do {
//                        val id = allTonesCursor.getInt(RingtoneManager.ID_COLUMN_INDEX)
//                        val uri = allTonesCursor.getString(RingtoneManager.URI_COLUMN_INDEX)
//
//                        viewModel.getToneUriList().add(Uri.parse("$uri/$id"))
//                        viewModel.getToneNameList()
//                            .add(allTonesCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX))
//                        viewModel.getToneIdList().add(View.generateViewId())
//                    } while (allTonesCursor.moveToNext())
//                }
//            }
//            thread.start()
//
//            if (intent.hasExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT)) {
//                viewModel.setShowDefault(
//                    Objects.requireNonNull(intent.extras)
//                        .getBoolean(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT)
//                )
//            } else {
//                viewModel.setShowDefault(true)
//            }
//
//            if (intent.hasExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT)) {
//                viewModel.setShowSilent(
//                    Objects.requireNonNull(intent.extras)
//                        .getBoolean(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT)
//                )
//            } else {
//                viewModel.setShowSilent(false)
//            }
//
//            if (viewModel.getShowDefault()) {
//                if (intent.hasExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI)) {
//                    viewModel.setDefaultUri(
//                        Objects.requireNonNull(intent.extras)
//                            .getParcelable(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI)
//                    )
//                } else {
//                    if (type == RingtoneManager.TYPE_ALARM) {
//                        viewModel.setDefaultUri(Settings.System.DEFAULT_ALARM_ALERT_URI)
//                    } else if (type == RingtoneManager.TYPE_NOTIFICATION) {
//                        viewModel.setDefaultUri(Settings.System.DEFAULT_NOTIFICATION_URI)
//                    } else if (type == RingtoneManager.TYPE_RINGTONE) {
//                        viewModel.setDefaultUri(Settings.System.DEFAULT_RINGTONE_URI)
//                    } else {
//                        viewModel.setDefaultUri(
//                            RingtoneManager.getActualDefaultRingtoneUri(this, type)
//                        )
//                    }
//                }
//            } else {
//                viewModel.setDefaultUri(null)
//            }
//
//            if (intent.hasExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI)) {
//                viewModel.setExistingUri(
//                    Objects.requireNonNull(intent.extras)
//                        .getParcelable(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI)
//                )
//                viewModel.setWasExistingUriGiven(true)
//            } else {
//                viewModel.setExistingUri(null)
//                viewModel.setWasExistingUriGiven(false)
//            }
//
//            if (intent.hasExtra(RingtoneManager.EXTRA_RINGTONE_TITLE)) {
//                val title = Objects.requireNonNull(intent.extras)
//                    .getString(RingtoneManager.EXTRA_RINGTONE_TITLE)
//                viewModel.setTitle(
//                    if (title as CharSequence? != null
//                    ) title
//                    else resources.getString(R.string.ringtonePicker_defaultTitle)
//                )
//            } else {
//                viewModel.setTitle(
//                    resources.getString(
//                        R.string.ringtonePicker_defaultTitle
//                    ) as CharSequence
//                )
//            }
//
//            if (intent.hasExtra(ConstantsAndStatics.EXTRA_PLAY_RINGTONE)) {
//                viewModel.setPlayTone(
//                    Objects.requireNonNull<Bundle?>(intent.extras)
//                        .getBoolean(ConstantsAndStatics.EXTRA_PLAY_RINGTONE)
//                )
//            } else {
//                viewModel.setPlayTone(true)
//            }
//
//            try {
//                thread.join()
//            } catch (ignored: InterruptedException) {
//            }
//
//            viewModel.setIsInitialised(true)
//        }
//
//        viewModel.getTitle()
//
//        populateRadioGroup()
//    }
//
//    private fun playChosenTone() {
//        var audioAttributes: AudioAttributes? = null
//        audioAttributes = AudioAttributes.Builder()
//            .setUsage(AudioAttributes.USAGE_MEDIA)
//            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//            .build()
//        if (viewModel.getPickedUri() != null && viewModel.getPlayTone()) {
//            try {
//                mediaPlayer.reset()
//                mediaPlayer.setDataSource(activity, viewModel.getPickedUri())
//                mediaPlayer.isLooping = false
//                mediaPlayer.setAudioAttributes(audioAttributes)
//                mediaPlayer.prepareAsync()
//                mediaPlayer.setOnPreparedListener { mp: MediaPlayer? -> audioFocusController.requestFocus() }
//            } catch (ignored: Exception) {
//            }
//        }
//    }
//
//    fun onDestroy() {
//        try {
//            mediaPlayer.release()
//        } catch (ignored: Exception) {
//        }
//    }
}
