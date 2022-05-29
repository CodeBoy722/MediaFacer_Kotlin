package com.codeboy.mediafacerkotlin.listeners

import com.codeboy.mediafacer.models.VideoContent

interface VideoContainerActionListener {

    fun onVideoFolderClicked(mediaType: String, title: String, videos: ArrayList<VideoContent>)

}