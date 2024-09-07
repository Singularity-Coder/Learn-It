package com.singularitycoder.learnit.subject.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.learnit.helpers.DbTable
import kotlinx.parcelize.Parcelize

@Entity(tableName = DbTable.SUBJECT)
@Parcelize
data class Subject(
     @PrimaryKey(autoGenerate = true) var id: Long = 0L,
     var title: String = ""
) : Parcelable