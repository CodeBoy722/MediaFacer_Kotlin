package com.codeboy.mediafacer.models
import android.media.MediaMetadata
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaMetadataCompat
import java.io.Serializable
import java.util.*

class AudioContent(): Serializable, Parcelable {

    /** the name of the audio file as it is on device storage */
    var name: String = ""

    /** the title of the audio/music of the audio file as it is in it's metadata and the device Mediastore note that this can be different from file name */
    var title: String = ""

    /** artist name of the audio/music item */
    var artist: String = ""

    /** album name to which the audio/music belongs as defined in it's metadata */
    var album: String = ""

    /** the music genre to which this audio item belongs */
    var genre: String = ""

    /** the path to directly access the cover art of this audio if one is present, can be used to display the cover art in an Imageview */
    var artUri: String = ""

    /** the file size of the audio item in bytes */
    var musicSize: Long = 0

    /** length of the song contained in the audio item measured in mili seconds */
    var duration: Long = 0

    /** the item id of this audio item as it is on the device Mediastore */
    var musicId: Long = 0

    /** the direct access path to this audio file, can be used to load the audio in a MediaPlayer or ExoPlayer instance */
    var musicUri: String = ""

    /** the full file path to this audio item as it is on device storage, can no longer be used to access the audio in runtime, use musicUri instead */
    var filePath: String = ""

    /** date representing the last time this audio item was modified */
    var dateModified: Date = Date()

    /** returns a Media3 object of this audio item that can be used to load the song in a MediaPlayer and an ExoPlayer instance */
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

    /**
    returns a Media2 object of this audio item that can be used to load the song in a MediaPlayer and an ExoPlayer instance
     */
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