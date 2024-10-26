package com.singularitycoder.learnit.subject.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlarmSoundPickerViewModel @Inject constructor() : ViewModel() {

    private val defaultUri = MutableLiveData<Uri?>()
    private val existingUri = MutableLiveData<Uri?>()
    private val pickedUri = MutableLiveData<Uri?>()

    private val showDefault = MutableLiveData<Boolean>()
    private val showSilent = MutableLiveData<Boolean>()
    private val wasExistingUriGiven = MutableLiveData<Boolean>()
    private val playTone = MutableLiveData(true)

    private val title = MutableLiveData<CharSequence>()

    private val toneUriList = MutableLiveData(ArrayList<Uri>())
    private val toneNameList = MutableLiveData(ArrayList<String>())
    private val toneIdList = MutableLiveData(ArrayList<Int>())

    private val isInitialised = MutableLiveData(false)
    private val requestedPermissions = MutableLiveData(false)

    fun werePermsRequested(): Boolean {
        return java.lang.Boolean.TRUE == requestedPermissions.value
    }

    fun setWerePermsRequested(value: Boolean) {
        requestedPermissions.value = value
    }

    /**
     * @noinspection BooleanMethodIsAlwaysInverted
     */
    fun getIsInitialised(): Boolean {
        return java.lang.Boolean.TRUE == isInitialised.value
    }

    fun setIsInitialised(isInitialised: Boolean) {
        this.isInitialised.value = isInitialised
    }

    fun getDefaultUri(): Uri? {
        return defaultUri.value
    }

    fun setDefaultUri(defaultUri: Uri?) {
        this.defaultUri.value = defaultUri
    }

    fun getExistingUri(): Uri? {
        return existingUri.value
    }

    fun setExistingUri(existingUri: Uri?) {
        this.existingUri.value = existingUri
    }

    fun getPickedUri(): Uri? {
        return pickedUri.value
    }

    fun setPickedUri(pickedUri: Uri?) {
        this.pickedUri.value = pickedUri
    }

    fun getShowDefault(): Boolean {
        return java.lang.Boolean.TRUE == showDefault.value
    }

    fun setShowDefault(showDefault: Boolean) {
        this.showDefault.value = showDefault
    }

    fun getShowSilent(): Boolean {
        return java.lang.Boolean.TRUE == showSilent.value
    }

    fun setShowSilent(showSilent: Boolean) {
        this.showSilent.value = showSilent
    }

    fun getWasExistingUriGiven(): Boolean {
        return java.lang.Boolean.TRUE == wasExistingUriGiven.value
    }

    fun setWasExistingUriGiven(wasExistingUriGiven: Boolean) {
        this.wasExistingUriGiven.value = wasExistingUriGiven
    }

    fun getPlayTone(): Boolean {
        return java.lang.Boolean.TRUE == playTone.value
    }

    fun setPlayTone(playTone: Boolean) {
        this.playTone.value = playTone
    }

    fun getTitle(): CharSequence {
        return title.value ?: ""
    }

    fun setTitle(title: CharSequence) {
        this.title.value = title
    }

    fun getToneUriList(): ArrayList<Uri> {
        return toneUriList.value ?: ArrayList()
    }

    fun getToneNameList(): ArrayList<String> {
        return toneNameList.value ?: ArrayList()
    }

    fun getToneIdList(): ArrayList<Int> {
        return toneIdList.value ?: ArrayList()
    }


}
