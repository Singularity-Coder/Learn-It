package com.singularitycoder.learnit

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.learnit.databinding.ListItemSubjectBinding
import com.singularitycoder.learnit.helpers.deviceHeight
import com.singularitycoder.learnit.helpers.deviceWidth
import com.singularitycoder.learnit.helpers.onCustomLongClick
import com.singularitycoder.learnit.helpers.onSafeClick

class DownloadsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var subjectList = emptyList<Subject?>()
    private var itemClickListener: (subject: Subject?, position: Int) -> Unit = { _, _ -> }
    private var itemLongClickListener: (subject: Subject?, view: View?, position: Int?) -> Unit = { _, _, _ -> }

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
            position: Int?
        ) -> Unit
    ) {
        itemLongClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemSubjectBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetJavaScriptEnabled")
        fun setData(subject: Subject?) {
            itemBinding.apply {
                itemBinding.root.animate().run {
                    withStartAction {
                        // make views visble or not
                    }
                    duration = 750
                    alpha(1.0F) // set default layout alpha to 0. So transition from alpha 0 to 1
                    withEndAction {}
                }
//                ivItemImage.layoutParams.height = deviceHeight() / 6
//                ivItemImage.layoutParams.width = deviceWidth() / 4
//                tvSource.text = "${subject?.extension}  •  ${subject?.pageCount} pages  •  ${subject?.size}"
                tvTitle.text = subject?.title
                root.onSafeClick {
                    itemClickListener.invoke(subject, bindingAdapterPosition)
                }
                root.onCustomLongClick {
                    itemLongClickListener.invoke(subject, it, bindingAdapterPosition)
                }
            }
        }
    }
}
