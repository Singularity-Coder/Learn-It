package com.singularitycoder.learnit.subject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.learnit.subject.dao.SubjectDao
import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.subtopic.dao.SubTopicDao
import com.singularitycoder.learnit.topic.dao.TopicDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val subjectDao: SubjectDao,
    private val topicDao: TopicDao,
    private val subTopicDao: SubTopicDao,
) : ViewModel() {

    fun addSubjectItem(subject: Subject) = viewModelScope.launch {
        subjectDao.insert(subject)
    }

    fun updateSubjectItem(subject: Subject) = viewModelScope.launch {
        subjectDao.update(subject)
    }

    fun getAllSubjectItemsFlow() = subjectDao.getAllItemsStateFlow()

    suspend fun getAllBookItems() = subjectDao.getAll()

    suspend fun hasBooks() = subjectDao.hasItems()

    suspend fun getBookItemById(id: String) = subjectDao.getItemById(id)

//    suspend fun getBookDataItemById(id: String) = topicDao.getItemById(id)

//    suspend fun updateCompletedPageWithId(completedPage: Int, id: String) = bookDao.updateCompletedPageWithId(completedPage, id)

//    suspend fun getLast3BookItems() = bookDao.getLast3By()

    fun deleteSubjectItem(subject: Subject?) = viewModelScope.launch {
        subTopicDao.deleteAll()
        topicDao.deleteAll()
        subjectDao.delete(subject ?: return@launch)
    }

//    fun deleteBookDataItem(subject: Subject?) = viewModelScope.launch {
//        topicDao.deleteBy(subject?.id ?: return@launch)
//    }

    fun deleteAllBookItems() = viewModelScope.launch {
        subjectDao.deleteAll()
    }
//
//    fun deleteAllBookDataItems() = viewModelScope.launch {
//        topicDao.deleteAll()
//    }
}
