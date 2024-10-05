package com.singularitycoder.learnit.topic.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentEditBottomSheetBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.color
import com.singularitycoder.learnit.helpers.constants.EditEvent
import com.singularitycoder.learnit.helpers.constants.FragmentResultBundleKey
import com.singularitycoder.learnit.helpers.constants.FragmentResultKey
import com.singularitycoder.learnit.helpers.dpToPx
import com.singularitycoder.learnit.helpers.enableSoftInput
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.setTransparentBackground
import com.singularitycoder.learnit.helpers.showKeyboard
import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.topic.model.Topic
import com.singularitycoder.learnit.topic.viewmodel.TopicViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class EditBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_PARAM_EDIT_EVENT_TYPE = "ARG_PARAM_EDIT_EVENT_TYPE"
        private const val KEY_SUBJECT = "KEY_SUBJECT"
        private const val KEY_TOPIC = "KEY_TOPIC"

        @JvmStatic
        fun newInstance(
            eventType: EditEvent,
            subject: Subject?,
            topic: Topic?
        ) = EditBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PARAM_EDIT_EVENT_TYPE, eventType)
                putParcelable(KEY_SUBJECT, subject)
                putParcelable(KEY_TOPIC, topic)
            }
        }
    }

    private val topicViewModel by viewModels<TopicViewModel>()

    private lateinit var binding: FragmentEditBottomSheetBinding

    private var isValidTopic = false
    private var isValidStudyMaterial = false

    private var eventType: EditEvent? = null
    private var subject: Subject? = null
    private var topic: Topic? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AndroidVersions.isTiramisu()) {
            eventType = arguments?.getParcelable(ARG_PARAM_EDIT_EVENT_TYPE, EditEvent::class.java)
            subject = arguments?.getParcelable(KEY_SUBJECT, Subject::class.java)
            topic = arguments?.getParcelable(KEY_TOPIC, Topic::class.java)
        } else {
            eventType = arguments?.getParcelable(ARG_PARAM_EDIT_EVENT_TYPE)
            subject = arguments?.getParcelable(KEY_SUBJECT)
            topic = arguments?.getParcelable(KEY_TOPIC)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    private fun FragmentEditBottomSheetBinding.setupUI() {
        enableSoftInput()
        setTransparentBackground()
        setBottomSheetExpanded()

        etEdit.hint = "Topic name"
        etEdit2.hint = "Study material"

        layoutVolumeSlider.apply {
            tvSliderTitle.text = "Volume: 5"
            sliderCustom.min = 1
            sliderCustom.max = 10
            sliderCustom.progress = 5
        }

        when (eventType) {
            EditEvent.ADD_TOPIC -> {
                tvHeader.text = "Add Topic"
            }

            EditEvent.UPDATE_TOPIC -> {
                tvHeader.text = "Update Topic"
                etEdit.editText?.setText(topic?.title)
                etEdit2.editText?.setText(topic?.studyMaterial)
            }

            else -> Unit
        }

        val isDoneBtnEnabled = etEdit.editText?.text.isNullOrBlank().not() && etEdit2.editText?.text.isNullOrBlank().not()
        btnDone.isEnabled = isDoneBtnEnabled
        if (isDoneBtnEnabled) {
            isValidTopic = true
            isValidStudyMaterial = true
        }

        etAlarmType.editText?.setText("Sound")
        val imageQuantityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOf("Sound", "Vibrate", "Sound & Vibrate"))
        (etAlarmType.editText as? AutoCompleteTextView)?.setAdapter(imageQuantityAdapter)

        etAlarmTone.editText?.setText("Default")
        val imageSizeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOf("256x256", "512x512", "1024x1024"))
        (etAlarmTone.editText as? AutoCompleteTextView)?.setAdapter(imageSizeAdapter)

        etEdit.editText?.showKeyboard()
    }

    private fun FragmentEditBottomSheetBinding.setupUserActionListeners() {
        etEdit.editText?.doAfterTextChanged { it: Editable? ->
            if (etEdit.editText?.text.isNullOrBlank()) {
                etEdit.error = "This is required!"
                isValidTopic = false
                etEdit.cursorErrorColor = ColorStateList.valueOf(requireContext().color(R.color.title_color))
            } else {
                etEdit.error = null
                isValidTopic = true
            }
            btnDone.isEnabled = isValidTopic && isValidStudyMaterial
        }

        etEdit2.editText?.doAfterTextChanged { it: Editable? ->
            if (etEdit2.editText?.text.isNullOrBlank()) {
                etEdit2.error = "This is required!"
                isValidStudyMaterial = false
                etEdit2.cursorErrorColor = ColorStateList.valueOf(requireContext().color(R.color.title_color))
            } else {
                etEdit2.error = null
                isValidStudyMaterial = true
            }
            btnDone.isEnabled = isValidTopic && isValidStudyMaterial
        }

        etEdit.editText?.setOnFocusChangeListener { view, isFocused ->
            etEdit.boxStrokeWidth = if (etEdit.editText?.text.isNullOrBlank().not()) 2.dpToPx().toInt() else 0
            etEdit.error = null
        }

        etEdit2.editText?.setOnFocusChangeListener { view, isFocused ->
            etEdit2.boxStrokeWidth = if (etEdit2.editText?.text.isNullOrBlank().not()) 2.dpToPx().toInt() else 0
            etEdit2.error = null
        }

        btnDone.onSafeClick {
            etEdit.error = null
            etEdit2.error = null

            if (etEdit.editText?.text.isNullOrBlank()) {
                etEdit.boxStrokeWidth = 2.dpToPx().toInt()
                etEdit.error = "This is required!"
                return@onSafeClick
            }

            if (etEdit2.editText?.text.isNullOrBlank()) {
                etEdit2.boxStrokeWidth = 2.dpToPx().toInt()
                etEdit2.error = "This is required!"
                return@onSafeClick
            }

            btnDone.isVisible = false
            progressCircular.isVisible = true

            CoroutineScope(Dispatchers.IO).launch {
                when (eventType) {
                    EditEvent.ADD_TOPIC -> {
                        val topic = Topic(
                            subjectId = subject?.id ?: 0L,
                            title = etEdit.editText?.text?.toString() ?: "",
                            studyMaterial = etEdit2.editText?.text?.toString() ?: "",
                            dateStarted = 0L,
                            nextSessionDate = 0L,
                            finishedSessions = 0,
                            revisionCount = 0
                        )
                        val topicId = topicViewModel.addTopic(topic)
                        val topicWithId = topicViewModel.getTopicById(topicId)

                        withContext(Dispatchers.Main) {
                            parentFragmentManager.setFragmentResult(
                                /* requestKey = */ FragmentResultKey.ADD_TOPIC,
                                /* result = */ bundleOf(FragmentResultBundleKey.TOPIC to topicWithId)
                            )
                        }
                    }

                    EditEvent.UPDATE_TOPIC -> {
                        topicViewModel.updateTopic(
                            topic = topic?.copy(
                                title = etEdit.editText?.text?.toString() ?: "",
                                studyMaterial = etEdit2.editText?.text?.toString() ?: ""
                            )
                        )
                    }

                    else -> Unit
                }

                withContext(Dispatchers.Main) {
                    progressCircular.isVisible = false
                    dismiss()
                }
            }
        }

        layoutVolumeSlider.apply {
            ibReduce.onSafeClick {
                sliderCustom.progress -= 1
                tvSliderTitle.text = "Volume: ${sliderCustom.progress}"
            }
            ibIncrease.onSafeClick {
                sliderCustom.progress += 1
                tvSliderTitle.text = "Volume: ${sliderCustom.progress}"
            }
            sliderCustom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    tvSliderTitle.text = "Volume: ${seekBar.progress}"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    println("seekbar progress: ${seekBar.progress}")
                    tvSliderTitle.text = "Volume: ${seekBar.progress}"
                }
            })
        }
    }

    private fun setBottomSheetExpanded() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout? ?: return
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}