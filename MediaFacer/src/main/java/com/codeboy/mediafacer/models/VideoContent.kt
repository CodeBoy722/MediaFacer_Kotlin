package com.codeboy.mediafacer.models

import android.media.MediaMetadata

class VideoContent() {

    var id: Int = 0
    lateinit var name: String
    lateinit var path: String
    var duration: Long = 0
    var size: Long = 0
    lateinit var videoUri: String
    lateinit var album: String
    lateinit var artist: String


    fun getMediaMetadata(): MediaMetadata {
        return MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, videoUri)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
            .putString(MediaMetadata.METADATA_KEY_TITLE, name)
            .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, videoUri)
            .putString(MediaMetadata.METADATA_KEY_ART_URI, videoUri)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
            .build()
    }

}