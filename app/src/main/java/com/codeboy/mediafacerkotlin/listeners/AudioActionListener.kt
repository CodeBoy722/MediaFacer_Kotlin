package com.codeboy.mediafacerkotlin.listeners

import com.codeboy.mediafacer.models.AudioContent

interface AudioActionListener {

    fun onAudioItemClicked(audio: AudioContent, position: Int)
    fun onAudioItemLongClicked(audio: AudioContent, position: Int)
}