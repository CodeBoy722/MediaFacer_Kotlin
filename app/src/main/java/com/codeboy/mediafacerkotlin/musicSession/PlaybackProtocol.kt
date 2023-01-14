package com.codeboy.mediafacerkotlin.musicSession

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.models.AudioContent

object PlaybackProtocol: ViewModel() {

    //check if the music service is running
    var isMusicServiceRunning: Boolean = false

    var isMusicPlaying: Boolean = false

    private val _currentMusic: MutableLiveData<AudioContent> = MutableLiveData()
    val currentMusic: LiveData<AudioContent> = _currentMusic



}