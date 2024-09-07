package com.singularitycoder.learnit.helpers.di

import android.content.Context
import androidx.room.Room
import com.singularitycoder.learnit.subject.dao.SubjectDao
import com.singularitycoder.learnit.topic.dao.TopicDao
import com.singularitycoder.learnit.helpers.Db
import com.singularitycoder.learnit.helpers.db.LearnItDatabase
import com.singularitycoder.learnit.subtopic.dao.SubTopicDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Singleton
    @Provides
    fun injectLearnItDatabase(@ApplicationContext context: Context): LearnItDatabase {
        return Room.databaseBuilder(context, LearnItDatabase::class.java, Db.LEARN_IT).build()
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
