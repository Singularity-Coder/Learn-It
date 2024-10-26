package com.singularitycoder.learnit.subject.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentAlarmSoundPickerBottomSheetBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.constants.globalSlideToBottomAnimation
import com.singularitycoder.learnit.helpers.enableSoftInput
import com.singularitycoder.learnit.helpers.hideKeyboard
import com.singularitycoder.learnit.helpers.layoutAnimationController
import com.singularitycoder.learnit.helpers.onImeClick
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.setTransparentBackground
import com.singularitycoder.learnit.helpers.showKeyboard
import com.singularitycoder.learnit.helpers.showToast
import com.singularitycoder.learnit.subject.viewmodel.AlarmSoundPickerViewModel
import com.singularitycoder.learnit.subtopic.model.SubTopic
import com.singularitycoder.learnit.subtopic.view.SubTopicsAdapter
import dagger.hilt.android.AndroidEntryPoint
import `in`.basulabs.audiofocuscontroller.AudioFocusController
import java.util.Objects

@AndroidEntryPoint
class AlarmSoundPickerBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_FROM_SCREEN = "ARG_FROM_SCREEN"

        private val DEFAULT_RADIO_BTN_ID: Int = View.generateViewId()
        private val SILENT_RADIO_BTN_ID: Int = View.generateViewId()

        @JvmStatic
        fun newInstance(fromScreen: String) = AlarmSoundPickerBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FROM_SCREEN, fromScreen)
            }
        }
    }

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioAttributes: AudioAttributes
    private lateinit var audioFocusController: AudioFocusController

    private var subTopicList = listOf<SubTopic?>()

    private val viewModel by viewModels<AlarmSoundPickerViewModel>()

    private val subTopicsAdapter: SubTopicsAdapter by lazy { SubTopicsAdapter() }

    private lateinit var binding: FragmentAlarmSoundPickerBottomSheetBinding

    private var fromScreen: String? = null

//    private val fileActLauncher = registerForActivityResult<Intent, ActivityResult>(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result: ActivityResult ->
//        val resultCode = result.resultCode
//        val data = result.data
//        if (resultCode == Activity.RESULT_OK && data != null) {
//            val toneUri = checkNotNull(data.data)
//            context?.contentResolver?.query(
//                /* uri = */ toneUri,
//                /* projection = */ null,
//                /* selection = */ null,
//                /* selectionArgs = */ null,
//                /* sortOrder = */ null
//            ).use { cursor ->
//                if (cursor != null) {
//                    if (viewModel.getToneUriList().contains(toneUri)) {
//                        val index = viewModel.getToneUriList().indexOf(toneUri)
////                        (findViewById(viewModel.getToneIdList()[index]) as RadioButton).isChecked = true
//                    } else {
//                        val nameIndex: Int = cursor.getColumnIndex(
//                            OpenableColumns.DISPLAY_NAME
//                        )
//                        cursor.moveToFirst()
//
//                        var fileName: String = cursor.getString(nameIndex)
//
//                        val indexOfDot = fileName.lastIndexOf(".")
//                        if (indexOfDot != -1) {
//                            fileName = fileName.substring(0, indexOfDot)
//                        }
//                        val toneId = View.generateViewId()
//
//                        viewModel.getToneNameList().add(fileName)
//                        viewModel.getToneUriList().add(toneUri)
//                        viewModel.getToneIdList().add(toneId)
//
//                        createOneRadioButton(toneId, fileName)
//
////                        (findViewById<View>(toneId) as RadioButton).isChecked = true
//                    }
//                    viewModel.setPickedUri(toneUri)
//                    playChosenTone()
//                }
//            }
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        if (AndroidVersions.isTiramisu()) {
//            fromScreen = arguments?.getString(ARG_FROM_SCREEN, "")
//        } else {
//            fromScreen = arguments?.getString(ARG_FROM_SCREEN)
//        }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        binding = FragmentAlarmSoundPickerBottomSheetBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding.setupUI()
//        binding.setupUserActionListeners()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (isPermissionAvailable()) {
//            viewModel.setWerePermsRequested(false)
//            if (viewModel.getIsInitialised().not()) {
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
//    override fun onPause() {
//        super.onPause()
//        try {
//            if (mediaPlayer.isPlaying) {
//                mediaPlayer.stop()
//                audioFocusController.abandonFocus()
//            }
//        } catch (ignored: java.lang.Exception) {
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        try {
//            mediaPlayer.release()
//        } catch (ignored: java.lang.Exception) {
//        }
//    }
//
//    private fun FragmentAlarmSoundPickerBottomSheetBinding.setupUI() {
//        enableSoftInput()
//        setTransparentBackground()
//
//        mediaPlayer = MediaPlayer()
//        audioAttributes = AudioAttributes.Builder()
//            .setUsage(AudioAttributes.USAGE_MEDIA)
//            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//            .build()
//
//        audioFocusController = AudioFocusController.Builder(requireContext())
//            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//            .setStream(AudioManager.STREAM_MUSIC)
//            .setUsage(AudioAttributes.USAGE_MEDIA)
//            .setAcceptsDelayedFocus(true)
//            .setPauseWhenAudioIsNoisy(true)
//            .setPauseWhenDucked(true)
//            .setDurationHint(AudioManager.AUDIOFOCUS_GAIN)
//            .setAudioFocusChangeListener(
//                object : AudioFocusController.OnAudioFocusChangeListener {
//                    override fun decreaseVolume() = Unit
//                    override fun increaseVolume() = Unit
//
//                    override fun pause() {
//                        try {
//                            if (mediaPlayer.isPlaying) {
//                                mediaPlayer.pause()
//                            }
//                        } catch (_: Exception) {
//                        }
//                    }
//
//                    override fun resume() {
//                        try {
//                            mediaPlayer.start()
//                        } catch (_: Exception) {
//                        }
//                    }
//                }).build()
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    private fun FragmentAlarmSoundPickerBottomSheetBinding.setupUserActionListeners() {
//        ivSearch.onSafeClick {
//            clSearch.layoutAnimation = clSearch.context.layoutAnimationController(globalSlideToBottomAnimation)
//            etSearch.setText("")
//            clSearch.isVisible = clSearch.isVisible.not()
//            if (clSearch.isVisible) {
//                etSearch.showKeyboard()
//            } else {
//                etSearch.hideKeyboard()
//            }
//        }
//
//        ibClearSearch.onSafeClick {
//            etSearch.setText("")
//        }
//
//        etSearch.doAfterTextChanged { query: Editable? ->
//            ibClearSearch.isVisible = query.isNullOrBlank().not()
//            if (query.isNullOrBlank()) {
//                subTopicsAdapter.subTopicList = subTopicList
//                subTopicsAdapter.notifyDataSetChanged()
//                return@doAfterTextChanged
//            }
//            subTopicsAdapter.subTopicList = subTopicList.filter {
//                it?.title?.contains(other = query, ignoreCase = true) == true
//            }
//            subTopicsAdapter.notifyDataSetChanged()
//        }
//
//        etSearch.onImeClick {
//            etSearch.hideKeyboard()
//        }
//
//        // Switch to turn on alarm
//        val actionView = toggleservice.getActionView()
//        Objects.requireNonNull(actionView).isChecked = viewModel.getPlayTone()
//        actionView.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
//            viewModel.setPlayTone(isChecked)
//            if (!isChecked) {
//                try {
//                    mediaPlayer.stop()
//                } catch (ignored: IllegalStateException) {
//                }
//            }
//        }
//
//        // Add default alarm, slient, choose sound options to list
//        fun onClick(view: View) {
//            if (view.id == DEFAULT_RADIO_BTN_ID) {
//                viewModel.setPickedUri(viewModel.getDefaultUri())
//                playChosenTone()
//            } else if (view.id == SILENT_RADIO_BTN_ID) {
//                viewModel.setPickedUri(null)
//            } else if (view.id == R.id.chooseCustomToneConstarintLayout) {
//                openFileBrowser()
//            } else {
//                viewModel.setPickedUri(
//                    viewModel.getToneUriList()[viewModel.getToneIdList().indexOf(view.id)]
//                )
//                playChosenTone()
//            }
//        }
//
//        fun onDoneBtnClick() {
//            if (viewModel.getPickedUri() == null) {
//                if (viewModel.getShowSilent()) {
//                    val intent = Intent().putExtra(
//                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
//                        viewModel.getPickedUri()
//                    )
//                }
//            } else {
//                val intent = Intent().putExtra(
//                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
//                    viewModel.getPickedUri()
//                )
//            }
//        }
//    }
//
//    /**
//     * Populate [.radioGroup] by creating and adding appropriate
//     * [RadioButton].
//     */
//    private fun populateRadioGroup() {
//        if (viewModel.getShowDefault()) {
//            createOneRadioButton(
//                DEFAULT_RADIO_BTN_ID,
//                resources.getString(R.string.defaultTone)
//            )
//        }
//
//        if (viewModel.getShowSilent()) {
//            createOneRadioButton(
//                SILENT_RADIO_BTN_ID,
//                resources.getString(R.string.silentTone)
//            )
//        }
//
//        for (i in 0 until viewModel.getToneIdList().size) {
//            createOneRadioButton(
//                viewModel.getToneIdList()[i],
//                viewModel.getToneNameList()[i]
//            )
//        }
//
//        if (viewModel.getExistingUri() != null) {
//            // As existingUri is not null, we are required to pre-select
//            // a specific RadioButton.
//            if (viewModel.getShowDefault() &&
//                viewModel.getExistingUri()!!.equals(viewModel.getDefaultUri())
//            ) {
//                // The existingUri is same as defaultUri, and showDefault is true.
//                // So, we check the "Default" RadioButton.
//                (findViewById<View>(DEFAULT_RADIO_BTN_ID) as RadioButton).isChecked = true
//                setPickedUri(viewModel.getDefaultUri())
//            } else {
//                // Find index of existingUri in toneUriList
//
//                val index = viewModel.getToneUriList()
//                    .indexOf(viewModel.getExistingUri())
//
//                if (index != -1) {
//                    // toneUriList has existingUri. Check the corresponding RadioButton.
//
//                    (findViewById(
//                        viewModel.getToneIdList()[index]
//                    ) as RadioButton).isChecked = true
//                    setPickedUri(viewModel.getExistingUri())
//                } else {
//                    // toneUriList does NOT have existingUri. It is a custom Uri
//                    // provided to us. We have to first check whether the file exists
//                    // or not. If it exists, we shall add that file to our RadioGroup.
//                    // If the file does not exist, we do not select any Radiogroup.
//                    context?.contentResolver?.query(
//                        /* uri = */ viewModel.getExistingUri() ?: Uri.EMPTY,
//                        /* projection = */ null,
//                        /* selection = */ null,
//                        /* selectionArgs = */ null,
//                        /* sortOrder = */ null
//                    ).use { cursor ->
//                        if (cursor != null && cursor.count > 0 &&
//                            cursor.moveToFirst()
//                        ) {
//                            // existingUri is a valid Uri.
//
//                            val fileNameWithExt: String
//                            val columnIndex: Int = cursor.getColumnIndex(
//                                OpenableColumns.DISPLAY_NAME
//                            )
//                            fileNameWithExt = if (columnIndex != -1) {
//                                cursor.getString(columnIndex)
//                            } else {
//                                cursor.getString(
//                                    RingtoneManager.TITLE_COLUMN_INDEX
//                                )
//                            }
//
//                            val toneId = View.generateViewId()
//
//                            viewModel.getToneNameList().add(fileNameWithExt)
//                            viewModel.getToneUriList().add(viewModel.getExistingUri()!!)
//                            viewModel.getToneIdList().add(toneId)
//
//                            createOneRadioButton(toneId, fileNameWithExt)
//
////                            (findViewById<View>(toneId) as RadioButton).isChecked = true
//
//                            setPickedUri(viewModel.getExistingUri())
//                        }
//                    }
//                }
//            }
//        } else {
//            if (viewModel.getWasExistingUriGiven()) {
//                // existingUri was specifically passed as a null value. If showSilent
//                // is true, we pre-select the "Silent" RadioButton. Otherwise
//                // we do not select any specific RadioButton.
//                if (viewModel.getShowSilent()) {
//                    (findViewById<View>(SILENT_RADIO_BTN_ID) as RadioButton).isChecked = true
//                }
//            }
//            setPickedUri(null)
//        }
//    }
//
//    private fun setPickedUri(newUri: Uri?) {
//        viewModel.setPickedUri(newUri)
//    }
//
//    /**
//     * Fires an implicit Intent to open a file browser and let the user choose an alarm
//     * tone.
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
//
//        //startActivityForResult(intent, FILE_REQUEST_CODE);
//    }
//
//
//    /**
//     * Checks whether [Manifest.permission.READ_MEDIA_AUDIO] (for >= Tiramisu) or
//     * [Manifest.permission.READ_EXTERNAL_STORAGE] permission is available or not.
//     *
//     * @return `true` if the permission is available, otherwise `false`.
//     */
//    private fun isPermissionAvailable(): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.READ_MEDIA_AUDIO
//            ) ==
//                    PackageManager.PERMISSION_GRANTED
//        } else {
//            ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) ==
//                    PackageManager.PERMISSION_GRANTED
//        }
//    }
//
//
//    private fun requestPermission() {
//        val permAndroidString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            Manifest.permission.READ_MEDIA_AUDIO
//        } else {
//            Manifest.permission.READ_EXTERNAL_STORAGE
//        }
//
//        val perm = ArrayList<String>()
//        perm.add(permAndroidString)
//
//        val permsLevelBundle = Bundle()
//        permsLevelBundle.putInt(
//            permAndroidString,
//            ConstantsAndStatics.PERMISSION_LEVEL_ESSENTIAL
//        )
//
//        val intent: Intent = Intent(this, Activity_ListReqPerm::class.java)
//        intent.putStringArrayListExtra(
//            ConstantsAndStatics.EXTRA_PERMS_REQUESTED,
//            perm
//        )
//        intent.putExtra(
//            ConstantsAndStatics.EXTRA_PERMS_REQUESTED_LEVEL,
//            permsLevelBundle
//        )
//
//        viewModel.setWerePermsRequested(true)
//        startActivity(intent)
//    }
//
//    private fun onPermissionDenied() {
//        viewModel.setWerePermsRequested(false)
//        context?.showToast("Operation not possible without the permission.")
//        setResult(Activity.RESULT_CANCELED)
//    }
//
//
//    private fun initialise() {
//        if (!viewModel.getIsInitialised()) {
//            val ringtoneManager = RingtoneManager(context)
//
////                .putExtra(
////                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
////                    viewModel.getCurrentToneUri()
////                )
//
//            val type = if (intent.hasExtra(RingtoneManager.EXTRA_RINGTONE_TYPE)) {
//                RingtoneManager.TYPE_ALARM
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
//                        /** Get tone list data from here */
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
//                    intent.extras?.getBoolean(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT) ?: false
//                )
//            } else {
//                viewModel.setShowDefault(true)
//            }
//
//            if (intent.hasExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT)) {
//                viewModel.setShowSilent(
//                    intent.extras?.getBoolean(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT) ?: false
//                )
//            } else {
//                viewModel.setShowSilent(false)
//            }
//
//            if (viewModel.getShowDefault()) {
//                if (intent.hasExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI)) {
//                    viewModel.setDefaultUri(
//                        Settings.System.DEFAULT_ALARM_ALERT_URI
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
//                            RingtoneManager.getActualDefaultRingtoneUri(context, type)
//                        )
//                    }
//                }
//            } else {
//                viewModel.setDefaultUri(null)
//            }
//
//            if (intent.hasExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI)) {
//                viewModel.setExistingUri(
//                    intent.extras?.getParcelable(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI)
//                )
//                viewModel.setWasExistingUriGiven(true)
//            } else {
//                viewModel.setExistingUri(null)
//                viewModel.setWasExistingUriGiven(false)
//            }
//
//            if (intent.hasExtra(RingtoneManager.EXTRA_RINGTONE_TITLE)) {
//                val title = intent.extras?.getString(RingtoneManager.EXTRA_RINGTONE_TITLE) ?: ""
//                viewModel.setTitle(
//                    title.ifBlank { resources.getString(R.string.ringtonePicker_defaultTitle) }
//                )
//            } else {
//                viewModel.setTitle(
//                    (resources.getString(
//                        R.string.ringtonePicker_defaultTitle
//                    ) as CharSequence)
//                )
//            }
//
//            if (intent.hasExtra(ConstantsAndStatics.EXTRA_PLAY_RINGTONE)) {
//                viewModel.setPlayTone(
//                    intent.extras?.getBoolean(ConstantsAndStatics.EXTRA_PLAY_RINGTONE) ?: false
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
//        getSupportActionBar().setTitle(viewModel.getTitle())
//
//        populateRadioGroup()
//    }
//
//
//    private fun playChosenTone() {
//        if (viewModel.getPickedUri() != null && viewModel.getPlayTone()) {
//            try {
//                mediaPlayer.apply {
//                    reset()
//                    setDataSource(requireContext(), viewModel.getPickedUri()!!)
//                    isLooping = false
//                    setAudioAttributes(audioAttributes)
//                    prepareAsync()
//                    setOnPreparedListener { audioFocusController.requestFocus() }
//                }
//            } catch (_: Exception) {
//            }
//        }
//    }
}