package com.singularitycoder.learnit.permissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor() : ViewModel() {

    private val _permissionCount = MutableLiveData<Int>()
    val permissionCount: LiveData<Int> = _permissionCount

    fun setPermissionCount(count: Int) = viewModelScope.launch {
        _permissionCount.postValue(count)
    }
}
