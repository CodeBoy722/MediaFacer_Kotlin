package com.codeboy.mediafacer.models

import java.io.Serializable

class AudioBucketContent(): Serializable {

    var bucketName: String = ""
    var bucketPath: String = ""
    var bucketId: String = ""
    var audios = ArrayList<AudioContent>()

}