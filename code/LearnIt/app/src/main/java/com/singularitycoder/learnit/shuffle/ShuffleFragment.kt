package com.singularitycoder.learnit.shuffle

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentShuffleBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.constants.ShuffleType
import com.singularitycoder.learnit.helpers.constants.globalLayoutAnimation
import com.singularitycoder.learnit.helpers.drawable
import com.singularitycoder.learnit.helpers.layoutAnimationController
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.runLayoutAnimation
import com.singularitycoder.learnit.helpers.showAlertDialog
import com.singularitycoder.learnit.helpers.showPopupMenuWithIcons
import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.subtopic.view.SubTopicsAdapter
import com.singularitycoder.learnit.subtopic.viewmodel.SubTopicViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ShuffleFragment : Fragment() {

    companion object {
        private const val ARG_SHUFFLE_TYPE = "ARG_SHUFFLE_TYPE"
        private const val ARG_SUBJECT = "ARG_SUBJECT"

        @JvmStatic
        fun newInstance(
            shuffleType: String,
            subject: Subject?
        ) = ShuffleFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SHUFFLE_TYPE, shuffleType)
                putParcelable(ARG_SUBJECT, subject)
            }
        }
    }

    private lateinit var binding: FragmentShuffleBinding

    private val subTopicsAdapter: SubTopicsAdapter by lazy { SubTopicsAdapter() }

    private val subTopicViewModel by activityViewModels<SubTopicViewModel>()

    private var isNewInstance: Boolean = true

    private var shuffleType: String? = null
    private var subject: Subject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AndroidVersions.isTiramisu()) {
            shuffleType = arguments?.getString(ARG_SHUFFLE_TYPE)
            subject = arguments?.getParcelable(ARG_SUBJECT, Subject::class.java)
        } else {
            shuffleType = arguments?.getString(ARG_SHUFFLE_TYPE)
            subject = arguments?.getParcelable(ARG_SUBJECT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentShuffleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentShuffleBinding.setupUI() {
        layoutCustomToolbar.apply {
            ibBack.setImageDrawable(context?.drawable(R.drawable.ic_round_clear_24))
            tvTitle.text = if (shuffleType == ShuffleType.ALL_TOPICS) {
                "Shuffle ${subject?.title} Sub-Topics"
            } else {
                "Shuffle All Sub-Topics"
            }
        }
        rvSubTopics.apply {
            layoutAnimation = rvSubTopics.context.layoutAnimationController(globalLayoutAnimation)
            layoutManager = LinearLayoutManager(context)
            adapter = subTopicsAdapter
        }
        subTopicsAdapter.isVisibleHint = true
        if (shuffleType == ShuffleType.ALL_SUBJECTS) {
            lifecycleScope.launch(Dispatchers.IO) {
                val list = subTopicViewModel.getAllSubTopics()
                subTopicsAdapter.subTopicList = list.toMutableList().shuffled()
                subTopicsAdapter.notifyDataSetChanged()
                if (isNewInstance) {
                    binding.rvSubTopics.runLayoutAnimation(globalLayoutAnimation)
                    isNewInstance = false
                }
                layoutCustomToolbar.tvCount.text =
                    "${list.size} Sub-Topics   |   ${list.filter { it.isCorrectRecall }.size} Recalled"
            }
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                val list = subTopicViewModel.getAllSubTopicsBy(subject?.id)
                subTopicsAdapter.subTopicList = list.toMutableList().shuffled()
                subTopicsAdapter.notifyDataSetChanged()
                if (isNewInstance) {
                    binding.rvSubTopics.runLayoutAnimation(globalLayoutAnimation)
                    isNewInstance = false
                }
                layoutCustomToolbar.tvCount.text =
                    "${list.size} Sub-Topics   |   ${list.filter { it.isCorrectRecall }.size} Recalled"
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentShuffleBinding.setupUserActionListeners() {
        subTopicsAdapter.setOnItemClickListener { subTopic, position ->
        }

        subTopicsAdapter.setOnHintClickListener { subTopic, position ->
            subTopic ?: return@setOnHintClickListener
            lifecycleScope.launch(Dispatchers.IO) {
                val subject = subTopicViewModel.getSubjectById(subTopic.subjectId)
                val topic = subTopicViewModel.getTopicById(subTopic.topicId)
                withContext(Dispatchers.Main) {
                    requireContext().showAlertDialog(
                        title = "Hint",
                        message = """
                             Sub-Topic: ${subTopic.title} 
                             Topic: ${topic.title}
                             Subject: ${subject.title}
                         """.trimIndent(),
                        positiveBtnText = "OK",
                        positiveBtnColor = R.color.purple_500
                    )
                }
            }
        }

        subTopicsAdapter.setOnApproveUpdateClickListener { subTopic, position ->
            subTopicViewModel.updateSubTopic(subTopic)
            subTopicsAdapter.checkMarkItem(
                recyclerView = rvSubTopics,
                adapterPosition = position,
                isChecked = subTopicsAdapter.subTopicList.get(position)?.isCorrectRecall?.not() ?: false
            )
            layoutCustomToolbar.tvCount.text =
                "${subTopicsAdapter.subTopicList.size} Sub-Topics   |   ${subTopicsAdapter.subTopicList.filter { it?.isCorrectRecall == true }.size} Recalled"
        }

        layoutCustomToolbar.ibBack.onSafeClick {
            parentFragmentManager.popBackStackImmediate()
        }

        layoutCustomToolbar.ivMore.onSafeClick { pair: Pair<View?, Boolean> ->
            val optionsList = listOf(
                Pair("Reset", R.drawable.round_settings_backup_restore_24)
            )
            requireContext().showPopupMenuWithIcons(
                view = pair.first,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        subTopicViewModel.updateAllSubTopics(
                            subTopicsAdapter.subTopicList.map { it?.copy(isCorrectRecall = false) }.filterNotNull()
                        )
                        subTopicsAdapter.subTopicList = subTopicsAdapter.subTopicList.map { it?.copy(isCorrectRecall = false) }
                        subTopicsAdapter.notifyDataSetChanged()
                        layoutCustomToolbar.tvCount.text =
                            "${subTopicsAdapter.subTopicList.size} Sub-Topics   |   ${subTopicsAdapter.subTopicList.filter { it?.isCorrectRecall == true }.size} Recalled"
                    }
                }
            }
        }
    }
}