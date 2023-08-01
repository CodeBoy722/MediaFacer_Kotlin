package com.codeboy.mediafacer.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class ImageFolderContent(): Serializable, Parcelable {
    /** the full path on local storage containing the image items in this folder/bucket  on the device Mediastore */
    var folderPath: String = ""

    /** name of the folder on local storage containing the image items in this folder/bucket on the device Mediastore */
    var folderName: String = ""

    /** an ArrayList<ImageContent> containing all image items located in this folder/bucket */
    var images: ArrayList<ImageContent> = ArrayList()

    /** the total number of images located in this folder/bucket */
    var imageFolderSize = images.size

    /** the bucket id of this folder/bucket on the device Mediastore */
    var bucketId = 0

    constructor(parcel: Parcel) : this() {
        folderPath = parcel.readString()!!
        folderName = parcel.readString()!!
        imageFolderSize = parcel.readInt()
        bucketId = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(folderPath)
        parcel.writeString(folderName)
        parcel.writeList(images)
        parcel.writeInt(imageFolderSize)
        parcel.writeInt(bucketId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageFolderContent> {
        override fun createFromParcel(parcel: Parcel): ImageFolderContent {
            return ImageFolderContent(parcel)
        }

        override fun newArray(size: Int): Array<ImageFolderContent?> {
            return arrayOfNulls(size)
        }
    }
}