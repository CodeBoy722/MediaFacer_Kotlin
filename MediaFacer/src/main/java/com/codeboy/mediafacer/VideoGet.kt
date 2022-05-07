package com.codeboy.mediafacer

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.codeboy.mediafacer.models.VideoContent
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

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
   MediaStore.Video.Media.DATE_MODIFIED,
   MediaStore.Video.Media.ARTIST
  )


 fun getVideos(context: Context, contentMedium: Uri): ArrayList<VideoContent>{
  val allVideo: ArrayList<VideoContent> = ArrayList()
  val cursor = context.contentResolver.query(contentMedium, videoProjections, null, null,
   "LOWER (" + MediaStore.Video.Media.DATE_TAKEN + ") DESC")!! //DESC ASC
  try {
   if(cursor.moveToFirst()){
    do {
     val videoContent = VideoContent()

     videoContent.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))

     videoContent.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))

     videoContent.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))

     videoContent.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))))

     val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))

     videoContent.id = id
     val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
     videoContent.videoUri = contentUri.toString()

     videoContent.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM))

     videoContent.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST))
     allVideo.add(videoContent)
    } while (cursor.moveToNext())
   }
  } catch (e: Exception) {
   e.printStackTrace()
  }
  cursor.close()
  return allVideo
 }



 fun getVideoFolders(context: Context, contentMedium: Uri){

 }

}