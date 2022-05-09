package com.codeboy.mediafacer

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacer.models.VideoFolderContent
import java.util.*
import java.util.concurrent.TimeUnit

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
   MediaStore.Video.Media.DATE_MODIFIED,
   MediaStore.Video.Media.ARTIST
  )


 fun getVideos(context: Context, contentMedium: Uri): ArrayList<VideoContent>{
  val allVideo: ArrayList<VideoContent> = ArrayList()
  val cursor = context.contentResolver.query(contentMedium, videoProjections, null, null,
   "LOWER (" + MediaStore.Video.Media.DATE_MODIFIED + ") DESC")!! //DESC ASC
  //try {
   if(cursor.moveToFirst()){
    do {
     val video = VideoContent()

     video.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))

     video.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))

     video.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))

     video.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))))

     val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))

     video.id = id
     val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
     video.videoUri = contentUri.toString()

     video.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST))
     allVideo.add(video)
    } while (cursor.moveToNext())
   }
  /*} catch (e: Exception) {
   e.printStackTrace()
  }*/
  cursor.close()
  return allVideo
 }

 fun getVideoFolders(context: Context, contentMedium: Uri): ArrayList<VideoFolderContent>{
  val videoFolders: ArrayList<VideoFolderContent> = ArrayList()
  val folderIds: ArrayList<Int> = ArrayList()

  val cursor = context.contentResolver.query(contentMedium, videoProjections,
   null, null, "LOWER (" + MediaStore.Video.Media.DATE_MODIFIED + ") DESC")!! //DESC

  //try {
   if(cursor.moveToFirst()){
    do{

     val videoFolder = VideoFolderContent()
     val folderName: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
     val dataPath: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))

     val bucketId: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID))
     var folderPath = dataPath.substring(0, dataPath.lastIndexOf("$folderName/"))
     folderPath = "$folderPath$folderName/"

     if(!folderIds.contains(bucketId)){
      folderIds.add(bucketId)
      videoFolder.bucketId = bucketId
      videoFolder.folderName = folderName
      videoFolder.folderPath = folderPath
      videoFolders.add(videoFolder)
     }

    }while (cursor.moveToNext())
   }
  /*}catch (ex: Exception){
   ex.printStackTrace()
  }*/
  cursor.close()
  return videoFolders
 }

 fun getFolderVideos(context: Context, contentMedium: Uri, bucketId: Int): ArrayList<VideoContent>{
  val videos: ArrayList<VideoContent> = ArrayList()
  val cursor = context.contentResolver.query(contentMedium, videoProjections,
   MediaStore.Video.Media.BUCKET_ID + " like ? ", arrayOf("%$bucketId%"),
   "LOWER (" + MediaStore.Video.Media.DATE_MODIFIED + ") DESC")!! //DESC

  //try {
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

     videoContent.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST))
     videos.add(videoContent)
    } while (cursor.moveToNext())
   }
  /*} catch (e: Exception) {
   e.printStackTrace()
  }*/
  cursor.close()
  return videos
 }

 /**Returns an ArrayList of {@link VideoFolderContent} with all videos set,
  * NOTE: this function does not use pagination*/
 fun getAbsoluteVideoFolders(context: Context, contentMedium: Uri): ArrayList<VideoFolderContent>{
  val absoluteVideoFolders: ArrayList<VideoFolderContent> = ArrayList()
  val folderIds: ArrayList<Int> = ArrayList()

  val cursor = context.contentResolver.query(contentMedium, videoProjections,
   null, null, "LOWER (" + MediaStore.Video.Media.DATE_MODIFIED + ") DESC")!! //DESC

  //try {
   if(cursor.moveToFirst()){
    do{
     val videoFolder = VideoFolderContent()
     val video = VideoContent()

     video.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))

     video.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))

     video.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))

     video.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))))

     val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))

     video.id = id
     val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
     video.videoUri = contentUri.toString()

     video.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST))

     val folderName: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
     val dataPath: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))

     val bucketId: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID))
     var folderPath = dataPath.substring(0, dataPath.lastIndexOf("$folderName/"))
     folderPath = "$folderPath$folderName/"

     if(!folderIds.contains(bucketId)){
      folderIds.add(bucketId)
      videoFolder.bucketId = bucketId
      videoFolder.folderName = folderName
      videoFolder.folderPath = folderPath
      videoFolder.videos.add(video)
      absoluteVideoFolders.add(videoFolder)
     }else{
      for (folderX in absoluteVideoFolders) {
       if (folderX.bucketId == bucketId) {
        folderX.videos.add(video)
       }
      }
     }
    }while (cursor.moveToNext())
   }
  /*}catch (ex: Exception){
   ex.printStackTrace()
  }*/
  cursor.close()
  return absoluteVideoFolders
 }

}