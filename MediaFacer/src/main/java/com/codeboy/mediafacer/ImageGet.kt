package com.codeboy.mediafacer

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.models.ImageFolderContent
import java.util.*
import java.util.concurrent.TimeUnit


internal interface ImageGet {

    val imageProjections: Array<String>
        get() = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_MODIFIED
        )

    fun getImages(context: Context, contentMedium: Uri): ArrayList<ImageContent>{
        val allImages = ArrayList<ImageContent>()
        val cursor = context.contentResolver.query(contentMedium
            ,imageProjections
            , null, null,
            "LOWER (" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC")!!

        //try {
        when {
            cursor.moveToFirst() -> {
                do {
                    val imageContent = ImageContent()

                    imageContent.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))

                    imageContent.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))

                    imageContent.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))))

                    imageContent.bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))

                    val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    imageContent.imageId = id

                    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                    imageContent.imageUri = contentUri.toString()

                    allImages.add(imageContent)

                } while (cursor.moveToNext())

            }
        }
        /*} catch (e: Exception) {
            e.printStackTrace()
        }*/
        cursor.close()
        return  allImages
    }

    fun getImageFolders(context: Context, contentMedium: Uri): ArrayList<ImageFolderContent>{
        val imageFolders: ArrayList<ImageFolderContent> = ArrayList()
        val folderIds: ArrayList<Int> = ArrayList()
        val cursor = context.contentResolver.query(contentMedium
            ,imageProjections, null, null,
            "LOWER (" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC")!!

        //try {
        when {
            cursor.moveToFirst() -> {
                do{
                    val bucketId: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID))

                    when {
                        !folderIds.contains(bucketId) -> {
                            folderIds.add(bucketId)
                            val imageFolder = ImageFolderContent()

                            val folderName: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                            val dataPath: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                            var folderPath = dataPath.substring(0, dataPath.lastIndexOf("$folderName/"))
                            folderPath = "$folderPath$folderName/"

                            imageFolder.bucketId = bucketId
                            imageFolder.folderName = folderName
                            imageFolder.folderPath = folderPath
                            imageFolder.images = getFolderImages(context,contentMedium,bucketId)
                            imageFolders.add(imageFolder)
                        }
                    }
                }while (cursor.moveToNext())
            }
        }
        /*}catch (ex: Exception){
            ex.printStackTrace()
        }*/
        cursor.close()
        return imageFolders
    }

    fun getFolderImages(context: Context, contentMedium: Uri, bucketId: Int): ArrayList<ImageContent>{
        val images: ArrayList<ImageContent> = ArrayList()
        val cursor = context.contentResolver.query(contentMedium, imageProjections,
            MediaStore.Images.Media.BUCKET_ID + " like ? ", arrayOf("%$bucketId%"),
            "LOWER (" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC")!!

        //try {
        when {
            cursor.moveToFirst() -> {
                do {
                    val imageContent = ImageContent()

                    imageContent.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))

                    imageContent.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))

                    imageContent.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))))

                    imageContent.bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))

                    val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    imageContent.imageId = id

                    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                    imageContent.imageUri = contentUri.toString()

                    images.add(imageContent)

                } while (cursor.moveToNext())
            }
        }
        /*} catch (e: Exception) {
            e.printStackTrace()
        }*/
        cursor.close()
        return images
    }

    /**Returns an ArrayList of {@link ImageFolderContent} with all images set,
     * NOTE: this function does not use pagination*/
    fun getAbsoluteImageFolders(context: Context, contentMedium: Uri): ArrayList<ImageFolderContent>{
        val absolutePictureFolders: ArrayList<ImageFolderContent> = ArrayList()
        val folderIds: ArrayList<Int> = ArrayList()

        val cursor = context.contentResolver.query(contentMedium
            ,imageProjections, null, null,
            "LOWER (" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC")!!

        //try {
            if(cursor.moveToFirst()){
                do{
                    val imageFolder = ImageFolderContent()
                    val image = ImageContent()

                    image.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))

                    image.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))

                    image.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))))

                    image.bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))

                    val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    image.imageId = id

                    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                    image.imageUri = contentUri.toString()

                    val folderName: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                    val bucketId: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID))

                    val dataPath: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                    var folderPath = dataPath.substring(0, dataPath.lastIndexOf("$folderName/"))
                    folderPath = "$folderPath$folderName/"

                    if(!folderIds.contains(bucketId)){
                        folderIds.add(bucketId)
                        imageFolder.bucketId = bucketId
                        imageFolder.folderName = folderName
                        imageFolder.folderPath = folderPath
                        imageFolder.images.add(image)
                        absolutePictureFolders.add(imageFolder)
                    }else{
                        for (folderX in absolutePictureFolders) {
                            if (folderX.bucketId == bucketId) {
                                folderX.images.add(image)
                            }
                        }
                    }
                }while (cursor.moveToNext())
            }
        /*}catch (ex: Exception){
            ex.printStackTrace()
        }*/
        cursor.close()
        return absolutePictureFolders
    }

}