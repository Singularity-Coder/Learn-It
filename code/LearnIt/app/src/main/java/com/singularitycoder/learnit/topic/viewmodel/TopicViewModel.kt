package com.singularitycoder.learnit.topic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.learnit.topic.dao.TopicDao
import com.singularitycoder.learnit.topic.model.Topic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicViewModel @Inject constructor(
    private val topicDao: TopicDao,
) : ViewModel() {

    suspend fun addTopicItem(topic: Topic): Long = topicDao.insert(topic)

    fun getAllTopicItemsFlow() = topicDao.getAllItemsStateFlow()

    suspend fun getAllBookItems() = topicDao.getAll()

//    suspend fun hasBooks() = topicDao.hasItems()

    suspend fun getBookItemById(id: String) = topicDao.getItemById(id)

    suspend fun getBookDataItemById(id: String) = topicDao.getItemById(id)

//    suspend fun updateCompletedPageWithId(completedPage: Int, id: String) = bookDao.updateCompletedPageWithId(completedPage, id)

//    suspend fun getLast3BookItems() = bookDao.getLast3By()

//    fun deleteBookItem(subject: Subject?) = viewModelScope.launch {
//        topicDao.delete(subject ?: return@launch)
//    }

//    fun deleteBookDataItem(subject: Subject?) = viewModelScope.launch {
//        topicDao.deleteBy(subject?.id ?: return@launch)
//    }

    fun deleteAllBookItems() = viewModelScope.launch {
        topicDao.deleteAll()
    }

    fun deleteAllBookDataItems() = viewModelScope.launch {
        topicDao.deleteAll()
    }
}
