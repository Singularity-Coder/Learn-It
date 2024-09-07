package com.singularitycoder.learnit.topic.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.learnit.helpers.DbTable
import kotlinx.parcelize.Parcelize

@Entity(tableName = DbTable.TOPIC)
@Parcelize
data class Topic(
    @PrimaryKey var id: String = "",
    var title: String = "",
    var subjectId: Long = 0L,
    var studyMaterial: String = "",
    var dateStarted: Long = 0L,
    var nextSessionDate: Long = 0L,
    var finishedSessions: Int = 0, // 1 to 8
) : Parcelable