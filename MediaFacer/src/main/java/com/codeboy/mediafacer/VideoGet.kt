@file:Suppress("DEPRECATION")

package com.codeboy.mediafacer

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Video
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacer.models.VideoFolderContent
import java.util.*
import java.util.concurrent.TimeUnit

internal interface VideoGet {

    val videoProjections: Array<String>
        get() = arrayOf(
            Video.Media.DISPLAY_NAME,
            Video.Media.DURATION,
            Video.Media.BUCKET_DISPLAY_NAME,
            Video.Media.BUCKET_ID,
            Video.Media.SIZE,
            Video.Media._ID,
            Video.Media.ALBUM,
            Video.Media.DATE_MODIFIED,
            Video.Media.DATA,
            Video.Media.ARTIST
        )

    val videoSearchSelectionTypeBucketDisplayName: String
        get() = Video.Media.BUCKET_DISPLAY_NAME

    val videoSearchSelectionTypeDisplayName: String
        get() = Video.Media.DISPLAY_NAME

    fun getVideos(
        context: Context,
        contentMedium: Uri
    ): ArrayList<VideoContent> {
        val allVideo: ArrayList<VideoContent> = ArrayList()
        val cursor = context.contentResolver.query(
            contentMedium, videoProjections, null, null,
            "LOWER (" + Video.Media.DATE_MODIFIED + ") DESC"
        )!! //DESC ASC
        when {
            cursor.moveToFirst() -> {
                do {
                    val video = VideoContent()

                    video.filePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DATA))

                    video.name =
                        cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DISPLAY_NAME))

                    video.duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.DURATION))

                    video.size = cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.SIZE))

                    video.dateModified = Date(
                        TimeUnit.SECONDS.toMillis(
                            cursor.getLong(
                                cursor.getColumnIndexOrThrow(Video.Media.DATE_MODIFIED)
                            )
                        )
                    )

                    val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Video.Media._ID))
                    video.id = id

                    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                    video.videoUri = contentUri.toString()

                    try{
                        video.artist =
                            cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.ARTIST))
                    }catch (ex : Exception){
                        ex.printStackTrace()
                    }

                    allVideo.add(video)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return allVideo
    }

    fun getVideoFolders(
        context: Context,
        contentMedium: Uri
    ): ArrayList<VideoFolderContent> {
        val videoFolders: ArrayList<VideoFolderContent> = ArrayList()
        val folderIds: ArrayList<Int> = ArrayList()
        val cursor = context.contentResolver.query(
            contentMedium, videoProjections,
            null, null, "LOWER (" + Video.Media.DATE_MODIFIED + ") DESC"
        )!! //DESC

        when {
            cursor.moveToFirst() -> {
                do {
                    val bucketId: Int =
                        cursor.getInt(cursor.getColumnIndexOrThrow(Video.Media.BUCKET_ID))

                    when {
                        !folderIds.contains(bucketId) -> {
                            folderIds.add(bucketId)
                            val videoFolder = VideoFolderContent()
                            val folderName: String =
                                cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.BUCKET_DISPLAY_NAME))

                            val dataPath: String =
                                cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DATA))
                            var folderPath =
                                dataPath.take(dataPath.lastIndexOf("$folderName/"))
                            folderPath = "$folderPath$folderName/"
                            videoFolder.folderPath = folderPath

                            videoFolder.bucketId = bucketId
                            videoFolder.folderName = folderName
                            videoFolder.videos = getFolderVideos(context, contentMedium, bucketId)
                            videoFolders.add(videoFolder)
                        }
                    }
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return videoFolders
    }

    fun getFolderVideos(
        context: Context,
        contentMedium: Uri,
        bucketId: Int
    ): ArrayList<VideoContent> {
        val videos: ArrayList<VideoContent> = ArrayList()
        val cursor = context.contentResolver.query(
            contentMedium, videoProjections,
            Video.Media.BUCKET_ID + " like ? ", arrayOf("%$bucketId%"),
            "LOWER (" + Video.Media.DATE_MODIFIED + ") DESC"
        )!! //DESC

        when {
            cursor.moveToFirst() -> {
                do {
                    val video = VideoContent()

                    video.filePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DATA))

                    video.name =
                        cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DISPLAY_NAME))

                    video.duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.DURATION))

                    video.size = cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.SIZE))

                    video.dateModified = Date(
                        TimeUnit.SECONDS.toMillis(
                            cursor.getLong(
                                cursor.getColumnIndexOrThrow(Video.Media.DATE_MODIFIED)
                            )
                        )
                    )

                    val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Video.Media._ID))

                    video.id = id
                    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                    video.videoUri = contentUri.toString()

                    try{
                        video.artist =
                            cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.ARTIST))
                    }catch (ex : Exception){
                        ex.printStackTrace()
                    }

                    videos.add(video)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return videos
    }

    /**Returns an ArrayList of {@link VideoFolderContent} with all videos set,
     * NOTE: this function does not use pagination*/
    private fun getAbsoluteVideoFolders(
        context: Context,
        contentMedium: Uri
    ): ArrayList<VideoFolderContent> {
        val absoluteVideoFolders: ArrayList<VideoFolderContent> = ArrayList()
        val folderIds: ArrayList<Int> = ArrayList()

        val cursor = context.contentResolver.query(
            contentMedium, videoProjections,
            null, null, "LOWER (" + Video.Media.DATE_MODIFIED + ") DESC"
        )!! //DESC

        //try {
        if (cursor.moveToFirst()) {
            do {
                val videoFolder = VideoFolderContent()
                val video = VideoContent()

                video.name =
                    cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DISPLAY_NAME))

                video.duration = cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.DURATION))

                video.size = cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.SIZE))

                video.dateModified =
                    Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.DATE_MODIFIED))))

                val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Video.Media._ID))

                video.id = id
                val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                video.videoUri = contentUri.toString()

                video.artist = cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.ARTIST))

                val folderName: String =
                    cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.BUCKET_DISPLAY_NAME))
                val dataPath: String =
                    cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DATA))

                val bucketId: Int =
                    cursor.getInt(cursor.getColumnIndexOrThrow(Video.Media.BUCKET_ID))
                var folderPath = dataPath.take(dataPath.lastIndexOf("$folderName/"))
                folderPath = "$folderPath$folderName/"

                if (!folderIds.contains(bucketId)) {
                    folderIds.add(bucketId)
                    videoFolder.bucketId = bucketId
                    videoFolder.folderName = folderName
                    videoFolder.folderPath = folderPath
                    videoFolder.videos.add(video)
                    absoluteVideoFolders.add(videoFolder)
                } else {
                    for (folderX in absoluteVideoFolders) {
                        if (folderX.bucketId == bucketId) {
                            folderX.videos.add(video)
                        }
                    }
                }
            } while (cursor.moveToNext())
        }
        /*}catch (ex: Exception){
         ex.printStackTrace()
        }*/
        cursor.close()
        return absoluteVideoFolders
    }

    fun searchVideos(
        context: Context,
        contentMedium: Uri,
        selectionType: String,
        selectionValue: String
    ): ArrayList<VideoContent> {
        val videos: ArrayList<VideoContent> = ArrayList()
        val cursor = context.contentResolver.query(
            contentMedium, videoProjections,
            "$selectionType like ? ",
            arrayOf("%$selectionValue%"),
            "LOWER (" + MediaStore.Audio.Media.DATE_MODIFIED + ") ASC"
        )!!

        when {
            cursor.moveToFirst() -> {
                do {
                    val video = VideoContent()

                    video.filePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DATA))

                    video.name =
                        cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DISPLAY_NAME))

                    video.duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.DURATION))

                    video.size = cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.SIZE))

                    video.dateModified = Date(
                        TimeUnit.SECONDS.toMillis(
                            cursor.getLong(
                                cursor.getColumnIndexOrThrow(Video.Media.DATE_MODIFIED)
                            )
                        )
                    )

                    val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Video.Media._ID))

                    video.id = id
                    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                    video.videoUri = contentUri.toString()

                    try{
                        video.artist =
                            cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.ARTIST))
                    }catch (ex : Exception){
                        ex.printStackTrace()
                    }

                    videos.add(video)
                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        return videos
    }

    fun getVideoCount(
        context: Context,
        contentMedium: Uri
    ): Int {
        val allVideo: ArrayList<VideoContent> = ArrayList()
        val cursor = context.contentResolver.query(
            contentMedium, videoProjections, null, null, null
        )

        val numOfVideos = cursor?.count?: 0
        cursor?.close()
        return numOfVideos
    }
}