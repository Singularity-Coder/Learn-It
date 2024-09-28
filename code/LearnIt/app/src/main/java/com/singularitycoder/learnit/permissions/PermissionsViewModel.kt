package com.singularitycoder.learnit.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.learnit.helpers.constants.PERMISSION_LIST
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor() : ViewModel() {

    private val _permissionsList: MutableStateFlow<List<Permission>> = MutableStateFlow(emptyList())
    val permissionsList: StateFlow<List<Permission>> = _permissionsList

    init {
        loadPermissions()
    }

    private fun loadPermissions() = viewModelScope.launch {
        _permissionsList.emit(PERMISSION_LIST)
    }
}
