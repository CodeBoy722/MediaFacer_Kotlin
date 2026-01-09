@file:Suppress("DEPRECATION")

package com.codeboy.mediafacer

import android.content.Context
import android.net.Uri
import android.provider.MediaStore.Images
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.models.ImageFolderContent
import java.util.*
import java.util.concurrent.TimeUnit

internal interface ImageGet {

    val imageProjections: Array<String>
        get() = arrayOf(
            Images.Media.DISPLAY_NAME,
            Images.Media.SIZE,
            Images.Media.BUCKET_DISPLAY_NAME,
            Images.Media.BUCKET_ID,
            Images.Media._ID,
            Images.Media.DATA,
            Images.Media.DATE_MODIFIED
        )

    fun getImages(
        context: Context,
        contentMedium: Uri
    ): ArrayList<ImageContent> {
        val allImages = ArrayList<ImageContent>()
        val cursor = context.contentResolver.query(
            contentMedium, imageProjections, null, null,
            "LOWER (" + Images.Media.DATE_MODIFIED + ") DESC"
        )!!

        //try {
        when {
            cursor.moveToFirst() -> {
                do {
                    val imageContent = ImageContent()

                    imageContent.filePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA))

                    imageContent.name =
                        cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DISPLAY_NAME))

                    imageContent.size =
                        cursor.getLong(cursor.getColumnIndexOrThrow(Images.Media.SIZE))

                    imageContent.dateModified = Date(
                        TimeUnit.SECONDS.toMillis(
                            cursor.getLong(
                                cursor.getColumnIndexOrThrow(Images.Media.DATE_MODIFIED)
                            )
                        )
                    )

                    imageContent.bucketName =
                        cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME))

                    val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Images.Media._ID))
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
        return allImages
    }

    fun getImageFolders(
        context: Context,
        contentMedium: Uri
    ): ArrayList<ImageFolderContent> {
        val imageFolders: ArrayList<ImageFolderContent> = ArrayList()
        val folderIds: ArrayList<Int> = ArrayList()
        val cursor = context.contentResolver.query(
            contentMedium, imageProjections,
            null, null,
            "LOWER (" + Images.Media.DATE_MODIFIED + ") DESC"
        )!!

        when {
            cursor.moveToFirst() -> {
                do {
                    val bucketId: Int =
                        cursor.getInt(cursor.getColumnIndexOrThrow(Images.Media.BUCKET_ID))

                    when {
                        !folderIds.contains(bucketId) -> {
                            folderIds.add(bucketId)
                            val imageFolder = ImageFolderContent()

                            val folderName: String =
                                cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME))
                            val dataPath: String =
                                cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA))
                            var folderPath =
                                dataPath.take(dataPath.lastIndexOf("$folderName/"))
                            folderPath = "$folderPath$folderName/"
                            imageFolder.folderPath = folderPath

                            imageFolder.bucketId = bucketId
                            imageFolder.folderName = folderName
                            imageFolder.images = getFolderImages(context, contentMedium, bucketId)
                            imageFolders.add(imageFolder)
                        }
                    }
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return imageFolders
    }

    fun getFolderImages(
        context: Context,
        contentMedium: Uri,
        bucketId: Int
    ): ArrayList<ImageContent> {
        val images: ArrayList<ImageContent> = ArrayList()
        val cursor = context.contentResolver.query(
            contentMedium, imageProjections,
            Images.Media.BUCKET_ID + " like ? ", arrayOf("%$bucketId%"),
            "LOWER (" + Images.Media.DATE_MODIFIED + ") DESC"
        )!!

        //try {
        when {
            cursor.moveToFirst() -> {
                do {
                    val imageContent = ImageContent()

                    imageContent.filePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA))

                    imageContent.name =
                        cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DISPLAY_NAME))

                    imageContent.size =
                        cursor.getLong(cursor.getColumnIndexOrThrow(Images.Media.SIZE))

                    imageContent.dateModified = Date(
                        TimeUnit.SECONDS.toMillis(
                            cursor.getLong(
                                cursor.getColumnIndexOrThrow(Images.Media.DATE_MODIFIED)
                            )
                        )
                    )

                    imageContent.bucketName =
                        cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME))

                    val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Images.Media._ID))
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
    private fun getAbsoluteImageFolders(
        context: Context,
        contentMedium: Uri
    ): ArrayList<ImageFolderContent> {
        val absolutePictureFolders: ArrayList<ImageFolderContent> = ArrayList()
        val folderIds: ArrayList<Int> = ArrayList()

        val cursor = context.contentResolver.query(
            contentMedium, imageProjections, null, null,
            "LOWER (" + Images.Media.DATE_MODIFIED + ") DESC"
        )!!

        if (cursor.moveToFirst()) {
            do {
                val imageFolder = ImageFolderContent()
                val image = ImageContent()

                image.name =
                    cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DISPLAY_NAME))

                image.size = cursor.getLong(cursor.getColumnIndexOrThrow(Images.Media.SIZE))

                image.dateModified = Date(
                    TimeUnit.SECONDS.toMillis(
                        cursor.getLong(
                            cursor.getColumnIndexOrThrow(Images.Media.DATE_MODIFIED)
                        )
                    )
                )

                image.bucketName =
                    cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME))

                val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Images.Media._ID))
                image.imageId = id

                val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                image.imageUri = contentUri.toString()

                val folderName: String =
                    cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME))
                val bucketId: Int =
                    cursor.getInt(cursor.getColumnIndexOrThrow(Images.Media.BUCKET_ID))

                val dataPath: String =
                    cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA))
                var folderPath = dataPath.take(dataPath.lastIndexOf("$folderName/"))
                folderPath = "$folderPath$folderName/"

                if (!folderIds.contains(bucketId)) {
                    folderIds.add(bucketId)
                    imageFolder.bucketId = bucketId
                    imageFolder.folderName = folderName
                    imageFolder.folderPath = folderPath
                    imageFolder.images.add(image)
                    absolutePictureFolders.add(imageFolder)
                } else {
                    for (folderX in absolutePictureFolders) {
                        if (folderX.bucketId == bucketId) {
                            folderX.images.add(image)
                        }
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return absolutePictureFolders
    }


    fun getImageCount(
        context: Context,
        contentMedium: Uri
    ): Int {
        val allImages = ArrayList<ImageContent>()
        val cursor = context.contentResolver.query(
            contentMedium, imageProjections, null, null, null
        )
        val numOfImages = cursor?.count ?: 0
        cursor?.close()
        return numOfImages
    }
}