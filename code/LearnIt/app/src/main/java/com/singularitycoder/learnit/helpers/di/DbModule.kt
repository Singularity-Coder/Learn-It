package com.singularitycoder.learnit.helpers.di

import android.content.Context
import androidx.room.Room
import com.singularitycoder.learnit.BookDao
import com.singularitycoder.learnit.BookDataDao
import com.singularitycoder.learnit.helpers.Db
import com.singularitycoder.learnit.helpers.db.LearnItDatabase
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
        return Room.databaseBuilder(context, LearnItDatabase::class.java, Db.PLAY_BOOKS).build()
    }

    @Singleton
    @Provides
    fun injectBookDao(db: LearnItDatabase): BookDao = db.bookDao()

    @Singleton
    @Provides
    fun injectBookDataDao(db: LearnItDatabase): BookDataDao = db.bookDataDao()
}
