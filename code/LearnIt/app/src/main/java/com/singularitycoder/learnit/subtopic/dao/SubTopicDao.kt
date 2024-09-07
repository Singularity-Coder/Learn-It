package com.singularitycoder.learnit.subtopic.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.learnit.helpers.DbTable
import com.singularitycoder.learnit.subtopic.model.SubTopic
import com.singularitycoder.learnit.topic.model.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface SubTopicDao {

    /** room database will replace data based on primary key */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subTopic: SubTopic)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<SubTopic>)

    @Query("SELECT EXISTS(SELECT * FROM ${DbTable.SUB_TOPIC} WHERE id = :id)")
    suspend fun isItemPresent(id: String): Boolean

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(subTopic: SubTopic)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(list: List<SubTopic>)

    @Query("SELECT EXISTS(SELECT 1 FROM ${DbTable.SUB_TOPIC} WHERE topicId = :topicId)")
    suspend fun hasItemsWith(topicId: Long): Boolean

//    @Query("UPDATE ${Table.BOOK_DATA} SET link = :link WHERE website LIKE :website")
//    fun updateLinkWithWebsite(link: String?, website: String)

    @Query("SELECT * FROM ${DbTable.SUB_TOPIC} WHERE id LIKE :id LIMIT 1")
    suspend fun getItemById(id: String): SubTopic

    @Query("SELECT * FROM ${DbTable.SUB_TOPIC}")
    fun getAllItemsLiveData(): LiveData<List<SubTopic>>

    @Query("SELECT * FROM ${DbTable.SUB_TOPIC}")
    fun getAllItemsStateFlow(): Flow<List<SubTopic>>

    @Query("SELECT * FROM ${DbTable.SUB_TOPIC} WHERE topicId = :topicId")
    fun getAllItemsByTopicIdStateFlow(topicId: Long): Flow<List<SubTopic>>

//    @Query("SELECT * FROM ${Table.BOOK_DATA} WHERE website = :website")
//    fun getAllItemsByWebsiteStateFlow(website: String?): Flow<List<BookData>>
//
//    @Query("SELECT * FROM ${Table.BOOK_DATA} WHERE isSaved = 1")
//    fun getAllSavedItemsStateFlow(): Flow<List<BookData>>
//
//    @Query("SELECT * FROM ${Table.BOOK_DATA} WHERE website = :website")
//    fun getItemByWebsiteStateFlow(website: String?): Flow<BookData>

    @Query("SELECT * FROM ${DbTable.SUB_TOPIC}")
    suspend fun getAll(): List<SubTopic>


    @Delete
    suspend fun delete(subTopic: SubTopic)

    @Query("DELETE FROM ${DbTable.SUB_TOPIC} WHERE id = :id")
    suspend fun deleteBy(id: String)

    @Query("DELETE FROM ${DbTable.SUB_TOPIC}")
    suspend fun deleteAll()

    @Query("DELETE FROM ${DbTable.SUB_TOPIC} WHERE topicId = :topicId")
    suspend fun deleteAllByTopicId(topicId: Long)
}
