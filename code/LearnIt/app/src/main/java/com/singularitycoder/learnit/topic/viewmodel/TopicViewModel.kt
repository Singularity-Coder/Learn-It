package com.singularitycoder.learnit.topic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.learnit.subtopic.dao.SubTopicDao
import com.singularitycoder.learnit.topic.dao.TopicDao
import com.singularitycoder.learnit.topic.model.Topic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicViewModel @Inject constructor(
    private val topicDao: TopicDao,
    private val subTopicDao: SubTopicDao,
) : ViewModel() {

    suspend fun addTopic(topic: Topic): Long = topicDao.insert(topic)

    suspend fun updateTopic(topic: Topic?) {
        topicDao.update(topic ?: return)
    }

    fun updateTopic2(topic: Topic?) = viewModelScope.launch {
        topicDao.update(topic ?: return@launch)
    }

    fun getAllTopicBySubjectIdItemsFlow(subjectId: Long?): Flow<List<Topic>> {
        subjectId ?: return emptyFlow()
        return topicDao.getAllItemsBySubjectIdStateFlow(subjectId)
    }

    fun deleteTopic(topic: Topic?) = viewModelScope.launch {
//        subTopicDao.deleteAllByTopicId(topic?.id ?: return@launch)
//        topicDao.delete(topic)
        topicDao.deleteTopicAndSubTopics(topic ?: return@launch)
    }

    suspend fun hasSubTopicsWith(topicId: Long?): Boolean {
        topicId ?: return false
        return subTopicDao.hasItemsWith(topicId)
    }

    suspend fun getTopicById(id: Long): Topic = topicDao.getItemById(id)

    fun deleteAllTopicsBy(subjectId: Long?) = viewModelScope.launch {
        subjectId ?: return@launch
        topicDao.deleteAllBySubjectId(subjectId)
    }

    suspend fun getAllBookItems() = topicDao.getAll()

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
