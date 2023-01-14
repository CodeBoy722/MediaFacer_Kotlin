package com.codeboy.mediafacer.models

import java.io.Serializable

class VideoFolderContent(): Serializable {
    var videos: ArrayList<VideoContent> = ArrayList()
    var videoFolderSize = videos.size
    var folderName: String = ""
    var folderPath: String = ""
    var bucketId = 0
}