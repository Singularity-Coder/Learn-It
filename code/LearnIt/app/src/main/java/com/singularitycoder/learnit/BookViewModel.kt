package com.singularitycoder.learnit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val bookDataDao: BookDataDao,
) : ViewModel() {

    fun addBookItem(subject: Subject) = viewModelScope.launch {
        bookDao.insert(subject)
    }

    fun getAllBookItemsFlow() = bookDao.getAllItemsStateFlow()

    suspend fun getAllBookItems() = bookDao.getAll()

    suspend fun hasBooks() = bookDao.hasItems()

    suspend fun getBookItemById(id: String) = bookDao.getItemById(id)

    suspend fun getBookDataItemById(id: String) = bookDataDao.getItemById(id)

//    suspend fun updateCompletedPageWithId(completedPage: Int, id: String) = bookDao.updateCompletedPageWithId(completedPage, id)

//    suspend fun getLast3BookItems() = bookDao.getLast3By()

    fun deleteBookItem(subject: Subject?) = viewModelScope.launch {
        bookDao.delete(subject ?: return@launch)
    }

    fun deleteBookDataItem(subject: Subject?) = viewModelScope.launch {
        bookDataDao.deleteBy(subject?.id ?: return@launch)
    }

    fun deleteAllBookItems() = viewModelScope.launch {
        bookDao.deleteAll()
    }

    fun deleteAllBookDataItems() = viewModelScope.launch {
        bookDataDao.deleteAll()
    }
}
