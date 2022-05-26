package com.codeboy.mediafacerkotlin.listeners

import com.codeboy.mediafacer.models.AudioContent

interface AudioMediaListener {

    fun onAudioMediaClicked(mediaType: String, title: String, audios: ArrayList<AudioContent>)

}