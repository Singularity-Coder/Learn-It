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
import com.singularitycoder.learnit.helpers.constants.globalLayoutAnimation
import com.singularitycoder.learnit.helpers.drawable
import com.singularitycoder.learnit.helpers.layoutAnimationController
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.runLayoutAnimation
import com.singularitycoder.learnit.helpers.showAlertDialog
import com.singularitycoder.learnit.helpers.showKeyboard
import com.singularitycoder.learnit.helpers.showPopupMenuWithIcons
import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.subject.view.MainActivity
import com.singularitycoder.learnit.subtopic.model.SubTopic
import com.singularitycoder.learnit.subtopic.viewmodel.SubTopicViewModel
import com.singularitycoder.learnit.topic.model.Topic
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections

@AndroidEntryPoint
class AddSubTopicFragment : Fragment() {

    companion object {
        private const val ARG_TOPIC = "ARG_TOPIC"
        private const val ARG_SUBJECT = "ARG_SUBJECT"

        @JvmStatic
        fun newInstance(
            topic: Topic?,
            subject: Subject?
        ) = AddSubTopicFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_TOPIC, topic)
                putParcelable(ARG_SUBJECT, subject)
            }
        }
    }

    private lateinit var binding: FragmentAddSubTopicBinding

    private val addSubTopicsAdapter: AddSubTopicsAdapter by lazy { AddSubTopicsAdapter() }

    private val subTopicViewModel by activityViewModels<SubTopicViewModel>()

    private var topic: Topic? = null
    private var subject: Subject? = null

    private val itemTouchHelper by lazy {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            /* Drag Directions */ItemTouchHelper.UP or ItemTouchHelper.DOWN /*or ItemTouchHelper.START or ItemTouchHelper.END*/,
            /* Swipe Directions */0
        ) {
            var fromPos = 0
            var toPos = 0

            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                val adapter = recyclerView.adapter as AddSubTopicsAdapter
                fromPos = source.bindingAdapterPosition
                toPos = target.bindingAdapterPosition

                /** 2. Update the backing model. Custom implementation in AddSubTopicsAdapter. You need to implement reordering of the backing model inside the method. */
                Collections.swap(adapter.subTopicList, fromPos, toPos)

                /** 3. Tell adapter to render the model update. */
                adapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            /** 4. User has finished drag, save new item order to database */
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                val adapter = recyclerView.adapter as AddSubTopicsAdapter
                subTopicViewModel.updateAllSubTopics(adapter.subTopicList.filterNotNull())
            }

            /** 5. Code block for horizontal swipe. ItemTouchHelper handles horizontal swipe as well, but it is not relevant with reordering. Ignoring here. */
            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int,
            ) = Unit
        }
        ItemTouchHelper(itemTouchHelperCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AndroidVersions.isTiramisu()) {
            topic = arguments?.getParcelable(ARG_TOPIC, Topic::class.java)
            subject = arguments?.getParcelable(ARG_SUBJECT, Subject::class.java)
        } else {
            topic = arguments?.getParcelable(ARG_TOPIC)
            subject = arguments?.getParcelable(ARG_SUBJECT)
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

    override fun onPause() {
        super.onPause()
        addSubTopicsAdapter.removeHandlerCallback()
    }

    private fun FragmentAddSubTopicBinding.setupUI() {
        layoutCustomToolbar.apply {
            ibBack.setImageDrawable(context?.drawable(R.drawable.ic_round_clear_24))
            tvTitle.text = "${topic?.title ?: ""} Sub-Topics"
        }
        layoutAddItem.etItem.hint = "Add Sub-Topic"
        rvSubTopics.apply {
            // TODO fix dragging
            layoutAnimation = rvSubTopics.context.layoutAnimationController(globalLayoutAnimation)
            layoutManager = LinearLayoutManager(context)
            adapter = addSubTopicsAdapter
//            itemTouchHelper.attachToRecyclerView(this)
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
            binding.rvSubTopics.runLayoutAnimation(globalLayoutAnimation)
            layoutAddItem.etItem.setText("")
            binding.layoutCustomToolbar.tvCount.text = "${list.size} Topics"
        }
    }
}