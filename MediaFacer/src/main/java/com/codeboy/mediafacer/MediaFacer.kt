package com.codeboy.mediafacer

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log

class MediaFacer() {

    /*in initialization function, get all audio album Ids, audio bucket Ids, audio artist Ids, image bucket Ids, image album Ids
    * video folder Ids in their various media classes*/

    companion object {
        fun initialize(appContext: Context){

            VideoGet.findBuckets()
            ImageGet.findAlbums()
            ImageGet.findBuckets()
            AudioGet.findAlbums()
            AudioGet.findArtists()
            AudioGet.findBuckets()




        }

    }

    fun setVideoObserver(appContext: Context){
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

    }

    fun deleteMedia(mediaId: Int){

    }

    fun renameMedia(mediaId: Int){

    }

}