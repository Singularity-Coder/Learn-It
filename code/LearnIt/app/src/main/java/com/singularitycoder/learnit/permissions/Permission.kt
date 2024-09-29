package com.singularitycoder.learnit.permissions

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.learnit.helpers.constants.DbTable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Permission(
     var permissionName: String,
     @StringRes var title: Int,
     @StringRes var subtitle: Int,
     @StringRes var requirementType: Int
) : Parcelable