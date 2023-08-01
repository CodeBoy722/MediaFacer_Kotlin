package com.codeboy.mediafacer.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class AudioAlbumContent() : Serializable, Parcelable {

    /** title if this album */
    var albumName: String = ""

    /** id of this album on the device Mediastore */
    var albumId: String = ""

    /** cover art uri for this album, can be used in an Imageview to display the cover art*/
    var albumArtUri: String = ""

    /** artist name of this album */
    var albumArtist: String = ""

    /** list containing all songs items in this album */
    var albumAudios: ArrayList<AudioContent> = ArrayList()

    /** total number of songs in this album */
    var albumSize = albumAudios.size

    constructor(parcel: Parcel) : this() {
        albumName = parcel.readString()!!
        albumId = parcel.readString()!!
        albumArtUri = parcel.readString()!!
        albumArtist = parcel.readString()!!
        albumSize = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(albumName)
        parcel.writeString(albumId)
        parcel.writeString(albumArtUri)
        parcel.writeString(albumArtist)
        parcel.writeInt(albumSize)
        parcel.writeList(albumAudios)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioAlbumContent> {
        override fun createFromParcel(parcel: Parcel): AudioAlbumContent {
            return AudioAlbumContent(parcel)
        }

        override fun newArray(size: Int): Array<AudioAlbumContent?> {
            return arrayOfNulls(size)
        }
    }

}