package com.singularitycoder.learnit.topic.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.learnit.helpers.DbTable
import com.singularitycoder.learnit.topic.model.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    /** room database will replace data based on primary key */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: Topic): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Topic>)

    @Query("SELECT EXISTS(SELECT * FROM ${DbTable.TOPIC} WHERE id = :id)")
    suspend fun isItemPresent(id: String): Boolean

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(topic: Topic)

    @Query("SELECT EXISTS(SELECT 1 FROM ${DbTable.TOPIC})")
    suspend fun hasItems(): Boolean

//    @Query("UPDATE ${Table.BOOK_DATA} SET link = :link WHERE website LIKE :website")
//    fun updateLinkWithWebsite(link: String?, website: String)

    @Query("SELECT * FROM ${DbTable.TOPIC} WHERE id LIKE :id LIMIT 1")
    suspend fun getItemById(id: Long): Topic

    @Query("SELECT * FROM ${DbTable.TOPIC}")
    fun getAllItemsLiveData(): LiveData<List<Topic>>

    @Query("SELECT * FROM ${DbTable.TOPIC}")
    fun getAllItemsStateFlow(): Flow<List<Topic>>

    @Query("SELECT * FROM ${DbTable.TOPIC} WHERE subjectId = :subjectId")
    fun getAllItemsBySubjectIdStateFlow(subjectId: Long): Flow<List<Topic>>

//    @Query("SELECT * FROM ${Table.BOOK_DATA} WHERE isSaved = 1")
//    fun getAllSavedItemsStateFlow(): Flow<List<BookData>>
//
//    @Query("SELECT * FROM ${Table.BOOK_DATA} WHERE website = :website")
//    fun getItemByWebsiteStateFlow(website: String?): Flow<BookData>

    @Query("SELECT * FROM ${DbTable.TOPIC}")
    suspend fun getAll(): List<Topic>


    @Delete
    suspend fun delete(topic: Topic)

    @Query("DELETE FROM ${DbTable.TOPIC} WHERE id = :id")
    suspend fun deleteBy(id: String)

    @Query("DELETE FROM ${DbTable.TOPIC}")
    suspend fun deleteAll()
}
