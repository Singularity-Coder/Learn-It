package com.singularitycoder.learnit.subject.view

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.learnit.databinding.ListItemSubjectBinding
import com.singularitycoder.learnit.helpers.constants.DELAY_500_MILLIS
import com.singularitycoder.learnit.helpers.hideKeyboard
import com.singularitycoder.learnit.helpers.onCustomLongClick
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.showKeyboard
import com.singularitycoder.learnit.subject.model.Subject

class SubjectsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var subjectList = emptyList<Subject?>()
    private var itemClickListener: (subject: Subject?, position: Int) -> Unit = { _, _ -> }
    private var itemLongClickListener: (subject: Subject?, view: View?, position: Int) -> Unit = { _, _, _ -> }
    private var itemApproveUpdateClickListener: (subject: Subject?, position: Int) -> Unit = { _, _ -> }
    private var handler = Handler(Looper.getMainLooper())
    private var runnable = Runnable {  }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(subjectList[position])
    }

    override fun getItemCount(): Int = subjectList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (subject: Subject?, position: Int) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(
        listener: (
            subject: Subject?,
            view: View?,
            position: Int
        ) -> Unit
    ) {
        itemLongClickListener = listener
    }

    fun setOnApproveUpdateClickListener(listener: (subject: Subject?, position: Int) -> Unit) {
        itemApproveUpdateClickListener = listener
    }

    fun showEditView(
        recyclerView: RecyclerView,
        adapterPosition: Int,
    ) {
        val viewHolder = (recyclerView.findViewHolderForAdapterPosition(adapterPosition) as? ThisViewHolder) ?: return
        viewHolder.getRootView().apply {
            clTitle.isVisible = false
            layoutUpdateItem.apply {
                root.isVisible = true
                etUpdateItem.setText(subjectList[adapterPosition]?.title)
                runnable = Runnable {
                    etUpdateItem.showKeyboard()
                }
                handler.postDelayed(runnable, DELAY_500_MILLIS)
            }
        }
    }

    fun removeHandlerCallback() {
        handler.removeCallbacks(runnable)
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemSubjectBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun getRootView() = itemBinding

        fun setData(subject: Subject?) {
            itemBinding.apply {
                tvTitle.text = subject?.title
                root.onSafeClick {
                    itemClickListener.invoke(subject, bindingAdapterPosition)
                }
                root.onCustomLongClick {
                    itemLongClickListener.invoke(subject, it, bindingAdapterPosition)
                }
                layoutUpdateItem.ibCancelUpdate.onSafeClick {
                    layoutUpdateItem.root.isVisible = false
                    clTitle.isVisible = true
                    layoutUpdateItem.etUpdateItem.hideKeyboard()
                }
                layoutUpdateItem.ibApproveUpdate.onSafeClick {
                    itemApproveUpdateClickListener.invoke(
                        subject?.copy(title = layoutUpdateItem.etUpdateItem.text.toString()),
                        bindingAdapterPosition
                    )
                    layoutUpdateItem.root.isVisible = false
                    clTitle.isVisible = true
                    layoutUpdateItem.etUpdateItem.hideKeyboard()
                }
            }
        }
    }
}