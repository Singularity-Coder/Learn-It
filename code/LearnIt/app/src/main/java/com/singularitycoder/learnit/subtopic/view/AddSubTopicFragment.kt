package com.singularitycoder.learnit.subtopic.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentAddSubTopicBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.collectLatestLifecycleFlow
import com.singularitycoder.learnit.helpers.drawable
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.showAlertDialog
import com.singularitycoder.learnit.helpers.showKeyboard
import com.singularitycoder.learnit.helpers.showPopupMenuWithIcons
import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.subject.view.MainActivity
import com.singularitycoder.learnit.subtopic.model.SubTopic
import com.singularitycoder.learnit.subtopic.viewmodel.SubTopicViewModel
import com.singularitycoder.learnit.topic.model.Topic
import com.singularitycoder.learnit.topic.view.TopicFragment
import com.singularitycoder.learnit.topic.view.TopicFragment.Companion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AddSubTopicFragment : Fragment() {

    companion object {
        private const val KEY_TOPIC = "KEY_TOPIC"
        private const val KEY_SUBJECT = "KEY_SUBJECT"

        @JvmStatic
        fun newInstance(
            topic: Topic?,
            subject: Subject?
        ) = AddSubTopicFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_TOPIC, topic)
                putParcelable(KEY_SUBJECT, subject)
            }
        }
    }

    private lateinit var binding: FragmentAddSubTopicBinding

    private val addSubTopicsAdapter: AddSubTopicsAdapter by lazy { AddSubTopicsAdapter() }

    private val subTopicViewModel by activityViewModels<SubTopicViewModel>()

    private var topic: Topic? = null
    private var subject: Subject? = null

    /** https://yfujiki.medium.com/drag-and-reorder-recyclerview-items-in-a-user-friendly-manner-1282335141e9
     * https://github.com/yfujiki/Android-DragReorderSample/blob/master/app/src/main/java/com/yfujiki/android_dragreordersample/MainActivity.kt */
    private val itemTouchHelper by lazy {
        // TODO fix dragging - its crashing
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            /* Drag Directions */ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            /* Swipe Directions */0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                val adapter = recyclerView.adapter as AddSubTopicsAdapter
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                /** 2. Update the backing model. Custom implementation in AddSubTopicsAdapter. You need to implement reordering of the backing model inside the method. */
                adapter.moveItem(from, to)
                /** 3. Tell adapter to render the model update. */
                adapter.notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int,
            ) {
                /** 4. Code block for horizontal swipe. ItemTouchHelper handles horizontal swipe as well, but it is not relevant with reordering. Ignoring here. */
            }
        }
        ItemTouchHelper(itemTouchHelperCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AndroidVersions.isTiramisu()) {
            topic = arguments?.getParcelable(KEY_TOPIC, Topic::class.java)
            subject = arguments?.getParcelable(KEY_SUBJECT, Subject::class.java)
        } else {
            topic = arguments?.getParcelable(KEY_TOPIC)
            subject = arguments?.getParcelable(KEY_SUBJECT)
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
            tvTitle.text = "${topic?.title ?: ""} Sub-Topics"
        }
        layoutAddItem.etItem.hint = "Add Sub-Topic"
        rvSubTopics.apply {
            itemTouchHelper.attachToRecyclerView(this)
            layoutManager = LinearLayoutManager(context)
            adapter = addSubTopicsAdapter
        }
    }

    private fun FragmentAddSubTopicBinding.setupUserActionListeners() {
        layoutAddItem.ibAddItem.setOnClickListener {
            if (layoutAddItem.etItem.text.isNullOrBlank()) return@setOnClickListener
            val subTopic = SubTopic(
                topicId = topic?.id ?: return@setOnClickListener,
                subjectId = subject?.id ?: return@setOnClickListener,
                title = layoutAddItem.etItem.text.toString(),
            )
            subTopicViewModel.addSubTopicItem(subTopic)
        }

        addSubTopicsAdapter.setOnItemClickListener { subTopic, position ->
        }

        addSubTopicsAdapter.setOnItemLongClickListener { subTopic, view, position ->
            val optionsList = listOf(
                Pair("Edit", R.drawable.outline_edit_24),
                Pair("Delete", R.drawable.outline_delete_24),
            )
            requireContext().showPopupMenuWithIcons(
                view = view,
                menuList = optionsList,
                customColor = R.color.md_red_700,
                customColorItemText = optionsList.last().first
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        addSubTopicsAdapter.showEditView(
                            recyclerView = rvSubTopics,
                            adapterPosition = position
                        )
                    }

                    optionsList[1].first -> {
                        requireContext().showAlertDialog(
                            title = "Delete Item",
                            message = "\"${subTopic?.title}\" will be deleted.",
                            positiveBtnText = "Delete",
                            negativeBtnText = "Cancel",
                            positiveBtnColor = R.color.md_red_700,
                            positiveAction = {
                                subTopicViewModel.deleteSubTopic(subTopic)
                            }
                        )
                    }
                }
            }
        }

        addSubTopicsAdapter.setOnApproveUpdateClickListener { subTopic, position ->
            subTopicViewModel.updateSubTopic(subTopic)
        }

        layoutCustomToolbar.ibBack.onSafeClick {
            /** Updating all items to remember the reorder position. Uncomment when u fix drag to reposition */
//            subTopicViewModel.updateAllSubTopics(addSubTopicsAdapter.subTopicList.filterNotNull())
            parentFragmentManager.popBackStackImmediate()
        }

        layoutCustomToolbar.ivMore.onSafeClick { pair: Pair<View?, Boolean> ->
            val optionsList = listOf(
                Pair("Delete All", R.drawable.outline_delete_24),
            )
            requireContext().showPopupMenuWithIcons(
                view = pair.first,
                menuList = optionsList,
                customColor = R.color.md_red_700,
                customColorItemText = optionsList.last().first
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        requireContext().showAlertDialog(
                            message = "Delete all items from \"${topic?.title}\" topic? You cannot undo this action.",
                            positiveBtnText = "Delete",
                            negativeBtnText = "Cancel",
                            positiveBtnColor = R.color.md_red_700,
                            positiveAction = {
                                subTopicViewModel.deleteAllSubTopicsBy(topic?.id)
                            }
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentAddSubTopicBinding.observeForData() {
        (activity as? MainActivity)?.collectLatestLifecycleFlow(
            flow = subTopicViewModel.getAllTopicByTopicIdItemsFlow(topic?.id)
        ) { list: List<SubTopic> ->
            withContext(Dispatchers.Main) {
                if (list.isEmpty()) {
                    binding.layoutAddItem.etItem.showKeyboard()
                }
            }
            addSubTopicsAdapter.subTopicList = list.toMutableList()
            addSubTopicsAdapter.notifyDataSetChanged()
            layoutAddItem.etItem.setText("")
        }
    }
}