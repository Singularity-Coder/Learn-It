package com.singularitycoder.learnit.permissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.learnit.helpers.AppPreferences
import com.singularitycoder.learnit.helpers.constants.PERMISSION_LIST
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor() : ViewModel() {

    private val _permissionsList = MutableLiveData<List<Permission>>()
    val permissionsList: LiveData<List<Permission>> = _permissionsList

    init {
        loadPermissions()
    }

    private fun loadPermissions() = viewModelScope.launch {
        if (AppPreferences.getInstance().hasNotificationPermission) {
            val list = PERMISSION_LIST.toMutableList()
            _permissionsList.postValue(
                list.apply { removeAt(0) }
            )
        } else {
            _permissionsList.postValue(PERMISSION_LIST)
        }
    }

    fun updateList(position: Int) = viewModelScope.launch {
        val updatedList = permissionsList.value?.toMutableList()?.apply {
            removeAt(position)
        } ?: emptyList()
        _permissionsList.postValue(updatedList.toList())
    }
}
