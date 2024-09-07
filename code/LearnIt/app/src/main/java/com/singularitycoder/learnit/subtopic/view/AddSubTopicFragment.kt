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
import com.singularitycoder.learnit.helpers.collectLatestLifecycleFlow
import com.singularitycoder.learnit.helpers.drawable
import com.singularitycoder.learnit.helpers.showSnackBar
import com.singularitycoder.learnit.subject.view.MainActivity
import com.singularitycoder.learnit.subtopic.model.SubTopic
import com.singularitycoder.learnit.subtopic.viewmodel.SubTopicViewModel
import com.singularitycoder.learnit.topic.viewmodel.TopicViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddSubTopicFragment : Fragment() {

    companion object {
        private const val KEY_SUBJECT_ID = "KEY_SUBJECT_ID"

        @JvmStatic
        fun newInstance(topicId: Long) = AddSubTopicFragment().apply {
            arguments = Bundle().apply {
                putLong(KEY_SUBJECT_ID, topicId)
            }
        }
    }

    private lateinit var binding: FragmentAddSubTopicBinding

    private val subTopicsAdapter = SubTopicsAdapter()

    private val topicViewModel by viewModels<TopicViewModel>()
    private val subTopicViewModel by viewModels<SubTopicViewModel>()

    private var subjectId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subjectId = it.getLong(KEY_SUBJECT_ID, 0L)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddSubTopicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.observeForData()
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentAddSubTopicBinding.observeForData() {
        (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = subTopicViewModel.getAllSubTopicItemsFlow()) { list: List<SubTopic> ->
            subTopicsAdapter.subTopicList = list
            subTopicsAdapter.notifyDataSetChanged()
            layoutAddItem.etItem.setText("")
        }
    }

    // https://www.youtube.com/watch?v=H9D_HoOeKWM
    private fun FragmentAddSubTopicBinding.setupUI() {
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

        layoutCustomToolbar.apply {
            ibBack.setImageDrawable(context?.drawable(R.drawable.ic_round_clear_24))
            btnDone.isVisible = true
            tvTitle.text = "Add Sub-Topics"
        }
        layoutAddItem.etItem.hint = "Add Sub-Topic"
        rvSubTopics.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subTopicsAdapter
        }
    }

    private fun FragmentAddSubTopicBinding.setupUserActionListeners() {
        layoutAddItem.ibAddItem.setOnClickListener {
            if (layoutAddItem.etItem.text.isNullOrBlank()) return@setOnClickListener
            val subTopic = SubTopic(
                topicId = "",
                title = layoutAddItem.etItem.text.toString(),
                isCorrectRecall = false,
            )
            subTopicViewModel.addSubTopicItem(subTopic)
        }

        subTopicsAdapter.setOnItemClickListener { subTopic, position ->
        }

        layoutCustomToolbar.ibBack.setOnClickListener {
            parentFragmentManager.popBackStackImmediate()
        }

        layoutCustomToolbar.btnDone.setOnClickListener {
            if (subTopicsAdapter.subTopicList.isEmpty()) {
                binding.root.showSnackBar("Steps are required!")
                return@setOnClickListener
            }
        }

//        rvSubTopics.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                print("$dx $dy")
//            }
//        })
    }
}