package com.codeboy.mediafacer

import android.net.Uri
import android.provider.MediaStore

internal interface VideoGet {

   val videoProjections: Array<String>
   get() = arrayOf(
    MediaStore.Video.Media.DISPLAY_NAME,
    MediaStore.Video.Media.DURATION,
    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
    MediaStore.Video.Media.BUCKET_ID,
    MediaStore.Video.Media.SIZE,
    MediaStore.Video.Media._ID,
    MediaStore.Video.Media.ALBUM,
    MediaStore.Video.Media.DATE_TAKEN,
    MediaStore.Video.Media.ARTIST
   )


 fun getVideos(contentMedium: Uri){

 }

 fun getVideoFolders(contentMedium: Uri){

 }

}