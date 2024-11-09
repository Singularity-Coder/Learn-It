package com.singularitycoder.learnit.subtopic.view

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.ListItemSubTopicBinding
import com.singularitycoder.learnit.helpers.color
import com.singularitycoder.learnit.helpers.onCustomLongClick
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.subtopic.model.SubTopic

class SubTopicsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var subTopicList = emptyList<SubTopic?>()
    var isVisibleHint = false
    private var itemClickListener: (subTopic: SubTopic?, position: Int) -> Unit = { _, _ -> }
    private var itemLongClickListener: (subTopic: SubTopic?, view: View?, position: Int) -> Unit = { _, _, _ -> }
    private var itemApproveUpdateClickListener: (subTopic: SubTopic?, position: Int) -> Unit = { _, _ -> }
    private var itemHintClickListener: (subTopic: SubTopic?, position: Int) -> Unit = { _, _ -> }

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

    fun setOnHintClickListener(listener: (subTopic: SubTopic?, position: Int) -> Unit) {
        itemHintClickListener = listener
    }

    fun checkMarkItem(
        recyclerView: RecyclerView,
        adapterPosition: Int,
        isChecked: Boolean
    ) {
        subTopicList.get(adapterPosition)?.isCorrectRecall = isChecked
        val viewHolder = (recyclerView.findViewHolderForAdapterPosition(adapterPosition) as? ThisViewHolder) ?: return
        viewHolder.getView().ibCheck.setImageIcon(
            if (isChecked) {
                Icon.createWithResource(viewHolder.getView().root.context, R.drawable.round_check_white_24)
            } else {
                Icon.createWithResource(viewHolder.getView().root.context, R.drawable.round_check_purple_24)
            }
        )
        viewHolder.getView().ibCheck.backgroundTintList = ColorStateList.valueOf(
            if (isChecked) {
                viewHolder.getView().root.context.color(R.color.purple_500)
            } else {
                viewHolder.getView().root.context.color(R.color.purple_50)
            }
        )
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemSubTopicBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun getView() = itemBinding

        fun setData(subTopic: SubTopic?) {
            itemBinding.apply {
                tvTitle.text = subTopic?.title
                btnHint.isVisible = isVisibleHint
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
                btnHint.onSafeClick {
                    itemHintClickListener.invoke(
                        subTopic?.copy(isCorrectRecall = subTopic.isCorrectRecall.not()),
                        bindingAdapterPosition
                    )
                }
            }
        }
    }
}