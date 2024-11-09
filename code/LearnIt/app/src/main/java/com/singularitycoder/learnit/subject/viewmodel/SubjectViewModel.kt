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

    fun updateAllSubjects(list: List<Subject>) = viewModelScope.launch {
        subjectDao.updateAll(list)
    }

    fun getAllSubjectItemsFlow() = subjectDao.getAllItemsStateFlow()

    suspend fun getAllSubjects() = subjectDao.getAll()

    suspend fun hasSubjects() = subjectDao.hasItems()

    suspend fun getSubjectById(id: Long) = subjectDao.getItemById(id)

//    suspend fun getBookDataItemById(id: String) = topicDao.getItemById(id)

//    suspend fun updateCompletedPageWithId(completedPage: Int, id: String) = bookDao.updateCompletedPageWithId(completedPage, id)

//    suspend fun getLast3BookItems() = bookDao.getLast3By()

    fun deleteSubjectItem(subject: Subject?) = viewModelScope.launch {
//        subTopicDao.deleteAllBySubjectId(subject?.id ?: return@launch)
//        topicDao.deleteAllBySubjectId(subject.id)
//        subjectDao.delete(subject)
        subjectDao.deleteSubjectAndTopicAndSubTopics(subject ?: return@launch)
    }

//    fun deleteBookDataItem(subject: Subject?) = viewModelScope.launch {
//        topicDao.deleteBy(subject?.id ?: return@launch)
//    }

    fun deleteAllSubjects() = viewModelScope.launch {
//        subTopicDao.deleteAll()
//        topicDao.deleteAll()
//        subjectDao.deleteAll()
        subjectDao.deleteAllSubjectsTopicsSubTopics()
    }
//
//    fun deleteAllBookDataItems() = viewModelScope.launch {
//        topicDao.deleteAll()
//    }
}
