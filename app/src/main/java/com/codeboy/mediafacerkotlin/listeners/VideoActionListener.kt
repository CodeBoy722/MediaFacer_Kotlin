package com.codeboy.mediafacerkotlin.listeners

import com.codeboy.mediafacer.models.VideoContent

interface VideoActionListener {

    fun onVideoItemClicked(videoItem: VideoContent)

    fun onVideoItemLongClicked(videoItem: VideoContent)

}