@file:Suppress("DEPRECATION")

package com.codeboy.mediafacer

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore.Audio
import com.codeboy.mediafacer.MediaFacer.externalAudioContent
import com.codeboy.mediafacer.MediaFacer.internalAudioContent
import com.codeboy.mediafacer.models.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

internal interface AudioGet {

    val audioProjections: Array<String>
        @SuppressLint("InlinedApi")
        get() = arrayOf(
            Audio.Media.TITLE,
            Audio.Media.DISPLAY_NAME,
            Audio.Media.ARTIST,
            Audio.Media.ALBUM,
            Audio.Media.ALBUM_ID,
            Audio.Media.ALBUM_ARTIST,
            Audio.Media.COMPOSER,
            Audio.Media.SIZE,
            Audio.Media._ID,
            Audio.Media.DURATION,
            Audio.Media.BUCKET_ID,
            Audio.Media.DATA,
            Audio.Media.BUCKET_DISPLAY_NAME,
            Audio.Media.DATE_MODIFIED
        )

    val audioProjectionsBelowQ: Array<String>
        get() = arrayOf(
            Audio.Media.TITLE,
            Audio.Media.DISPLAY_NAME,
            Audio.Media.ARTIST,
            Audio.Media.ARTIST_ID,
            Audio.Media.ALBUM,
            Audio.Media.ALBUM_ID,
            Audio.Media.COMPOSER,
            Audio.Media.SIZE,
            Audio.Media._ID,
            Audio.Media.DATA,
            Audio.Media.DURATION,
            Audio.Media.DATE_MODIFIED
        )

    val artistProjection: Array<String>
        get() = arrayOf(
            Audio.Media.ARTIST,
            Audio.Artists.ARTIST
        )

    val audioSelection: String
        get() = Audio.Media.IS_MUSIC + " != 0"

    val genresProj: Array<String>
        get() = arrayOf(
            Audio.Genres.NAME,
            Audio.Genres._ID
        )

    fun getAudios(
        context: Context,
        contentMedium: Uri
    ): ArrayList<AudioContent> {
        val allAudio: ArrayList<AudioContent> = ArrayList()
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

        when {
            cursor.moveToFirst() -> {
                do {
                    val audio = AudioContent()
                    audio.filePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))
                    audio.name =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
                    audio.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

                    val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media._ID))
                    audio.musicId = id

                    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                    audio.musicUri = contentUri.toString()

                    audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
                    audio.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
                    audio.duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
                    audio.dateModified =
                        Date(
                            TimeUnit.SECONDS.toMillis(
                                cursor.getLong(
                                    cursor.getColumnIndexOrThrow(
                                        Audio.Media.DATE_MODIFIED
                                    )
                                )
                            )
                        )

                    val albumId: Long =
                        cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
                    val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                    val arUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())
                    audio.artUri = arUri.toString()

                    audio.artist =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

                    var genreVolume = ""
                    if (contentMedium == externalAudioContent) {
                        genreVolume = "external"
                    } else if (contentMedium == internalAudioContent) {
                        genreVolume = "internal"
                    }

                    audio.genre = getGenre(id.toInt(), genreVolume, context)
                    allAudio.add(audio)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return allAudio
    }

    fun getAlbums(
        context: Context,
        contentMedium: Uri
    ): ArrayList<AudioAlbumContent> {
        val albums = ArrayList<AudioAlbumContent>()
        val albumIds = ArrayList<String>()
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

        when {
            cursor.moveToFirst() -> {
                do {
                    val albumId =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
                    when {
                        !albumIds.contains(albumId) -> {
                            albumIds.add(albumId)
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

                            val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                            val imageUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())
                            album.albumArtUri = imageUri.toString()
                            album.albumId = albumId

                            val audios = getAlbumAudios(context, contentMedium, albumId)
                            album.albumAudios = audios
                            albums.add(album)
                        }
                    }
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return albums
    }

    fun getAlbumAudios(
        context: Context,
        contentMedium: Uri,
        album: String
    ): ArrayList<AudioContent> {
        val albumAudios = ArrayList<AudioContent>()
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.query(
                contentMedium, audioProjections,
                Audio.Media.ALBUM_ID + " like ? ", arrayOf("%$album%"),
                "LOWER (" + Audio.Media.TITLE + ") ASC"
            )!! //"LOWER ("+Audio.Media.TITLE + ") ASC"
        } else {
            context.contentResolver.query(
                contentMedium, audioProjectionsBelowQ,
                Audio.Media.ALBUM_ID + " like ? ", arrayOf("%$album%"),
                "LOWER (" + Audio.Media.TITLE + ") ASC"
            )!! //"LOWER ("+Audio.Media.TITLE + ") ASC"
        }

        when {
            cursor.moveToFirst() -> {
                do {
                    val audio = AudioContent()
                    audio.filePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))
                    audio.name =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
                    audio.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

                    val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media._ID))
                    audio.musicId = id

                    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                    audio.musicUri = contentUri.toString()

                    audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
                    audio.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
                    audio.duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
                    audio.dateModified =
                        Date(
                            TimeUnit.SECONDS.toMillis(
                                cursor.getLong(
                                    cursor.getColumnIndexOrThrow(
                                        Audio.Media.DATE_MODIFIED
                                    )
                                )
                            )
                        )

                    val albumId: Long =
                        cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
                    val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                    val arUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())
                    audio.artUri = arUri.toString()

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
                    albumAudios.add(audio)
                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        return albumAudios
    }

    fun getBuckets(
        context: Context,
        contentMedium: Uri
    ): ArrayList<AudioBucketContent> {
        val buckets = ArrayList<AudioBucketContent>()
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

        when {
            cursor.moveToFirst() -> {
                do {
                    val audioBucket = AudioBucketContent()
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                            val bucketIdOrPath =
                                cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.BUCKET_ID))
                            val folderNameQ =
                                cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.BUCKET_DISPLAY_NAME))
                            val dataPath: String =
                                cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))

                            var folderPath =
                                dataPath.substring(0, dataPath.lastIndexOf("$folderNameQ/"))
                            folderPath = "$folderPath$folderNameQ/"

                            audioBucket.bucketPath = folderPath
                            audioBucket.bucketId = bucketIdOrPath
                            audioBucket.bucketName = folderNameQ

                            when {
                                !bucketIdsOrPaths.contains(bucketIdOrPath) -> {
                                    bucketIdsOrPaths.add(bucketIdOrPath)
                                    val folderAudios = getBucketAudios(
                                        context,
                                        contentMedium,
                                        bucketIdOrPath,
                                        "id"
                                    )
                                    audioBucket.audios = folderAudios
                                    buckets.add(audioBucket)
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

                            audioBucket.bucketPath = folderPath
                            audioBucket.bucketName = folderName

                            when {
                                !bucketIdsOrPaths.contains(folderPath) -> {
                                    bucketIdsOrPaths.add(folderPath)
                                    val folderAudios =
                                        getBucketAudios(context, contentMedium, folderPath, "path")
                                    audioBucket.audios = folderAudios
                                    buckets.add(audioBucket)
                                }
                            }
                        }
                    }
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return buckets
    }

    @SuppressLint("InlinedApi")
    fun getBucketAudios(
        context: Context,
        contentMedium: Uri,
        bucketIdOrPath: String,
        type: String
    ): ArrayList<AudioContent> {
        val audios = ArrayList<AudioContent>()
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

        when (type) {
            "id" -> {
                when {
                    cursor.moveToFirst() -> {
                        do {
                            val folderAudio = AudioContent()
                            val bucketId =
                                cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.BUCKET_ID))

                            if (bucketId == bucketIdOrPath) {
                                folderAudio.filePath =
                                    cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))
                                folderAudio.name =
                                    cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
                                folderAudio.title =
                                    cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

                                val id: Long =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media._ID))
                                folderAudio.musicId = id

                                val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                                folderAudio.musicUri = contentUri.toString()

                                folderAudio.musicSize =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
                                folderAudio.album =
                                    cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
                                folderAudio.duration =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
                                folderAudio.dateModified =
                                    Date(
                                        TimeUnit.SECONDS.toMillis(
                                            cursor.getLong(
                                                cursor.getColumnIndexOrThrow(
                                                    Audio.Media.DATE_MODIFIED
                                                )
                                            )
                                        )
                                    )

                                val albumId: Long =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
                                val sArtworkUri =
                                    Uri.parse("content://media/external/audio/albumart")

                                val arUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())
                                folderAudio.artUri = arUri.toString()

                                folderAudio.artist =
                                    cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

                                var genreVolume = ""
                                if (contentMedium == externalAudioContent) {
                                    genreVolume = "external"
                                } else if (contentMedium == internalAudioContent) {
                                    genreVolume = "internal"
                                }
                                folderAudio.genre = getGenre(
                                    cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID)),
                                    genreVolume,
                                    context
                                )
                                audios.add(folderAudio)
                            }
                        } while (cursor.moveToNext())
                    }
                }
            }
            "path" -> {
                when {
                    cursor.moveToFirst() -> {
                        do {
                            val folderAudio = AudioContent()
                            val dataPath: String =
                                cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))
                            val path = File(dataPath)
                            val parent = File(path.parent!!)
                            val folderName = parent.name

                            var folderPath =
                                dataPath.substring(0, dataPath.lastIndexOf("$folderName/"))
                            folderPath = "$folderPath$folderName/"

                            if (folderPath == bucketIdOrPath) {
                                folderAudio.filePath = dataPath
                                folderAudio.name =
                                    cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
                                folderAudio.title =
                                    cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

                                val id: Long =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media._ID))
                                folderAudio.musicId = id

                                val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                                folderAudio.musicUri = contentUri.toString()

                                folderAudio.musicSize =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
                                folderAudio.album =
                                    cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
                                folderAudio.duration =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
                                folderAudio.dateModified =
                                    Date(
                                        TimeUnit.SECONDS.toMillis(
                                            cursor.getLong(
                                                cursor.getColumnIndexOrThrow(
                                                    Audio.Media.DATE_MODIFIED
                                                )
                                            )
                                        )
                                    )

                                val albumId: Long =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
                                val sArtworkUri =
                                    Uri.parse("content://media/external/audio/albumart")

                                val artUri =
                                    Uri.withAppendedPath(sArtworkUri, albumId.toString())
                                folderAudio.artUri = artUri.toString()

                                folderAudio.artist =
                                    cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

                                var genreVolume = ""
                                if (contentMedium == externalAudioContent) {
                                    genreVolume = "external"
                                } else if (contentMedium == internalAudioContent) {
                                    genreVolume = "internal"
                                }

                                folderAudio.genre = getGenre(
                                    cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID)),
                                    genreVolume,
                                    context
                                )
                                audios.add(folderAudio)
                            }
                        } while (cursor.moveToNext())
                    }
                }
            }
        }

        cursor.close()
        return audios
    }

    fun getArtists(
        context: Context,
        contentMedium: Uri
    ): ArrayList<AudioArtistContent> {
        val artists = ArrayList<AudioArtistContent>()
        val artistNames = ArrayList<String>()
        val cursor = context.contentResolver.query(
            contentMedium, artistProjection, audioSelection, null,
            "LOWER (" + Audio.Artists.ARTIST + ") ASC"
        )!!

        when {
            cursor.moveToFirst() -> {
                do {
                    val artist = AudioArtistContent()
                    val artistName: String =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Artists.ARTIST))
                    when {
                        !artistNames.contains(artistName) -> {
                            artistNames.add(artistName)
                            artist.artistName = artistName
                            artist.albums = getArtistAlbums(context, contentMedium, artistName)
                            artists.add(artist)
                        }
                    }
                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        return artists
    }

    fun getArtistAlbums(
        context: Context,
        contentMedium: Uri,
        artistName: String
    ): ArrayList<AudioAlbumContent> {
        val artistAlbums = ArrayList<AudioAlbumContent>()
        val albumIds = ArrayList<String>()
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.query(
                contentMedium, audioProjections,
                Audio.Artists.ARTIST + " like ? ",
                arrayOf("%$artistName%"),
                "LOWER (" + Audio.Artists.ARTIST + ") ASC"
            )!!
        } else {
            context.contentResolver.query(
                contentMedium, audioProjectionsBelowQ,
                Audio.Artists.ARTIST + " like ? ",
                arrayOf("%$artistName%"),
                "LOWER (" + Audio.Artists.ARTIST + ") ASC"
            )!!
        }

        when {
            cursor.moveToFirst() -> {
                do {
                    val album = AudioAlbumContent()
                    val audio = AudioContent()
                    audio.filePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))
                    audio.name =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
                    audio.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

                    val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media._ID))
                    audio.musicId = id

                    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                    audio.musicUri = contentUri.toString()

                    audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
                    audio.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
                    audio.duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
                    audio.dateModified =
                        Date(
                            TimeUnit.SECONDS.toMillis(
                                cursor.getLong(
                                    cursor.getColumnIndexOrThrow(
                                        Audio.Media.DATE_MODIFIED
                                    )
                                )
                            )
                        )

                    val albumId: String =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
                    val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                    val artUri = Uri.withAppendedPath(sArtworkUri, albumId)
                    audio.artUri = artUri.toString()

                    audio.artist =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

                    var genreVolume = ""
                    if (contentMedium == externalAudioContent) {
                        genreVolume = "external"
                    } else if (contentMedium == internalAudioContent) {
                        genreVolume = "internal"
                    }

                    val albumName =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
                    val albumArtist =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))
                    /*val albumArtist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                     cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ARTIST))
                    } else {
                     cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
                    }*/

                    audio.genre =
                        getGenre(
                            cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID)),
                            genreVolume,
                            context
                        )

                    when {
                        !albumIds.contains(albumId) -> {
                            albumIds.add(albumId)
                            album.albumId = albumId
                            album.albumName = albumName
                            album.albumArtist = albumArtist
                            album.albumArtUri = artUri.toString()
                            album.albumAudios.add(audio)
                            artistAlbums.add(album)
                        }
                        else -> {
                            artistAlbums.forEach { albumX ->
                                when (albumX.albumId) {
                                    albumId -> {
                                        albumX.albumAudios.add(audio)
                                    }
                                }
                            }
                        }
                    }

                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        return artistAlbums
    }

    fun getGenres(
        context: Context,
        contentMedium: Uri
    ): ArrayList<AudioGenreContent> {
        val genres = ArrayList<AudioGenreContent>()
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

        when {
            cursor.moveToFirst() -> {
                do {
                    val audioGenre = AudioGenreContent()
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

                    if (!genreNames.contains(genre)) {
                        genreNames.add(genre)
                        audioGenre.genreName = genre
                        audioGenre.genreId = genreId
                        audioGenre.audios =
                            getGenreAudios(context, contentMedium, genreVolume, genreId, genre)
                        genres.add(audioGenre)
                    }
                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        return genres
    }

    fun getGenreAudios(
        context: Context,
        contentMedium: Uri,
        volume: String,
        genreId: String,
        genreName: String
    ): ArrayList<AudioContent> {
        val audios = ArrayList<AudioContent>()
        when {
            genreId.trim().isEmpty() -> {
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

                when {
                    cursor.moveToFirst() -> {
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
                            audio.dateModified =
                                Date(
                                    TimeUnit.SECONDS.toMillis(
                                        cursor.getLong(
                                            cursor.getColumnIndexOrThrow(
                                                Audio.Media.DATE_MODIFIED
                                            )
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

                            val genre = getGenre(id.toInt(), genreVolume, context)
                            audio.genre = genre
                            if (genre == "unknown") {
                                audios.add(audio)
                            }
                        } while (cursor.moveToNext())
                    }
                }
                cursor.close()
            }
            else -> {
                val genreAudiosUri =
                    Audio.Genres.Members.getContentUri("external", genreId.toLong())

                val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.query(
                        genreAudiosUri, audioProjections, null, null,
                        "LOWER (" + Audio.Media.TITLE + ") ASC"
                    )!!
                } else {
                    context.contentResolver.query(
                        genreAudiosUri, audioProjectionsBelowQ, null, null,
                        "LOWER (" + Audio.Media.TITLE + ") ASC"
                    )!!
                }

                when {
                    cursor.moveToFirst() -> {
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
                            audio.dateModified =
                                Date(
                                    TimeUnit.SECONDS.toMillis(
                                        cursor.getLong(
                                            cursor.getColumnIndexOrThrow(
                                                Audio.Media.DATE_MODIFIED
                                            )
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
                            audio.genre = genreName

                            audios.add(audio)

                        } while (cursor.moveToNext())
                    }
                }
                cursor.close()
            }
        }
        return audios
    }

    fun searchAudios(
        context: Context,
        contentMedium: Uri,
        selectionType: String,
        selectionValue: String
    ): ArrayList<AudioContent> {
        val foundAudios = ArrayList<AudioContent>()
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

        when {
            cursor.moveToFirst() -> {
                do {
                    val audio = AudioContent()
                    audio.filePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))
                    audio.name =
                        cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
                    audio.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

                    val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media._ID))
                    audio.musicId = id

                    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
                    audio.musicUri = contentUri.toString()

                    audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
                    audio.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
                    audio.duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
                    audio.dateModified =
                        Date(
                            TimeUnit.SECONDS.toMillis(
                                cursor.getLong(
                                    cursor.getColumnIndexOrThrow(
                                        Audio.Media.DATE_MODIFIED
                                    )
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

                    audio.genre =
                        getGenre(
                            cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID)),
                            genreVolume,
                            context
                        )
                    foundAudios.add(audio)
                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        return foundAudios
    }

    fun getGenre(
        media_id: Int,
        volumeName: String,
        context: Context
    ): String {
        val uri = Audio.Genres.getContentUriForAudioId(volumeName, media_id)
        val genresCursor: Cursor =
            context.contentResolver.query(uri, genresProj, null, null, null)!!
        var genre = ""
        while (genresCursor.moveToNext()) {
            genre = genresCursor.getString(genresCursor.getColumnIndexOrThrow(Audio.Genres.NAME))
        }
        genresCursor.close()
        if (genre.trim().isEmpty()) {
            genre = "unknown"
        }
        return genre
    }

}