package com.singularitycoder.learnit.helpers.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.singularitycoder.learnit.helpers.constants.DEFAULT_SUBJECTS
import com.singularitycoder.learnit.helpers.constants.DEFAULT_SUB_TOPICS
import com.singularitycoder.learnit.helpers.constants.DEFAULT_TOPICS
import com.singularitycoder.learnit.subject.dao.SubjectDao
import com.singularitycoder.learnit.topic.dao.TopicDao
import com.singularitycoder.learnit.helpers.constants.Db
import com.singularitycoder.learnit.helpers.db.LearnItDatabase
import com.singularitycoder.learnit.subtopic.dao.SubTopicDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Singleton
    @Provides
    fun injectLearnItDatabase(@ApplicationContext context: Context): LearnItDatabase {
        val roomDbBuilder: RoomDatabase.Builder<LearnItDatabase> = Room.databaseBuilder(context, LearnItDatabase::class.java, Db.LEARN_IT)
        var roomDb: LearnItDatabase? = null
        roomDbBuilder.addCallback(object : RoomDatabase.Callback() {
            // prepopulate the database after onCreate was called - https://medium.com/androiddevelopers/7-pro-tips-for-room-fbadea4bfbd1
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(IO).launch {
                    roomDb?.subjectDao()?.insertAll(DEFAULT_SUBJECTS)
                    roomDb?.topicDao()?.insertAll(DEFAULT_TOPICS)
                    roomDb?.subTopicDao()?.insertAll(DEFAULT_SUB_TOPICS)
                }
            }
        })
        roomDb = roomDbBuilder.build()
        return roomDb
    }

    @Singleton
    @Provides
    fun injectSubjectDao(db: LearnItDatabase): SubjectDao = db.subjectDao()

    @Singleton
    @Provides
    fun injectTopicDao(db: LearnItDatabase): TopicDao = db.topicDao()

    @Singleton
    @Provides
    fun injectSubTopicDao(db: LearnItDatabase): SubTopicDao = db.subTopicDao()
}
