package com.singularitycoder.learnit.topic.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentTopicBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.BottomSheetTag
import com.singularitycoder.learnit.helpers.EditEvent
import com.singularitycoder.learnit.helpers.FragmentResultBundleKey
import com.singularitycoder.learnit.helpers.FragmentResultKey
import com.singularitycoder.learnit.helpers.FragmentsTag
import com.singularitycoder.learnit.helpers.collectLatestLifecycleFlow
import com.singularitycoder.learnit.helpers.globalLayoutAnimation
import com.singularitycoder.learnit.helpers.layoutAnimationController
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.showScreen
import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.subject.view.MainActivity
import com.singularitycoder.learnit.subject.view.SubjectsAdapter
import com.singularitycoder.learnit.subtopic.view.AddSubTopicFragment
import com.singularitycoder.learnit.topic.model.Topic
import com.singularitycoder.learnit.topic.viewmodel.TopicViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TopicFragment : Fragment() {

    companion object {
        private const val KEY_SUBJECT = "KEY_SUBJECT"

        @JvmStatic
        fun newInstance(subject: Subject?) = TopicFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_SUBJECT, subject)
            }
        }
    }

    private lateinit var binding: FragmentTopicBinding

    private val topicsAdapter = TopicsAdapter()

    private var topicList = listOf<Topic?>()

    private val viewModel by viewModels<TopicViewModel>()

    private var subject: Subject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AndroidVersions.isTiramisu()) {
            subject = arguments?.getParcelable(KEY_SUBJECT, Subject::class.java)
        } else {
            subject = arguments?.getParcelable(KEY_SUBJECT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTopicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentTopicBinding.setupUI() {
        layoutCustomToolbar.tvTitle.text = "${subject?.title} Topics"
        rvTopics.apply {
            layoutAnimation = rvTopics.context.layoutAnimationController(globalLayoutAnimation)
            layoutManager = LinearLayoutManager(context)
            adapter = topicsAdapter
        }
    }

    private fun FragmentTopicBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        layoutCustomToolbar.ibBack.onSafeClick {
            parentFragmentManager.popBackStackImmediate()
        }

        fabAdd.onSafeClick {
            EditBottomSheetFragment.newInstance(
                eventType = EditEvent.ADD_TOPIC,
                subject = subject
            ).show(parentFragmentManager, BottomSheetTag.TAG_EDIT)
        }

        parentFragmentManager.setFragmentResultListener(
            /* requestKey = */ FragmentResultKey.ADD_TOPIC,
            /* lifecycleOwner = */ viewLifecycleOwner
        ) { _, bundle: Bundle ->
            val topicId = bundle.getLong(FragmentResultBundleKey.TOPIC_ID)
            (requireActivity() as MainActivity).showScreen(
                fragment = AddSubTopicFragment.newInstance(topicId),
                tag = FragmentsTag.ADD_TOPIC,
                isAdd = true,
                enterAnim = R.anim.slide_to_top,
                exitAnim = R.anim.slide_to_bottom,
                popEnterAnim = R.anim.slide_to_top,
                popExitAnim = R.anim.slide_to_bottom,
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = viewModel.getAllTopicItemsFlow()) { list: List<Topic?> ->
            topicList = list
            topicsAdapter.topicList = topicList
            topicsAdapter.notifyDataSetChanged()
        }
    }
}