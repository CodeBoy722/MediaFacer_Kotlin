@file:Suppress("DEPRECATION")

package com.codeboy.mediafacer

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import com.codeboy.mediafacer.models.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

object MediaFacer : VideoGet, AudioGet, ImageGet {

    val externalAudioContent: Uri = Audio.Media.EXTERNAL_CONTENT_URI
    val internalAudioContent: Uri = Audio.Media.INTERNAL_CONTENT_URI
    val externalVideoContent: Uri = Video.Media.EXTERNAL_CONTENT_URI
    val internalVideoContent: Uri = Video.Media.INTERNAL_CONTENT_URI
    val externalImagesContent: Uri = Images.Media.EXTERNAL_CONTENT_URI
    val internalImagesContent: Uri = Images.Media.INTERNAL_CONTENT_URI

    const val audioSearchSelectionTypeAlbum: String = Audio.Media.ALBUM
    const val audioSearchSelectionTypeArtist: String = Audio.Media.ARTIST
    const val audioSearchSelectionTypeTitle: String = Audio.Media.TITLE

    @SuppressLint("InlinedApi")
    const val audioSearchSelectionTypeGenre: String = Audio.Media.GENRE

    private var mediaPaginationStart = 0
    private var mediaPaginationLimit = 0
    private var shouldPaginate = false

    fun withPagination(
        start: Int,
        limit: Int,
    ): MediaFacer {
        mediaPaginationStart = start
        mediaPaginationLimit = limit
        this.shouldPaginate = true
        return this
    }

    /**
     *@param context the app or activity Context
     * @param contentMedium internal or external storage uri
     *
     * @return  ArrayList of AudioContent representing data of audio media items on device Mediastore
     */
    override fun getAudios(
        context: Context,
        contentMedium: Uri
    ): ArrayList<AudioContent> {
        var allAudio = ArrayList<AudioContent>()
        when {
            shouldPaginate -> {

                val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.query(
                        contentMedium, audioProjections, audioSelection, null,
                        "LOWER (" + Audio.Media.TITLE + ") ASC"
                    )!! //"LOWER ("+Audio.Media.TITLE + ") ASC"
                } else {
                    context.contentResolver.query(
                        contentMedium, audioProjectionsBelowQ, audioSelection, null,
                        "LOWER (" + Audio.Media.TITLE + ") ASC"
                    )!! //"LOWER ("+Audio.Media.TITLE + ") ASC"
                }
                var index = 0
                when {
                    cursor.moveToPosition(mediaPaginationStart) -> {
                        do {
                            val audio = AudioContent()

                            audio.filePath =
                                cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))

                            audio.name =
                                cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))

                            audio.title =
                                cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

                            val id: Long =
                                cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media._ID))
                            audio.musicId = id

                            val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                            audio.musicUri = contentUri.toString()

                            audio.musicSize =
                                cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))

                            audio.album =
                                cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))

                            audio.duration =
                                cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))

                            audio.dateModified = Date(
                                TimeUnit.SECONDS.toMillis(
                                    cursor.getLong(
                                        cursor.getColumnIndexOrThrow(Audio.Media.DATE_MODIFIED)
                                    )
                                )
                            )

                            val albumId: Long =
                                cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
                            val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                            val artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())
                            audio.artUri = artUri.toString()

                            audio.artist =
                                cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

                            var genreVolume = ""
                            if (contentMedium == externalAudioContent) {
                                genreVolume = "external"
                            } else if (contentMedium == internalAudioContent) {
                                genreVolume = "internal"
                            }

                            audio.genre = getGenre(
                                cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID)),
                                genreVolume,
                                context
                            )

                            allAudio.add(audio)
                            index++
                            if (index == mediaPaginationLimit)
                                break
                        } while (cursor.moveToNext())
                    }
                }
                cursor.close()
                mediaPaginationStart = 0
                mediaPaginationLimit = 0
                shouldPaginate = false
            }
            else -> allAudio = super.getAudios(context, contentMedium)
        }
        return allAudio
    }

    /**
     *@param context the app or activity Context
     * @param contentMedium internal or external storage uri
     *
     * @return  ArrayList of VideoContent representing data of video media items on device Mediastore
     */
    @SuppressLint("Range")
    override fun getVideos(
        context: Context,
        contentMedium: Uri
    ): ArrayList<VideoContent> {
        var videos = ArrayList<VideoContent>()
        when {
            shouldPaginate -> {
                val cursor = context.contentResolver.query(
                    contentMedium, videoProjections, null, null,
                    "LOWER (" + Video.Media.DATE_MODIFIED + ") DESC"
                )!! //DESC ASC
                var index = 0
                when {
                    cursor.moveToPosition(mediaPaginationStart) -> {
                        do {
                            val video = VideoContent()

                            video.filePath =
                                cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DATA))

                            video.name =
                                cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DISPLAY_NAME))

                            video.duration =
                                cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.DURATION))

                            video.size =
                                cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.SIZE))

                            video.dateModified = Date(
                                TimeUnit.SECONDS.toMillis(
                                    cursor.getLong(
                                        cursor.getColumnIndexOrThrow(Video.Media.DATE_MODIFIED)
                                    )
                                )
                            )

                            val id: Int =
                                cursor.getInt(cursor.getColumnIndexOrThrow(Video.Media._ID))
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
                            index++
                            if (index == mediaPaginationLimit)
                                break
                        } while (cursor.moveToNext())
                    }
                }
                cursor.close()
                mediaPaginationStart = 0
                mediaPaginationLimit = 0
                shouldPaginate = false
            }
            else -> videos = super.getVideos(context, contentMedium)
        }
        return videos
    }

    /**
     *@param context the app or activity Context
     * @param contentMedium internal or external storage uri
     *
     * @return  ArrayList of ImageContent representing data of image media items on device Mediastore
     */
    override fun getImages(
        context: Context,
        contentMedium: Uri
    ): ArrayList<ImageContent> {
        var allImages = ArrayList<ImageContent>()
        when {
            shouldPaginate -> {

                val cursor = context.contentResolver.query(
                    contentMedium, imageProjections, null, null,
                    "LOWER (" + Images.Media.DATE_MODIFIED + ") DESC"
                )!!

                var index = 0
                when {
                    cursor.moveToPosition(mediaPaginationStart) -> {
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

                            val id: Int =
                                cursor.getInt(cursor.getColumnIndexOrThrow(Images.Media._ID))
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
                cursor.close()
                mediaPaginationStart = 0
                mediaPaginationLimit = 0
                shouldPaginate = false
            }
            else -> allImages = super.getImages(context, contentMedium)
        }
        return allImages
    }


    /**
     * Get an ArrayList of VideoFolderContent with each item in the list representing a folder with videos from device Mediastore
     *@param context the app or activity Context
     * @param contentMedium internal or external storage uri
     *
     * @return  ArrayList of VideoFolderContent
     */
    override fun getVideoFolders(
        context: Context,
        contentMedium: Uri
    ): ArrayList<VideoFolderContent> {
        var videoFolders = ArrayList<VideoFolderContent>()
        if (shouldPaginate) {
            val folderIds: java.util.ArrayList<Int> = java.util.ArrayList()
            val cursor = context.contentResolver.query(
                contentMedium, videoProjections,
                null, null, "LOWER (" + Video.Media.DATE_MODIFIED + ") DESC"
            )!! //DESC
            var index = 0
            when {
                cursor.moveToFirst() -> {
                    do {
                        val bucketId: Int =
                            cursor.getInt(cursor.getColumnIndexOrThrow(Video.Media.BUCKET_ID))
                        when {
                            !folderIds.contains(bucketId) -> {
                                folderIds.add(bucketId)
                                when {
                                    (folderIds.size - 1) >= (mediaPaginationStart) -> {
                                        val videoFolder = VideoFolderContent()
                                        val folderName: String =
                                            cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.BUCKET_DISPLAY_NAME))

                                        val dataPath: String =
                                            cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DATA))
                                        var folderPath = dataPath.substring(
                                            0,
                                            dataPath.lastIndexOf("$folderName/")
                                        )
                                        folderPath = "$folderPath$folderName/"
                                        videoFolder.folderPath = folderPath

                                        videoFolder.bucketId = bucketId
                                        videoFolder.folderName = folderName
                                        videoFolder.videos =
                                            getFolderVideos(context, contentMedium, bucketId)
                                        videoFolders.add(videoFolder)

                                        index++
                                        if (index == mediaPaginationLimit)
                                            break
                                    }
                                }
                            }
                        }
                    } while (cursor.moveToNext())
                }
            }
            cursor.close()
            mediaPaginationStart = 0
            mediaPaginationLimit = 0
            shouldPaginate = false
        } else videoFolders = super.getVideoFolders(context, contentMedium)
        return videoFolders
    }


    /**
     * Get an ArrayList of ImageFolderContent with each item in the list representing a folder with images from device Mediastore
     *@param context the app or activity Context
     * @param contentMedium internal or external storage uri
     *
     * @return  ArrayList of ImageFolderContent
     */
    override fun getImageFolders(
        context: Context,
        contentMedium: Uri
    ): ArrayList<ImageFolderContent> {
        var imageFolders = ArrayList<ImageFolderContent>()
        if (shouldPaginate) {
            val folderIds: java.util.ArrayList<Int> = java.util.ArrayList()
            val cursor = context.contentResolver.query(
                contentMedium, imageProjections,
                null, null,
                "LOWER (" + Images.Media.DATE_MODIFIED + ") DESC"
            )!!
            var index = 0
            when {
                cursor.moveToFirst() -> {
                    do {
                        val bucketId: Int =
                            cursor.getInt(cursor.getColumnIndexOrThrow(Images.Media.BUCKET_ID))
                        when {
                            !folderIds.contains(bucketId) -> {
                                folderIds.add(bucketId)
                                when {
                                    (folderIds.size - 1) >= mediaPaginationStart -> {
                                        val imageFolder = ImageFolderContent()
                                        val folderName: String =
                                            cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME))
                                        val dataPath: String =
                                            cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA))
                                        var folderPath = dataPath.substring(
                                            0,
                                            dataPath.lastIndexOf("$folderName/")
                                        )
                                        folderPath = "$folderPath$folderName/"
                                        imageFolder.folderPath = folderPath

                                        imageFolder.bucketId = bucketId
                                        imageFolder.folderName = folderName
                                        imageFolder.images =
                                            getFolderImages(context, contentMedium, bucketId)
                                        imageFolders.add(imageFolder)

                                        index++
                                        if (index == mediaPaginationLimit)
                                            break
                                    }
                                }
                            }
                        }
                    } while (cursor.moveToNext())
                }
            }

            cursor.close()
            mediaPaginationStart = 0
            mediaPaginationLimit = 0
            shouldPaginate = false
        } else imageFolders = super.getImageFolders(context, contentMedium)
        return imageFolders
    }

    /**
     * Get an ArrayList of AudioAlbumContent where each AudioAlbumContent is a data class containing audios with the same album name
     * from device Mediastore
     *@param context the app or activity Context
     * @param contentMedium internal or external storage uri
     *
     * @return  ArrayList of AudioAlbumContent
     */
    override fun getAlbums(
        context: Context,
        contentMedium: Uri
    ): ArrayList<AudioAlbumContent> {
        var albums = ArrayList<AudioAlbumContent>()
        if (shouldPaginate) {
            val albumIds = java.util.ArrayList<String>()
            val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.query(
                    contentMedium, audioProjections, audioSelection, null,
                    "LOWER (" + Audio.Media.ALBUM + ") ASC"
                )!!
            } else {
                context.contentResolver.query(
                    contentMedium, audioProjectionsBelowQ, audioSelection, null,
                    "LOWER (" + Audio.Media.ALBUM + ") ASC"
                )!!
            }
            var index = 0
            when {
                cursor.moveToFirst() -> {
                    do {
                        val albumId =
                            cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
                        when {
                            !albumIds.contains(albumId) -> {
                                albumIds.add(albumId)
                                when {
                                    (albumIds.size - 1) >= mediaPaginationStart -> {
                                        val album = AudioAlbumContent()
                                        val albumName =
                                            cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
                                        album.albumName = albumName

                                        val albumArtist =
                                            cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))
                                        /* val albumArtist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                             cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ARTIST))
                                         } else {
                                             cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))
                                         }*/
                                        album.albumArtist = albumArtist

                                        val sArtworkUri =
                                            Uri.parse("content://media/external/audio/albumart")
                                        val imageUri =
                                            Uri.withAppendedPath(sArtworkUri, albumId.toString())
                                        album.albumArtUri = imageUri.toString()
                                        album.albumId = albumId

                                        val audios = getAlbumAudios(context, contentMedium, albumId)
                                        album.albumAudios = audios
                                        albums.add(album)

                                        index++
                                        if (index == mediaPaginationLimit)
                                            break
                                    }
                                }
                            }
                        }
                    } while (cursor.moveToNext())
                }
            }
            cursor.close()
            mediaPaginationStart = 0
            mediaPaginationLimit = 0
            shouldPaginate = false
        } else albums = super.getAlbums(context, contentMedium)
        return albums
    }

    /**
     * Get an ArrayList of AudioBucketContent where each AudioBucketContent is a data class containing audios in the same folder on device storage
     * from device Mediastore
     *@param context the app or activity Context
     * @param contentMedium internal or external storage uri
     *
     * @return  ArrayList of AudioBucketContent
     */
    override fun getBuckets(
        context: Context,
        contentMedium: Uri
    ): ArrayList<AudioBucketContent> {
        var audioBuckets = ArrayList<AudioBucketContent>()
        if (shouldPaginate) {

            val bucketIdsOrPaths = ArrayList<String>()
            val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.query(
                    contentMedium, audioProjections, audioSelection, null,
                    "LOWER (" + Audio.Media.TITLE + ") ASC"
                )!!
            } else {
                context.contentResolver.query(
                    contentMedium, audioProjectionsBelowQ, audioSelection, null,
                    "LOWER (" + Audio.Media.TITLE + ") ASC"
                )!!
            }
            var index = 0
            when {
                cursor.moveToFirst() -> {
                    do {
                        val audioBucket = AudioBucketContent()
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                                val bucketIdOrPath =
                                    cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.BUCKET_ID))
                                when {
                                    !bucketIdsOrPaths.contains(bucketIdOrPath) -> {
                                        bucketIdsOrPaths.add(bucketIdOrPath)
                                        when {
                                            bucketIdsOrPaths.size - 1 >= mediaPaginationStart -> {
                                                val folderNameQ = cursor.getString(
                                                    cursor.getColumnIndexOrThrow(Audio.Media.BUCKET_DISPLAY_NAME)
                                                )
                                                val dataPath: String = cursor.getString(
                                                    cursor.getColumnIndexOrThrow(Audio.Media.DATA)
                                                )
                                                var folderPath = dataPath.substring(
                                                    0,
                                                    dataPath.lastIndexOf("$folderNameQ/")
                                                )
                                                folderPath = "$folderPath$folderNameQ/"

                                                audioBucket.bucketPath = folderPath
                                                audioBucket.bucketId = bucketIdOrPath
                                                audioBucket.bucketName = folderNameQ

                                                val folderAudios = getBucketAudios(
                                                    context,
                                                    contentMedium,
                                                    bucketIdOrPath,
                                                    "id"
                                                )
                                                audioBucket.audios = folderAudios
                                                audioBuckets.add(audioBucket)

                                                index++
                                                if (index == mediaPaginationLimit)
                                                    break
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                val dataPath: String =
                                    cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))
                                val path = File(dataPath)
                                val parent = File(path.parent!!)
                                val folderName = parent.name

                                var folderPath =
                                    dataPath.substring(0, dataPath.lastIndexOf("$folderName/"))
                                folderPath = "$folderPath$folderName/"

                                when {
                                    !bucketIdsOrPaths.contains(folderPath) -> {
                                        bucketIdsOrPaths.add(folderPath)
                                        when {
                                            bucketIdsOrPaths.size - 1 >= mediaPaginationStart -> {
                                                audioBucket.bucketPath = folderPath
                                                audioBucket.bucketName = folderName
                                                val folderAudios = getBucketAudios(
                                                    context,
                                                    contentMedium,
                                                    folderPath,
                                                    "path"
                                                )
                                                audioBucket.audios = folderAudios
                                                audioBuckets.add(audioBucket)

                                                index++
                                                if (index == mediaPaginationLimit)
                                                    break
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } while (cursor.moveToNext())
                }
            }
            cursor.close()
            mediaPaginationStart = 0
            mediaPaginationLimit = 0
            shouldPaginate = false
        } else audioBuckets = super.getBuckets(context, contentMedium)
        return audioBuckets
    }

    /**
     * Get an ArrayList of AudioArtistContent where each AudioArtistContent is a data class containing audio items from the same artist
     * from device Mediastore
     *@param context the app or activity Context
     * @param contentMedium internal or external storage uri
     *
     * @return  ArrayList of AudioArtistContent
     */
    override fun getArtists(
        context: Context,
        contentMedium: Uri
    ): java.util.ArrayList<AudioArtistContent> {
        var audioArtists = ArrayList<AudioArtistContent>()
        if (shouldPaginate) {
            val artistNames = ArrayList<String>()
            val cursor = context.contentResolver.query(
                contentMedium, artistProjection, audioSelection, null,
                "LOWER (" + Audio.Artists.ARTIST + ") ASC"
            )!!
            var index = 0
            when {
                cursor.moveToFirst() -> {
                    do {
                        val artist = AudioArtistContent()
                        val artistName: String =
                            cursor.getString(cursor.getColumnIndexOrThrow(Audio.Artists.ARTIST))
                        when {
                            !artistNames.contains(artistName) -> {
                                artistNames.add(artistName)
                                when {
                                    (artistNames.size - 1) >= mediaPaginationStart -> {
                                        artist.artistName = artistName
                                        artist.albums =
                                            getArtistAlbums(context, contentMedium, artistName)
                                        audioArtists.add(artist)

                                        index++
                                        if (index == mediaPaginationLimit)
                                            break
                                    }
                                }
                            }
                        }
                    } while (cursor.moveToNext())
                }
            }
            cursor.close()
            mediaPaginationStart = 0
            mediaPaginationLimit = 0
            shouldPaginate = false
        } else audioArtists = super.getArtists(context, contentMedium)
        return audioArtists
    }


    /**
     * Get an ArrayList of AudioGenreContent where each AudioGenreContent is a data class containing audio items with the same genre
     * from device Mediastore
     *@param context the app or activity Context
     * @param contentMedium internal or external storage uri
     *
     * @return  ArrayList of AudioGenreContent
     */
    override fun getGenres(
        context: Context,
        contentMedium: Uri
    ): java.util.ArrayList<AudioGenreContent> {
        var audioGenres = ArrayList<AudioGenreContent>()
        if (shouldPaginate) {
            val genreNames = ArrayList<String>()
            val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.query(
                    contentMedium, audioProjections, audioSelection, null,
                    "LOWER (" + Audio.Media.TITLE + ") ASC"
                )!! //"LOWER ("+Audio.Media.TITLE + ") ASC"
            } else {
                context.contentResolver.query(
                    contentMedium, audioProjectionsBelowQ, audioSelection, null,
                    "LOWER (" + Audio.Media.TITLE + ") ASC"
                )!! //"LOWER ("+Audio.Media.TITLE + ") ASC"
            }
            var index = 0
            when {
                cursor.moveToFirst() -> {
                    do {
                        val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID))
                        var genreVolume = ""
                        if (contentMedium == externalAudioContent) {
                            genreVolume = "external"
                        } else if (contentMedium == internalAudioContent) {
                            genreVolume = "internal"
                        }

                        var genre = ""
                        var genreId = ""
                        val uri = Audio.Genres.getContentUriForAudioId(genreVolume, id)
                        val genresCursor: Cursor =
                            context.contentResolver.query(uri, genresProj, null, null, null)!!
                        while (genresCursor.moveToNext()) {
                            genre =
                                genresCursor.getString(genresCursor.getColumnIndexOrThrow(Audio.Genres.NAME))
                            genreId =
                                genresCursor.getString(genresCursor.getColumnIndexOrThrow(Audio.Genres._ID))
                        }
                        genresCursor.close()
                        when {
                            genre.trim().isEmpty() -> {
                                genre = "unknown"
                            }
                        }

                        when {
                            !genreNames.contains(genre) -> {
                                genreNames.add(genre)
                                when {
                                    genreNames.size - 1 >= mediaPaginationStart -> {
                                        val audioGenre = AudioGenreContent()
                                        audioGenre.genreName = genre
                                        audioGenre.genreId = genreId
                                        audioGenre.audios = getGenreAudios(
                                            context,
                                            contentMedium,
                                            genreVolume,
                                            genreId,
                                            genre
                                        )
                                        audioGenres.add(audioGenre)

                                        index++
                                        if (index == mediaPaginationLimit)
                                            break
                                    }
                                }
                            }
                        }
                    } while (cursor.moveToNext())
                }
            }

            cursor.close()
            mediaPaginationStart = 0
            mediaPaginationLimit = 0
            shouldPaginate = false
        } else audioGenres = super.getGenres(context, contentMedium)
        return audioGenres
    }

    /**
     * Get an ArrayList of AudioContent from device Mediastore matching a specified search string
     *@param context the app or activity Context
     * @param contentMedium internal or external storage uri
     * @param selectionType defines the type of value to search for ie:  Audio.Media.ALBUM, Audio.Media.ARTIST, Audio.Media.TITLE
     * @param selectionValue the search string
     *
     * @return  ArrayList of AudioContent
     */
    override fun searchAudios(
        context: Context,
        contentMedium: Uri,
        selectionType: String,
        selectionValue: String
    ): ArrayList<AudioContent> {
        var foundAudios = ArrayList<AudioContent>()
        if (shouldPaginate) {

            val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.query(
                    contentMedium, audioProjections,
                    "$selectionType like ? ",
                    arrayOf("%$selectionValue%"),
                    "LOWER (" + Audio.Media.TITLE + ") ASC"
                )!!
            } else {
                context.contentResolver.query(
                    contentMedium, audioProjectionsBelowQ,
                    "$selectionType like ? ",
                    arrayOf("%$selectionValue%"),
                    "LOWER (" + Audio.Media.TITLE + ") ASC"
                )!!
            }
            var index = 0
            when {
                cursor.moveToPosition(mediaPaginationStart) -> {
                    do {
                        val audio = AudioContent()
                        audio.filePath =
                            cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))
                        audio.name =
                            cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
                        audio.title =
                            cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

                        val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media._ID))
                        audio.musicId = id

                        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                        audio.musicUri = contentUri.toString()

                        audio.musicSize =
                            cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
                        audio.album =
                            cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
                        audio.duration =
                            cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
                        audio.dateModified = Date(
                            TimeUnit.SECONDS.toMillis(
                                cursor.getLong(
                                    cursor.getColumnIndexOrThrow(Audio.Media.DATE_MODIFIED)
                                )
                            )
                        )

                        val albumId: Long =
                            cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
                        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                        val artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())
                        audio.artUri = artUri.toString()

                        audio.artist =
                            cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

                        var genreVolume = ""
                        if (contentMedium == externalAudioContent) {
                            genreVolume = "external"
                        } else if (contentMedium == internalAudioContent) {
                            genreVolume = "internal"
                        }

                        audio.genre = getGenre(
                            cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID)),
                            genreVolume,
                            context
                        )
                        foundAudios.add(audio)

                        index++
                        if (index == mediaPaginationLimit)
                            break
                    } while (cursor.moveToNext())
                }
            }

            cursor.close()
            mediaPaginationStart = 0
            mediaPaginationLimit = 0
            shouldPaginate = false
        } else foundAudios =
            super.searchAudios(context, contentMedium, selectionType, selectionValue)
        return foundAudios
    }


    /**
     * Get an ArrayList of VideoContent from device Mediastore matching a specified search string
     *@param context the app or activity Context
     * @param contentMedium internal or external storage uri
     * @param selectionType defines the type of value to search for ie:  Video.Media.DISPLAY_NAME, Video.Media.BUCKET_DISPLAY_NAME
     * @param selectionValue the search string
     *
     * @return  ArrayList of VideoContent
     */
    override fun searchVideos(
        context: Context,
        contentMedium: Uri,
        selectionType: String,
        selectionValue: String
    ): ArrayList<VideoContent> {
        var videos: ArrayList<VideoContent> = ArrayList()
        if (shouldPaginate) {

            val cursor = context.contentResolver.query(
                contentMedium, videoProjections,
                "$selectionType like ? ",
                arrayOf("%$selectionValue%"),
                "LOWER (" + Audio.Media.DATE_MODIFIED + ") ASC"
            )!!
            var index = 0
            when {
                cursor.moveToPosition(mediaPaginationStart) -> {
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

                        video.artist =
                            cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.ARTIST))
                        videos.add(video)

                        index++
                        if (index == mediaPaginationLimit)
                            break
                    } while (cursor.moveToNext())
                }
            }
            cursor.close()
            mediaPaginationStart = 0
            mediaPaginationLimit = 0
            shouldPaginate = false
        } else {
            videos = super.searchVideos(context, contentMedium, selectionType, selectionValue)
        }
        return videos
    }

    /*fun getGalleryContent(context: Context, contentMedium: Uri){


    }

    fun getGalleryByFolderContent(context: Context, contentMedium: Uri){

    }

    fun deleteMedia(mediaId: Int) {

    }

    fun renameMedia(mediaId: Int) {

    }*/


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