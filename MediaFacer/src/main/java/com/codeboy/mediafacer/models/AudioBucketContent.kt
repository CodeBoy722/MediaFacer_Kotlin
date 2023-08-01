package com.codeboy.mediafacer.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class AudioBucketContent(): Serializable, Parcelable {

    /** name on local storage of the folder/bucket */
    var bucketName: String = ""

    /** full path of the folder/bucket on local storage*/
    var bucketPath: String = ""

    /** he bucket id of this folder/bucket on the device Mediastore */
    var bucketId: String = ""

    /** list containing all audio item in this folder/bucket */
    var audios = ArrayList<AudioContent>()

    constructor(parcel: Parcel) : this() {
        bucketName = parcel.readString()!!
        bucketPath = parcel.readString()!!
        bucketId = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bucketName)
        parcel.writeString(bucketPath)
        parcel.writeString(bucketId)
        parcel.writeList(audios)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioBucketContent> {
        override fun createFromParcel(parcel: Parcel): AudioBucketContent {
            return AudioBucketContent(parcel)
        }

        override fun newArray(size: Int): Array<AudioBucketContent?> {
            return arrayOfNulls(size)
        }
    }

}