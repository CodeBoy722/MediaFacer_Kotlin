package com.codeboy.mediafacer.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class VideoFolderContent(): Serializable, Parcelable {
    /** an ArrayList<VideoContent> containing all video items located in this folder/bucket */
    var videos: ArrayList<VideoContent> = ArrayList()

    /** the total number of video items located in this folder/bucket  */
    var videoFolderSize = videos.size

    /** name of the folder on local storage containing the video items in this folder/bucket on the device Mediastore*/
    var folderName: String = ""

    /** the full path on local storage containing the video items in this folder/bucket  on the device Mediastore*/
    var folderPath: String = ""

    /** the bucket id of this folder/bucket on the device Mediastore*/
    var bucketId = 0

    constructor(parcel: Parcel) : this() {
        videoFolderSize = parcel.readInt()
        folderName = parcel.readString()!!
        folderPath = parcel.readString()!!
        bucketId = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(videos)
        parcel.writeInt(videoFolderSize)
        parcel.writeString(folderName)
        parcel.writeString(folderPath)
        parcel.writeInt(bucketId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoFolderContent> {
        override fun createFromParcel(parcel: Parcel): VideoFolderContent {
            return VideoFolderContent(parcel)
        }

        override fun newArray(size: Int): Array<VideoFolderContent?> {
            return arrayOfNulls(size)
        }
    }
}