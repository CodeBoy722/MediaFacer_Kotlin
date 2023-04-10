package com.codeboy.mediafacer.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import java.util.*

class ImageContent(): Serializable, Parcelable {

    var name: String = ""
    var bucketName: String = ""
    var size: Long = 0
    var imageUri: String = ""
    var imageId = 0
    var filePath: String = ""
    var dateModified: Date = Date()

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()!!
        bucketName = parcel.readString()!!
        size = parcel.readLong()
        imageUri = parcel.readString()!!
        imageId = parcel.readInt()
        filePath = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(bucketName)
        parcel.writeLong(size)
        parcel.writeString(imageUri)
        parcel.writeInt(imageId)
        parcel.writeString(filePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageContent> {
        override fun createFromParcel(parcel: Parcel): ImageContent {
            return ImageContent(parcel)
        }

        override fun newArray(size: Int): Array<ImageContent?> {
            return arrayOfNulls(size)
        }
    }

}