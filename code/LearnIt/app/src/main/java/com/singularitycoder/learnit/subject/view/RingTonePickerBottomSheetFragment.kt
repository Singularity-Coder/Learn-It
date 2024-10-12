package com.singularitycoder.learnit.subject.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.learnit.databinding.FragmentRingTonePickerBottomSheetBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.constants.EditEvent
import com.singularitycoder.learnit.helpers.constants.globalSlideToBottomAnimation
import com.singularitycoder.learnit.helpers.enableSoftInput
import com.singularitycoder.learnit.helpers.hideKeyboard
import com.singularitycoder.learnit.helpers.layoutAnimationController
import com.singularitycoder.learnit.helpers.onImeClick
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.setTransparentBackground
import com.singularitycoder.learnit.helpers.showKeyboard
import com.singularitycoder.learnit.subtopic.model.SubTopic
import com.singularitycoder.learnit.subtopic.view.SubTopicsAdapter
import com.singularitycoder.learnit.subtopic.viewmodel.SubTopicViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingTonePickerBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_EDIT_EVENT_TYPE = "ARG_EDIT_EVENT_TYPE"

        @JvmStatic
        fun newInstance(eventType: EditEvent) = RingTonePickerBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_EDIT_EVENT_TYPE, eventType)
            }
        }
    }

    private var subTopicList = listOf<SubTopic?>()

    private val subTopicViewModel by viewModels<SubTopicViewModel>()

    private val subTopicsAdapter: SubTopicsAdapter by lazy { SubTopicsAdapter() }

    private lateinit var binding: FragmentRingTonePickerBottomSheetBinding

    private var eventType: EditEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AndroidVersions.isTiramisu()) {
            eventType = arguments?.getParcelable(ARG_EDIT_EVENT_TYPE, EditEvent::class.java)
        } else {
            eventType = arguments?.getParcelable(ARG_EDIT_EVENT_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRingTonePickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    private fun FragmentRingTonePickerBottomSheetBinding.setupUI() {
        enableSoftInput()
        setTransparentBackground()


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentRingTonePickerBottomSheetBinding.setupUserActionListeners() {
        ivSearch.onSafeClick {
            clSearch.layoutAnimation = clSearch.context.layoutAnimationController(globalSlideToBottomAnimation)
            etSearch.setText("")
            clSearch.isVisible = clSearch.isVisible.not()
            if (clSearch.isVisible) {
                etSearch.showKeyboard()
            } else {
                etSearch.hideKeyboard()
            }
        }

        ibClearSearch.onSafeClick {
            etSearch.setText("")
        }

        etSearch.doAfterTextChanged { query: Editable? ->
            ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                subTopicsAdapter.subTopicList = subTopicList
                subTopicsAdapter.notifyDataSetChanged()
                return@doAfterTextChanged
            }
            subTopicsAdapter.subTopicList = subTopicList.filter {
                it?.title?.contains(other = query, ignoreCase = true) == true
            }
            subTopicsAdapter.notifyDataSetChanged()
        }

        etSearch.onImeClick {
            etSearch.hideKeyboard()
        }
    }
}