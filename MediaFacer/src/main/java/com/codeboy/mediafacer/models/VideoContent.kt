package com.codeboy.mediafacer.models

import android.media.MediaMetadata
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaMetadataCompat
import java.io.Serializable
import java.util.*

class VideoContent(): Parcelable, Serializable {

    var id: Int = 0
    var name: String = ""
    var duration: Long = 0
    var size: Long = 0
    var videoUri: String = ""
    var artist: String = ""
    var filePath: String = ""
    var dateModified: Date = Date()

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        name = parcel.readString()!!
        duration = parcel.readLong()
        size = parcel.readLong()
        videoUri = parcel.readString()!!
        artist = parcel.readString()!!
        filePath = parcel.readString()!!
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getMediaMetadata(): androidx.media3.common.MediaMetadata {
        return androidx.media3.common.MediaMetadata.Builder()
            //.setAlbumTitle(album)
            .setTitle(name)
            .setArtist(artist)
            //.setGenre(genre)
            .setIsBrowsable(true)
            .setIsPlayable(true)
            //.setArtworkUri(Uri.parse(artUri))
            .setMediaType(androidx.media3.common.MediaMetadata.MEDIA_TYPE_VIDEO)
            .build()
    }

    fun getMediaMetadataCompat(): MediaMetadataCompat{
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, videoUri)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadata.METADATA_KEY_TITLE, name)
            .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, videoUri)
            .putString(MediaMetadata.METADATA_KEY_ART_URI, videoUri)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
            .build()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeLong(duration)
        parcel.writeLong(size)
        parcel.writeString(videoUri)
        parcel.writeString(artist)
        parcel.writeString(filePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoContent> {
        override fun createFromParcel(parcel: Parcel): VideoContent {
            return VideoContent(parcel)
        }

        override fun newArray(size: Int): Array<VideoContent?> {
            return arrayOfNulls(size)
        }
    }

}