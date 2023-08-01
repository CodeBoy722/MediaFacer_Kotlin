package com.codeboy.mediafacer.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class AudioGenreContent() : Serializable, Parcelable {

    /** list containing all songs of the genre defined in genreName */
    var audios = ArrayList<AudioContent>()

    /** title of this genre */
    var genreName: String = ""

    /** id of this genre on the device Mediastore */
    var genreId: String = ""

    /** total number of songs on this genre */
    var numOfSongs = audios.size

    constructor(parcel: Parcel) : this() {
        genreName = parcel.readString()!!
        genreId = parcel.readString()!!
        numOfSongs = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(genreName)
        parcel.writeString(genreId)
        parcel.writeInt(numOfSongs)
        parcel.writeList(audios)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioGenreContent> {
        override fun createFromParcel(parcel: Parcel): AudioGenreContent {
            return AudioGenreContent(parcel)
        }

        override fun newArray(size: Int): Array<AudioGenreContent?> {
            return arrayOfNulls(size)
        }
    }

}