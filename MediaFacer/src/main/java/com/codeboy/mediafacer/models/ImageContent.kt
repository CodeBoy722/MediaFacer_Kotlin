package com.codeboy.mediafacer.models

import java.io.Serializable
import java.util.*

class ImageContent(): Serializable {

    var name: String = ""
    var bucketName: String = ""
    var size: Long = 0
    var imageUri: String = ""
    var imageId = 0
    var filePath: String = ""
    var dateModified: Date = Date()

}