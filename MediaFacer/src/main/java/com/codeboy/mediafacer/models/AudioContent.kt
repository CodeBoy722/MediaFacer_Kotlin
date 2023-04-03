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
    var artUri: String = ""
    var musicSize: Long = 0
    var duration: Long = 0
    var musicId: Long = 0
    var musicUri: String = ""
    var filePath: String = ""
    var dateModified: Date = Date()

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun  getMediaMetadata(): androidx.media3.common.MediaMetadata {
        return androidx.media3.common.MediaMetadata.Builder()
                .setAlbumTitle(album)
                .setTitle(title.ifEmpty { name })
                .setArtist(artist)
                .setGenre(genre)
                .setIsBrowsable(true)
                .setIsPlayable(true)
                .setArtworkUri(Uri.parse(artUri))
                .build()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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