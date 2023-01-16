package com.codeboy.mediafacer.models

import android.media.MediaMetadata
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import java.io.Serializable
import java.util.*

class AudioContent(): Serializable {

    var name: String = ""
    var title: String = ""
    var artist: String = ""
    var album: String = ""
    var genre: String = ""
    var artUri: Uri = Uri.EMPTY
    var musicSize: Long = 0
    var duration: Long = 0
    var musicId: Long = 0
    var musicUri: String = ""
    var filePath: String = ""
    var dateModified: Date = Date()


    fun getMediaMetadata(): MediaMetadata {
        return MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID,musicUri)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_GENRE,genre)
            .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, artUri.toString())
            .putString(MediaMetadata.METADATA_KEY_ART_URI, artUri.toString())
            .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
            .build()
    }

    fun getMediaMetaDataCompat(): MediaMetadataCompat{
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID,musicUri)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_GENRE,genre)
            .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, artUri.toString())
            .putString(MediaMetadata.METADATA_KEY_ART_URI, artUri.toString())
            .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
            .build()
    }

}