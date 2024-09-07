package com.singularitycoder.learnit.subtopic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.learnit.subtopic.dao.SubTopicDao
import com.singularitycoder.learnit.subtopic.model.SubTopic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubTopicViewModel @Inject constructor(
    private val subTopicDao: SubTopicDao,
) : ViewModel() {

    fun addSubTopicItem(subTopic: SubTopic) = viewModelScope.launch {
        subTopicDao.insert(subTopic)
    }

    fun addAllSubTopicItem(list: List<SubTopic>) = viewModelScope.launch {
        subTopicDao.insertAll(list)
    }

    fun getAllSubTopicItemsFlow() = subTopicDao.getAllItemsStateFlow()

    suspend fun getAllBookItems() = subTopicDao.getAll()

//    suspend fun hasBooks() = subTopicDao.hasItems()

    suspend fun getBookItemById(id: String) = subTopicDao.getItemById(id)

    suspend fun getBookDataItemById(id: String) = subTopicDao.getItemById(id)

//    suspend fun updateCompletedPageWithId(completedPage: Int, id: String) = bookDao.updateCompletedPageWithId(completedPage, id)

//    suspend fun getLast3BookItems() = bookDao.getLast3By()

//    fun deleteBookItem(subject: Subject?) = viewModelScope.launch {
//        subTopicDao.delete(subject ?: return@launch)
//    }

//    fun deleteBookDataItem(subject: Subject?) = viewModelScope.launch {
//        subTopicDao.deleteBy(subject?.id ?: return@launch)
//    }

    fun deleteAllBookItems() = viewModelScope.launch {
        subTopicDao.deleteAll()
    }

    fun deleteAllBookDataItems() = viewModelScope.launch {
        subTopicDao.deleteAll()
    }
}
