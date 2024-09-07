package com.singularitycoder.learnit.helpers.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.subject.dao.SubjectDao
import com.singularitycoder.learnit.subtopic.dao.SubTopicDao
import com.singularitycoder.learnit.topic.model.Topic
import com.singularitycoder.learnit.topic.dao.TopicDao
import com.singularitycoder.learnit.subtopic.model.SubTopic

@Database(
    entities = [
        Subject::class,
        Topic::class,
        SubTopic::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    IntListConverter::class,
    IntHashMapConverter::class
)
abstract class LearnItDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun subTopicDao(): SubTopicDao
}

