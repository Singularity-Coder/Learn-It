package com.singularitycoder.learnit.subtopic.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.singularitycoder.learnit.helpers.constants.DbTable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(tableName = DbTable.SUB_TOPIC)
@Parcelize
data class SubTopic(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    var topicId: Long = 0L,
    var subjectId: Long,
    var title: String = "",
    var isCorrectRecall: Boolean = false
) : Parcelable {

    @IgnoredOnParcel
    @Ignore
    var isDateShown: Boolean = false
}