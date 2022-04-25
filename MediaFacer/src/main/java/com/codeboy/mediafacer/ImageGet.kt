package com.codeboy.mediafacer

import android.annotation.SuppressLint
import android.provider.MediaStore


internal interface ImageGet {

    val imageProjections: Array<String>
        get() = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN
        )

    fun getImages(){

    }

    fun getImageAlbums(){

    }

    fun getImageFolders(){

    }

}