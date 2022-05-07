package com.codeboy.mediafacer

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.codeboy.mediafacer.models.ImageContent
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


internal interface ImageGet {

    val imageProjections: Array<String>
        get() = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.ALBUM,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_MODIFIED
        )

    fun getImages(context: Context, contentMedium: Uri): ArrayList<ImageContent>{
        val allImages = ArrayList<ImageContent>()

        val cursor = context.contentResolver.query(contentMedium
            ,imageProjections
            , null, null,
            "LOWER (" + MediaStore.Images.Media.DATE_TAKEN + ") DESC")!!

        try {
            if(cursor.moveToFirst()){
                do {
                    val imageContent = ImageContent()

                    imageContent.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))

                    imageContent.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))

                    imageContent.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        //imageContent.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ALBUM))
                    }

                    imageContent.bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))

                    val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    imageContent.imageId = id

                    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                    imageContent.imageUri = contentUri.toString()

                    allImages.add(imageContent)

                } while (cursor.moveToNext())

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cursor.close()
        return  allImages
    }

    fun getImageAlbums(context: Context, contentMedium: Uri){

    }

    fun getImageFolders(context: Context, contentMedium: Uri){

    }

}