package com.codeboy.mediafacer

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

class MediaFacer(val context:Context): VideoGet, AudioGet,ImageGet {

    companion object {
        val externalAudioContent = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val internalAudioContent = MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        val externalVideoContent = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val internalVideoContent = MediaStore.Video.Media.INTERNAL_CONTENT_URI
        val externalImagesContent = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val internalImagesContent = MediaStore.Images.Media.INTERNAL_CONTENT_URI
    }

    lateinit var cursor: Cursor
    var audioSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

    var mediaPaginationStart = 0
    var mediaPaginationLimit = 0
    var shouldPaginate = false

    @SuppressLint("InlinedApi")
    override val audioProjections = arrayOf(
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.COMPOSER,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.BUCKET_ID
    )

    @SuppressLint("InlinedApi")
    override val imageProjections = arrayOf(
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATE_TAKEN
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
        MediaStore.Video.Media.ARTIST
    )

    fun withVideoPagination(start: Int, limit: Int,shouldPaginate: Boolean):MediaFacer{
        return this
    }

    override fun getVideoFolders(contentMedium: Uri) {
        //super.findVideoBuckets()
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