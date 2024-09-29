package com.singularitycoder.learnit.permissions

import android.Manifest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.learnit.helpers.AndroidVersions
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
        val hasNotifPermission = AndroidVersions.isTiramisu() && AppPreferences.getInstance().hasNotificationPermission
        if (hasNotifPermission) {
            val list = PERMISSION_LIST.toMutableList()
            val itemToRemove = list.find { it.permissionName == Manifest.permission.POST_NOTIFICATIONS }
            list.remove(itemToRemove)
            _permissionsList.postValue(list)
        } else {
            _permissionsList.postValue(PERMISSION_LIST)
        }
    }

    fun removePermission(permissionName: String) = viewModelScope.launch {
        val updatedList = permissionsList.value?.toMutableList()
        val itemToRemove = permissionsList.value?.find { it.permissionName == permissionName }
        updatedList?.remove(itemToRemove)
        _permissionsList.postValue(updatedList?.toList())
    }
}
