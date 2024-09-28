package com.singularitycoder.learnit.permissions

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.ListItemPermissionBinding
import com.singularitycoder.learnit.helpers.color
import com.singularitycoder.learnit.helpers.onSafeClick

class PermissionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var permissionList = emptyList<Permission>()
    private var itemGrantClickListener: (permission: Permission, position: Int) -> Unit = { _, _ -> }
    private var itemLaterClickListener: (permission: Permission, position: Int) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemPermissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(permissionList[position])
    }

    override fun getItemCount(): Int = permissionList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnGrantClickListener(listener: (permission: Permission, position: Int) -> Unit) {
        itemGrantClickListener = listener
    }

    fun setOnLaterClickListener(listener: (permission: Permission, position: Int) -> Unit) {
        itemLaterClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemPermissionBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun setData(permission: Permission) {
            itemBinding.apply {
                tvTitle.text = root.context.getString(permission.title)
                tvSubtitle.text = root.context.getString(permission.subtitle)
                tvSubtitle2.text = SpannableStringBuilder()
//                    .bold { append("Requirement: ") }
//                    .append("Requirement: ")
                    .color(
                        root.context.color(R.color.purple_300),
                        { append(root.context.getString(permission.requirementType)) }
                    )
                btnLater.isVisible = permission.requirementType != R.string.essential
                btnGrant.onSafeClick {
                    itemGrantClickListener.invoke(permission, bindingAdapterPosition)
                }
                btnLater.onSafeClick {
                    itemLaterClickListener.invoke(permission, bindingAdapterPosition)
                }
            }
        }
    }
}