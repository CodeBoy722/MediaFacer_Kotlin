package com.codeboy.mediafacerkotlin.listeners

import com.codeboy.mediafacer.models.AudioContent

interface AudioContainerActionListener {

    fun onAudioContainerClicked(mediaType: String, title: String, audios: ArrayList<AudioContent>)

}