package com.codeboy.mediafacer

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.models.VideoContent
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MediaFacer(): VideoGet, AudioGet,ImageGet {

    companion object {
        val externalAudioContent: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val internalAudioContent: Uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        val externalVideoContent: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val internalVideoContent: Uri = MediaStore.Video.Media.INTERNAL_CONTENT_URI
        val externalImagesContent: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val internalImagesContent: Uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI
    }
    var mediaPaginationStart = 0
    var mediaPaginationLimit = 0
    var shouldPaginate = false

    @SuppressLint("InlinedApi")
    override val audioProjections = arrayOf(
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.COMPOSER,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.BUCKET_ID,
        MediaStore.Audio.Media.DATE_MODIFIED
    )

    @SuppressLint("InlinedApi")
    override val imageProjections = arrayOf(
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATE_TAKEN,
        MediaStore.Images.Media.DATE_MODIFIED
    )

    @SuppressLint("InlinedApi")
    override val videoProjections = arrayOf(
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

    fun withPagination(start: Int, limit: Int,shouldPaginate: Boolean):MediaFacer{
        mediaPaginationStart = start
        mediaPaginationLimit = limit
        this.shouldPaginate = shouldPaginate
        return this
    }

    override fun getVideos(context: Context, contentMedium: Uri): ArrayList<VideoContent> {
        var videos = ArrayList<VideoContent>()
        if(shouldPaginate){
            val cursor = context.contentResolver.query(contentMedium, videoProjections, null, null,
                "LOWER (" + MediaStore.Video.Media.DATE_MODIFIED + ") DESC")!! //DESC ASC
            var index = 0
            //try {
                if(cursor.moveToPosition(mediaPaginationStart)){
                    do {
                        val videoContent = VideoContent()

                        videoContent.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))

                        videoContent.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))

                        videoContent.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))

                        val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))

                        videoContent.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))))

                        videoContent.id = id
                        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                        videoContent.videoUri = contentUri.toString()

                        videoContent.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM))

                        videoContent.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST))
                        videos.add(videoContent)

                        index++
                        if (index == mediaPaginationLimit)
                            break

                    } while (cursor.moveToNext())
                }
           /* } catch (e: Exception) {
                e.printStackTrace()
            }*/
            cursor.close()
            mediaPaginationStart = 0
            mediaPaginationLimit = 0
            shouldPaginate = false
        }else videos = super.getVideos(context, contentMedium)
        return videos
    }

    override fun getAudios(context: Context, contentMedium: Uri): ArrayList<AudioContent> {
        var allAudio = ArrayList<AudioContent>()
        if(shouldPaginate){

            val audioSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
            val cursor = context.contentResolver.query(contentMedium
                ,audioProjections
                ,audioSelection,
                null,
                "LOWER (" + MediaStore.Audio.Media.TITLE + ") ASC")!! //"LOWER ("+MediaStore.Audio.Media.TITLE + ") ASC"
            var index = 0
            //try {
                if (cursor.moveToPosition(mediaPaginationStart)) {
                    do {
                        val audioContent = AudioContent()
                        audioContent.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))

                        audioContent.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))

                        val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                        audioContent.musicId = id

                        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                        audioContent.musicUri = contentUri.toString()

                        audioContent.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))

                        audioContent.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))

                        audioContent.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))

                        audioContent.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))))

                        val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                        audioContent.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

                        audioContent.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))

                        try {
                            audioContent.composer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER))
                        }catch (ex: Exception){
                          ex.printStackTrace()
                        }

                        var genreVolume = ""
                        if(contentMedium == externalAudioContent){
                            genreVolume = "external"
                        }else if(contentMedium == internalAudioContent){
                            genreVolume = "internal"
                        }

                        audioContent.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)), genreVolume, context)

                        allAudio.add(audioContent)

                        index++
                        if (index == mediaPaginationLimit)
                            break
                    } while (cursor.moveToNext())
                }
           /* }catch (e: Exception){
                e.printStackTrace()
            }*/
            cursor.close()
            mediaPaginationStart = 0
            mediaPaginationLimit = 0
            shouldPaginate = false
        }else allAudio = super.getAudios(context, contentMedium)
        return  allAudio
    }

    override fun getImages(context: Context, contentMedium: Uri): ArrayList<ImageContent> {
        var allImages = ArrayList<ImageContent>()
        if(shouldPaginate){

            val cursor = context.contentResolver.query(contentMedium
                ,imageProjections
                , null, null,
                "LOWER (" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC")!!

            var index = 0
            //try {
                if(cursor.moveToPosition(mediaPaginationStart)){
                    do {
                        val imageContent = ImageContent()

                        imageContent.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))

                        imageContent.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))

                        imageContent.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))))

                        val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                        imageContent.imageId = id

                        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                        imageContent.imageUri = contentUri.toString()

                        allImages.add(imageContent)

                        index++
                        if (index == mediaPaginationLimit)
                            break

                    } while (cursor.moveToNext())

                }
           /* } catch (e: Exception) {
                e.printStackTrace()
            }*/
            cursor.close()
            mediaPaginationStart = 0
            mediaPaginationLimit = 0
            shouldPaginate = false
        }else allImages = super.getImages(context, contentMedium)
        return allImages
    }


    override fun getVideoFolders(context: Context, contentMedium: Uri) {
        if(shouldPaginate){

        }else{
            super.getVideoFolders(context, contentMedium)
        }
    }

    override fun getImageAlbums(context: Context, contentMedium: Uri) {
        super.getImageAlbums(context, contentMedium)
    }

    override fun getImageFolders(context: Context, contentMedium: Uri) {
        super.getImageFolders(context, contentMedium)
    }

    override fun getAudioAlbums(context: Context, contentMedium: Uri) {
        super.getAudioAlbums(context, contentMedium)
    }

    override fun getAudioArtist(context: Context, contentMedium: Uri) {
        super.getAudioArtist(context, contentMedium)
    }

    override fun getAudioBuckets(context: Context, contentMedium: Uri) {
        super.getAudioBuckets(context, contentMedium)
    }

    override fun getAudioGenres(context: Context, contentMedium: Uri) {
        super.getAudioGenres(context, contentMedium)
    }

    fun deleteMedia(mediaId: Int){

    }

    fun renameMedia(mediaId: Int){

    }















   /* fun setVideoObserver(appContext: Context){
        appContext.contentResolver.registerContentObserver(
            MediaStore.Video.Media.INTERNAL_CONTENT_URI, true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    Log.d("MediaFacer", "Internal Media has been added or changed")
                    super.onChange(selfChange)
                    //update items here
                }
            }
        )

        appContext.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    Log.d("MediaFacer", "External Media has been added or changed")
                    super.onChange(selfChange)
                    //update items here
                }
            }
        )
    }

    fun setAudioObserver(appContext: Context){
        appContext.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI, true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    Log.d("MediaFacer", "Internal Media has been added or changed")
                    super.onChange(selfChange)
                    //update items here
                }
            }
        )

        appContext.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    Log.d("MediaFacer", "External Media has been added or changed")
                    super.onChange(selfChange)
                    //update items here
                }
            }
        )
    }

    fun setImageObserver(appContext: Context){
        appContext.contentResolver.registerContentObserver(
            MediaStore.Images.Media.INTERNAL_CONTENT_URI, true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    Log.d("MediaFacer", "Internal Media has been added or changed")
                    super.onChange(selfChange)
                    //update items here
                }
            }
        )

        appContext.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    Log.d("MediaFacer", "External Media has been added or changed")
                    super.onChange(selfChange)
                    //update items here
                }
            }
        )
    }

    fun removeImageObserver(){

    }

    fun removeAudioObserver(){

    }

    fun removeVideoObserver(){

    }*/



}