package com.singularitycoder.learnit

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.learnit.helpers.DbTable
import kotlinx.parcelize.Parcelize

@Entity(tableName = DbTable.TOPIC)
@Parcelize
data class Topic(
    @PrimaryKey var id: String,
    var path: String? = "",
    var text: String? = "",
    var pageCount: Int = 0
) : Parcelable