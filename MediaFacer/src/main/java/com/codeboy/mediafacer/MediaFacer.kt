package com.codeboy.mediafacer

import android.content.Context
import android.net.Uri
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import com.codeboy.mediafacer.models.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MediaFacer: VideoGet, AudioGet, ImageGet {

    companion object {
        val externalAudioContent: Uri = Audio.Media.EXTERNAL_CONTENT_URI
        val internalAudioContent: Uri = Audio.Media.INTERNAL_CONTENT_URI
        val externalVideoContent: Uri = Video.Media.EXTERNAL_CONTENT_URI
        val internalVideoContent: Uri = Video.Media.INTERNAL_CONTENT_URI
        val externalImagesContent: Uri = Images.Media.EXTERNAL_CONTENT_URI
        val internalImagesContent: Uri = Images.Media.INTERNAL_CONTENT_URI
    }

    var mediaPaginationStart = 0
    var mediaPaginationLimit = 0
    var shouldPaginate = false

    fun withPagination(start: Int, limit: Int,shouldPaginate: Boolean):MediaFacer{
        mediaPaginationStart = start
        mediaPaginationLimit = limit
        this.shouldPaginate = shouldPaginate
        return this
    }

    override fun getAudios(context: Context, contentMedium: Uri): ArrayList<AudioContent> {
        var allAudio = ArrayList<AudioContent>()
        when {
            shouldPaginate -> {

                val audioSelection = Audio.Media.IS_MUSIC + " != 0"
                val cursor = context.contentResolver.query(contentMedium,audioProjections,audioSelection,
                    null,
                    "LOWER (" + Audio.Media.TITLE + ") ASC")!! //"LOWER ("+Audio.Media.TITLE + ") ASC"
                var index = 0
                //try {
                when {
                    cursor.moveToPosition(mediaPaginationStart) -> {
                        do {
                            val audioContent = AudioContent()

                            audioContent.name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))

                            audioContent.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

                            val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID))
                            audioContent.musicId = id

                            val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                            audioContent.musicUri = contentUri.toString()

                            audioContent.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))

                            audioContent.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))

                            audioContent.duration = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))

                            audioContent.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DATE_MODIFIED))))

                            val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
                            val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                            audioContent.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

                            audioContent.artist = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

                            var genreVolume = ""
                            if(contentMedium == externalAudioContent){
                                genreVolume = "external"
                            }else if(contentMedium == internalAudioContent){
                                genreVolume = "internal"
                            }

                            audioContent.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID)), genreVolume, context)

                            allAudio.add(audioContent)
                            index++
                            if (index == mediaPaginationLimit)
                                break
                        } while (cursor.moveToNext())
                    }
                }
                /*}catch (e: Exception){
                    e.printStackTrace()
                }*/
                cursor.close()
                mediaPaginationStart = 0
                mediaPaginationLimit = 0
                shouldPaginate = false
            }
            else -> allAudio = super.getAudios(context, contentMedium)
        }
        return  allAudio
    }

    override fun getVideos(context: Context, contentMedium: Uri): ArrayList<VideoContent> {
        var videos = ArrayList<VideoContent>()
        when {
            shouldPaginate -> {
                val cursor = context.contentResolver.query(contentMedium, videoProjections, null, null,
                    "LOWER (" + Video.Media.DATE_MODIFIED + ") DESC")!! //DESC ASC
                var index = 0
                //try {
                when {
                    cursor.moveToPosition(mediaPaginationStart) -> {
                        do {
                            val videoContent = VideoContent()

                            videoContent.name = cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DISPLAY_NAME))

                            videoContent.duration = cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.DURATION))

                            videoContent.size = cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.SIZE))

                            videoContent.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.DATE_MODIFIED))))

                            val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Video.Media._ID))

                            videoContent.id = id
                            val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                            videoContent.videoUri = contentUri.toString()

                            videoContent.artist = cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.ARTIST))

                            videos.add(videoContent)
                            index++
                            if (index == mediaPaginationLimit)
                                break
                        } while (cursor.moveToNext())
                    }
                }
                /* } catch (e: Exception) {
                    e.printStackTrace()
                }*/
                cursor.close()
                mediaPaginationStart = 0
                mediaPaginationLimit = 0
                shouldPaginate = false
            }
            else -> videos = super.getVideos(context, contentMedium)
        }
        return videos
    }

    override fun getImages(context: Context, contentMedium: Uri): ArrayList<ImageContent> {
        var allImages = ArrayList<ImageContent>()
        when {
            shouldPaginate -> {

                val cursor = context.contentResolver.query(contentMedium,imageProjections
                    , null, null,
                    "LOWER (" + Images.Media.DATE_MODIFIED + ") DESC")!!

                var index = 0
                //try {
                when {
                    cursor.moveToPosition(mediaPaginationStart) -> {
                        do {
                            val imageContent = ImageContent()

                            imageContent.name = cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DISPLAY_NAME))

                            imageContent.size = cursor.getLong(cursor.getColumnIndexOrThrow(Images.Media.SIZE))

                            imageContent.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(Images.Media.DATE_MODIFIED))))

                            imageContent.bucketName = cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME))

                            val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Images.Media._ID))
                            imageContent.imageId = id

                            val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                            imageContent.imageUri = contentUri.toString()

                            allImages.add(imageContent)
                            index++
                            if (index == mediaPaginationLimit)
                                break
                        } while (cursor.moveToNext())

                    }
                }
                /*} catch (e: Exception) {
                    e.printStackTrace()
                }*/
                cursor.close()
                mediaPaginationStart = 0
                mediaPaginationLimit = 0
                shouldPaginate = false
            }
            else -> allImages = super.getImages(context, contentMedium)
        }
        return allImages
    }

    override fun getVideoFolders(context: Context, contentMedium: Uri): ArrayList<VideoFolderContent> {
        var videoFolders = ArrayList<VideoFolderContent>()
        if(shouldPaginate){
            //todo
        }else videoFolders = super.getVideoFolders(context, contentMedium)
        return videoFolders
    }

    override fun getImageFolders(context: Context, contentMedium: Uri): ArrayList<ImageFolderContent> {
        var imageFolders = ArrayList<ImageFolderContent>()
        if(shouldPaginate){
            //todo
        }else imageFolders = super.getImageFolders(context, contentMedium)
        return imageFolders
    }

    override fun getAlbums(context: Context, contentMedium: Uri): ArrayList<AudioAlbumContent> {
        var albums = ArrayList<AudioAlbumContent>()
        if(shouldPaginate){
            //todo
        }else albums = super.getAlbums(context, contentMedium)
        return albums
    }

    override fun getBuckets(context: Context, contentMedium: Uri, ): ArrayList<AudioBucketContent> {
        var audioBuckets = ArrayList<AudioBucketContent>()
        if (shouldPaginate){
            //todo
        }else audioBuckets = super.getBuckets(context, contentMedium)
        return audioBuckets
    }

    override fun getArtists(context: Context, contentMedium: Uri, ): java.util.ArrayList<AudioArtistContent> {
       var audioArtists = ArrayList<AudioArtistContent>()
        if (shouldPaginate){
            //todo
        }else audioArtists = super.getArtists(context, contentMedium)
        return audioArtists
    }

    override fun getGenres(context: Context, contentMedium: Uri, ): java.util.ArrayList<AudioGenreContent> {
        var audioGenres = ArrayList<AudioGenreContent>()
        if(shouldPaginate){
            //todo
        }else audioGenres = super.getGenres(context, contentMedium)
        return audioGenres
    }

    override fun searchAudios(context: Context, contentMedium: Uri, selectionType: String, selectionValue: String): ArrayList<AudioContent> {
        var foundAudios = ArrayList<AudioContent>()
        if (shouldPaginate){
            //todo
        }else foundAudios = super.searchAudios(context, contentMedium, selectionType, selectionValue)
        return foundAudios
    }

    fun deleteMedia(mediaId: Int){

    }

    fun renameMedia(mediaId: Int){

    }















   /* fun setVideoObserver(appContext: Context){
        appContext.contentResolver.registerContentObserver(
            Video.Media.INTERNAL_CONTENT_URI, true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    Log.d("MediaFacer", "Internal Media has been added or changed")
                    super.onChange(selfChange)
                    //update items here
                }
            }
        )

        appContext.contentResolver.registerContentObserver(
            Video.Media.EXTERNAL_CONTENT_URI, true,
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
            Audio.Media.INTERNAL_CONTENT_URI, true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    Log.d("MediaFacer", "Internal Media has been added or changed")
                    super.onChange(selfChange)
                    //update items here
                }
            }
        )

        appContext.contentResolver.registerContentObserver(
            Audio.Media.EXTERNAL_CONTENT_URI, true,
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
            Images.Media.INTERNAL_CONTENT_URI, true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    Log.d("MediaFacer", "Internal Media has been added or changed")
                    super.onChange(selfChange)
                    //update items here
                }
            }
        )

        appContext.contentResolver.registerContentObserver(
            Images.Media.EXTERNAL_CONTENT_URI, true,
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