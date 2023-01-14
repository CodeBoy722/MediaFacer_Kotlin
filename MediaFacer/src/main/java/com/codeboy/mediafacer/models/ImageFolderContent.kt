package com.codeboy.mediafacer.models

import java.io.Serializable

class ImageFolderContent(): Serializable {
    var folderPath: String = ""
    var folderName: String = ""
    var images: ArrayList<ImageContent> = ArrayList()
    var imageFolderSize = images.size
    var bucketId = 0
}