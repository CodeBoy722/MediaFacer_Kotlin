package com.codeboy.mediafacer.models
import android.media.MediaMetadata
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaMetadataCompat
import java.io.Serializable
import java.util.*

class AudioContent(): Serializable, Parcelable {

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
    fun getMediaMetadata(): androidx.media3.common.MediaMetadata {
        return androidx.media3.common.MediaMetadata.Builder()
            .setAlbumTitle(album)
            .setTitle(title.ifEmpty { name })
            .setArtist(artist)
            .setGenre(genre)
            .setIsBrowsable(true)
            .setIsPlayable(true)
            .setArtworkUri(Uri.parse(artUri))
            .setMediaType(androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC)
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

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()!!
        title = parcel.readString()!!
        artist = parcel.readString()!!
        album = parcel.readString()!!
        genre = parcel.readString()!!
        artUri = parcel.readString()!!
        musicSize = parcel.readLong()
        duration = parcel.readLong()
        musicId = parcel.readLong()
        musicUri = parcel.readString()!!
        filePath = parcel.readString()!!
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeString(album)
        parcel.writeString(genre)
        parcel.writeString(artUri)
        parcel.writeLong(musicSize)
        parcel.writeLong(duration)
        parcel.writeLong(musicId)
        parcel.writeString(musicUri)
        parcel.writeString(filePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioContent> {
        override fun createFromParcel(parcel: Parcel): AudioContent {
            return AudioContent(parcel)
        }

        override fun newArray(size: Int): Array<AudioContent?> {
            return arrayOfNulls(size)
        }
    }

}