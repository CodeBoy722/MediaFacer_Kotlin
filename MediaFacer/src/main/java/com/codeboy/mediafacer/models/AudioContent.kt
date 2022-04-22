package com.codeboy.mediafacer.models

import android.media.MediaMetadata
import android.net.Uri

class AudioContent() {

    lateinit var name: String
    lateinit var title: String
    lateinit var filePath: String
    lateinit var artist: String
    lateinit var album: String
    lateinit var genre: String
    lateinit var composer: String
    lateinit var artUri: Uri
    var musicSize: Long = 0
    var duration: Long = 0
    var musicId: Long = 0
    lateinit var musicUri: String


    fun getMediaMetadata(): MediaMetadata {
        return MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID,musicUri)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_COMPOSER,composer)
            .putString(MediaMetadata.METADATA_KEY_GENRE,genre)
            .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, artUri.toString())
            .putString(MediaMetadata.METADATA_KEY_ART_URI, artUri.toString())
            .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
            .build()
    }

}