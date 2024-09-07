package com.singularitycoder.learnit.subtopic.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.singularitycoder.learnit.helpers.DbTable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(tableName = DbTable.SUB_TOPIC)
@Parcelize
data class SubTopic(
    @PrimaryKey var id: String = "",
    var topicId: String = "",
    var title: String = "",
    var isCorrectRecall: Boolean = false,
) : Parcelable {

    @IgnoredOnParcel
    @Ignore
    var isDateShown: Boolean = false
}