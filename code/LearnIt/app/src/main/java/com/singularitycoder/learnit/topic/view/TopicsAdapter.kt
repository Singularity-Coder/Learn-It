package com.singularitycoder.learnit.topic.view

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.ListItemTopicBinding
import com.singularitycoder.learnit.helpers.color
import com.singularitycoder.learnit.helpers.onCustomLongClick
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.topic.model.Topic

class TopicsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var topicList = emptyList<Topic?>()
    private var itemClickListener: (topic: Topic?, position: Int) -> Unit = { _, _ -> }
    private var itemLongClickListener: (topic: Topic?, view: View?, position: Int) -> Unit = { _, _, _ -> }
    private var startClickListener: (topic: Topic?, position: Int) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(topicList[position])
    }

    override fun getItemCount(): Int = topicList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (topic: Topic?, position: Int) -> Unit) {
        itemClickListener = listener
    }

    fun setOnStartClickListener(listener: (topic: Topic?, position: Int) -> Unit) {
        startClickListener = listener
    }

    fun setOnItemLongClickListener(
        listener: (
            topic: Topic?,
            view: View?,
            position: Int
        ) -> Unit
    ) {
        itemLongClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemTopicBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun setData(topic: Topic?) {
            itemBinding.apply {
                tvTitle.text = topic?.title
                tvStudyMaterial.text = "Study Material: ${topic?.studyMaterial}"
                root.onSafeClick {
                    itemClickListener.invoke(topic, bindingAdapterPosition)
                }
                root.onCustomLongClick {
                    itemLongClickListener.invoke(topic, it, bindingAdapterPosition)
                }
                btnStart.onSafeClick {
                    btnStart.isVisible = false
                    clRepetitionDays.isVisible = true
                    tvDay1.backgroundTintList = ColorStateList.valueOf(root.context.color(R.color.purple_500))
                    tvDay1.setTextColor(root.context.color(R.color.white))
                    startClickListener.invoke(topic, bindingAdapterPosition)
                }
            }
        }
    }
}