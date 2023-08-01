package com.codeboy.mediafacer.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class AudioArtistContent() : Serializable, Parcelable {

    var artistName: String = ""

    var albums = ArrayList<AudioAlbumContent>()

    constructor(parcel: Parcel) : this() {
        artistName = parcel.readString()!!
    }

    fun musicCount(): Int{
        var count = 0
        albums.forEach { album: AudioAlbumContent ->
            count += album.albumAudios.size
        }
        return count
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(artistName)
        parcel.writeList(albums)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioArtistContent> {
        override fun createFromParcel(parcel: Parcel): AudioArtistContent {
            return AudioArtistContent(parcel)
        }

        override fun newArray(size: Int): Array<AudioArtistContent?> {
            return arrayOfNulls(size)
        }
    }

}