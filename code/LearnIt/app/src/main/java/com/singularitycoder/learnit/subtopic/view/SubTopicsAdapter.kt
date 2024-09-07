package com.singularitycoder.learnit.subtopic.view

import android.content.res.ColorStateList
import android.graphics.drawable.Icon
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.ListItemSubTopicBinding
import com.singularitycoder.learnit.helpers.color
import com.singularitycoder.learnit.helpers.onCustomLongClick
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.subtopic.model.SubTopic

class SubTopicsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var subTopicList = emptyList<SubTopic?>()
    private var itemClickListener: (subTopic: SubTopic?, position: Int) -> Unit = { _, _ -> }
    private var itemLongClickListener: (subTopic: SubTopic?, view: View?, position: Int) -> Unit = { _, _, _ -> }
    private var itemApproveUpdateClickListener: (subTopic: SubTopic?, position: Int) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemSubTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(subTopicList[position])
    }

    override fun getItemCount(): Int = subTopicList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (subTopic: SubTopic?, position: Int) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(
        listener: (
            subTopic: SubTopic?,
            view: View?,
            position: Int
        ) -> Unit
    ) {
        itemLongClickListener = listener
    }

    fun setOnApproveUpdateClickListener(listener: (subTopic: SubTopic?, position: Int) -> Unit) {
        itemApproveUpdateClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemSubTopicBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun setData(subTopic: SubTopic?) {
            itemBinding.apply {
                tvTitle.text = subTopic?.title
                root.onSafeClick {
                    itemClickListener.invoke(subTopic, bindingAdapterPosition)
                }
                root.onCustomLongClick {
                    itemLongClickListener.invoke(subTopic, it, bindingAdapterPosition)
                }
                ibCheck.backgroundTintList = ColorStateList.valueOf(
                    if (subTopic?.isCorrectRecall == true) {
                        root.context.color(R.color.purple_500)
                    } else {
                        root.context.color(R.color.purple_50)
                    }
                )
                ibCheck.setImageIcon(
                    if (subTopic?.isCorrectRecall == true) {
                        Icon.createWithResource(root.context, R.drawable.round_check_white_24)
                    } else {
                        Icon.createWithResource(root.context, R.drawable.round_check_purple_24)
                    }
                )
                ibCheck.onSafeClick {
                    itemApproveUpdateClickListener.invoke(
                        subTopic?.copy(isCorrectRecall = subTopic.isCorrectRecall.not()),
                        bindingAdapterPosition
                    )
                }
            }
        }
    }
}