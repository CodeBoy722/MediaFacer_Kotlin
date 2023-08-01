package com.codeboy.mediafacer.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import java.util.*

class ImageContent(): Serializable, Parcelable {

    /** the name of the image file as it is on device storage */
    var name: String = ""

    /** name of the folder on storage that contains this image file */
    var bucketName: String = ""

    /** the file size in kb, Mo or GO of this image file */
    var size: Long = 0

    /** the direct access path to this image file, can be used in an Imageview to display the image */
    var imageUri: String = ""

    /** image item id as stored in the Images MediaStore */
    var imageId = 0

    /** the full file path to this image item as it is on device storage, can no longer be used to access and display the image at runtime, use imageUri instead */
    var filePath: String = ""

    /** date representing the last time this image item was modified */
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