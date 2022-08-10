package com.codeboy.mediafacerkotlin.listeners

import com.codeboy.mediafacer.models.VideoContent

interface VideoActionListener {

    fun onVideoItemClicked(playPosition: Int, mediaItemList: ArrayList<VideoContent>)

    fun onVideoItemLongClicked(videoItem: VideoContent)

}