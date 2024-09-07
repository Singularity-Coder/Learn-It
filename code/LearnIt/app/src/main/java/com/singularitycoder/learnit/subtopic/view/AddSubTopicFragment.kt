package com.singularitycoder.learnit.subtopic.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentAddSubTopicBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.collectLatestLifecycleFlow
import com.singularitycoder.learnit.helpers.drawable
import com.singularitycoder.learnit.subject.view.MainActivity
import com.singularitycoder.learnit.subtopic.model.SubTopic
import com.singularitycoder.learnit.subtopic.viewmodel.SubTopicViewModel
import com.singularitycoder.learnit.topic.model.Topic
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddSubTopicFragment : Fragment() {

    companion object {
        private const val KEY_TOPIC = "KEY_TOPIC"

        @JvmStatic
        fun newInstance(topic: Topic) = AddSubTopicFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_TOPIC, topic)
            }
        }
    }

    private lateinit var binding: FragmentAddSubTopicBinding

    private val addSubTopicsAdapter: AddSubTopicsAdapter by lazy { AddSubTopicsAdapter() }

    private val subTopicViewModel by viewModels<SubTopicViewModel>()

    private var topic: Topic? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AndroidVersions.isTiramisu()) {
            topic = arguments?.getParcelable(KEY_TOPIC, Topic::class.java)
        } else {
            topic = arguments?.getParcelable(KEY_TOPIC)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddSubTopicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        binding.observeForData()
    }

    private fun FragmentAddSubTopicBinding.setupUI() {
        layoutCustomToolbar.apply {
            ibBack.setImageDrawable(context?.drawable(R.drawable.ic_round_clear_24))
            btnDone.isVisible = true
            tvTitle.text = "Add Sub-Topics for ${topic?.title}"
        }
        layoutAddItem.etItem.hint = "Add Sub-Topic"
        rvSubTopics.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addSubTopicsAdapter
        }
    }

    private fun FragmentAddSubTopicBinding.setupUserActionListeners() {
        // https://www.youtube.com/watch?v=H9D_HoOeKWM
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            /* Drag Directions */ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            /* Swipe Directions */0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                // FIXME drag is not smooth and gets attached to its immediate next position
//                val fromPosition = viewHolder.adapterPosition
//                val toPosition = target.adapterPosition
//                val fromPositionItem = subTopicsList[fromPosition].apply { stepNumber = toPosition + 1 }
//                subTopicsList[fromPosition] = subTopicsList[toPosition].apply { stepNumber = fromPosition + 1 }
//                subTopicsList[toPosition] = fromPositionItem
//                subTopicsAdapter.notifyItemMoved(fromPosition, toPosition)
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int,
            ) = Unit
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvSubTopics)

        layoutAddItem.ibAddItem.setOnClickListener {
            if (layoutAddItem.etItem.text.isNullOrBlank()) return@setOnClickListener
            val subTopic = SubTopic(
                topicId = topic?.id ?: return@setOnClickListener,
                title = layoutAddItem.etItem.text.toString(),
            )
            subTopicViewModel.addSubTopicItem(subTopic)
        }

        addSubTopicsAdapter.setOnItemClickListener { subTopic, position ->
        }

        layoutCustomToolbar.ibBack.setOnClickListener {
            parentFragmentManager.popBackStackImmediate()
        }

        layoutCustomToolbar.btnDone.setOnClickListener {
            parentFragmentManager.popBackStackImmediate()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentAddSubTopicBinding.observeForData() {
        (activity as? MainActivity)?.collectLatestLifecycleFlow(
            flow = subTopicViewModel.getAllTopicByTopicIdItemsFlow(topic?.id)
        ) { list: List<SubTopic> ->
            addSubTopicsAdapter.subTopicList = list
            addSubTopicsAdapter.notifyDataSetChanged()
            layoutAddItem.etItem.setText("")
        }
    }
}