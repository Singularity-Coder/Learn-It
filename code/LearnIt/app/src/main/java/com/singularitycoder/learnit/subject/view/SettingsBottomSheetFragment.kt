package com.singularitycoder.learnit.subject.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.learnit.databinding.FragmentSettingsBottomSheetBinding
import com.singularitycoder.learnit.helpers.collectLatestLifecycleFlow
import com.singularitycoder.learnit.helpers.constants.remindMeInList
import com.singularitycoder.learnit.helpers.setTransparentBackground
import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.subtopic.model.SubTopic
import com.singularitycoder.learnit.subtopic.viewmodel.SubTopicViewModel
import com.singularitycoder.learnit.topic.model.Topic
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = SettingsBottomSheetFragment()
    }

    private lateinit var binding: FragmentSettingsBottomSheetBinding

//    private val subTopicViewModel by viewModels<SubTopicViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentSettingsBottomSheetBinding.setupUI() {
        setTransparentBackground()
        tvHeader.text = "Settings"
        layoutVolumeSlider.tvSliderTitle.isVisible = false
        layoutShakeSensitivitySlider.tvSliderTitle.isVisible = false

        etOnShake.editText?.setText(remindMeInList.first())
        val onShakeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, remindMeInList)
        (etOnShake.editText as? AutoCompleteTextView)?.setAdapter(onShakeAdapter)

        etPowerBtn.editText?.setText(remindMeInList.first())
        val powerBtnAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, remindMeInList)
        (etPowerBtn.editText as? AutoCompleteTextView)?.setAdapter(powerBtnAdapter)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentSettingsBottomSheetBinding.setupUserActionListeners() {
        switchDefaultAlarmTone.setOnCheckedChangeListener { buttonView, isChecked ->
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
//        (activity as? MainActivity)?.collectLatestLifecycleFlow(
//            flow = subTopicViewModel.getAllTopicByTopicIdItemsFlow(topic?.id)
//        ) { list: List<SubTopic> ->
//
//        }
    }
}