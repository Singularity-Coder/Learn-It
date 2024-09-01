package com.singularitycoder.learnit.helpers.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.learnit.Book
import com.singularitycoder.learnit.BookDao
import com.singularitycoder.learnit.BookData
import com.singularitycoder.learnit.BookDataDao

@Database(
    entities = [
        Book::class,
        BookData::class,
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
    abstract fun bookDao(): BookDao
    abstract fun bookDataDao(): BookDataDao
}

