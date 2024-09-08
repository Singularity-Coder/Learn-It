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
import com.singularitycoder.learnit.helpers.currentTimeMillis
import com.singularitycoder.learnit.helpers.onCustomLongClick
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.showKeyboard
import com.singularitycoder.learnit.helpers.toDateTime
import com.singularitycoder.learnit.subject.view.SubjectsAdapter.ThisViewHolder
import com.singularitycoder.learnit.topic.model.Topic
import java.util.concurrent.TimeUnit
import kotlin.math.floor

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

    fun reset(
        recyclerView: RecyclerView,
        adapterPosition: Int,
    ) {
        val viewHolder = (recyclerView.findViewHolderForAdapterPosition(adapterPosition) as? ThisViewHolder) ?: return
        viewHolder.resetRepetitionDayViews()
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemTopicBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun setData(topic: Topic?) {
            itemBinding.apply {
                tvTitle.text = topic?.title
                tvStudyMaterial.text = "Study Material: ${topic?.studyMaterial}"
                val timeLeftMillis = (topic?.nextSessionDate ?: 0L) - currentTimeMillis
                val totalMinutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillis)
                val daysLeft = totalMinutesLeft / (24 * 60)
                val hoursLeft = (totalMinutesLeft % (24 * 60)) / 60
                val minutesLeft = (totalMinutesLeft % (24 * 60)) % 60
                tvNextSession.text = "Next Session: ${daysLeft}d : ${hoursLeft}h : ${minutesLeft}m"
                if (topic?.dateStarted != 0L) {
                    setStartedState(topic)
                }
                root.onSafeClick {
                    itemClickListener.invoke(topic, bindingAdapterPosition)
                }
                root.onCustomLongClick {
                    itemLongClickListener.invoke(topic, it, bindingAdapterPosition)
                }
                btnStart.onSafeClick {
                    setStartedState(topic)
                    startClickListener.invoke(topic, bindingAdapterPosition)
                }
            }
        }

        private fun setStartedState(topic: Topic?) {
            itemBinding.apply {
                btnStart.isVisible = false
                tvNextSession.isVisible = btnStart.isVisible.not()
                clRepetitionDays.isVisible = btnStart.isVisible.not()
                val dayViewList = listOf(tvDay1, tvDay2, tvDay3, tvDay4, tvDay5)
                (1..(topic?.finishedSessions ?: 0)).forEachIndexed { index, value ->
                    dayViewList[index].backgroundTintList = ColorStateList.valueOf(root.context.color(R.color.purple_500))
                    dayViewList[index].setTextColor(root.context.color(R.color.white))
                }
            }
        }

        fun resetRepetitionDayViews() {
            itemBinding.apply {
                btnStart.isVisible = true
                tvNextSession.isVisible = btnStart.isVisible.not()
                clRepetitionDays.isVisible = btnStart.isVisible.not()
                listOf(tvDay1, tvDay2, tvDay3, tvDay4, tvDay5).forEachIndexed { index, textView ->
                    textView.backgroundTintList = ColorStateList.valueOf(root.context.color(R.color.purple_50))
                    textView.setTextColor(root.context.color(R.color.purple_500))
                }
            }
        }
    }
}