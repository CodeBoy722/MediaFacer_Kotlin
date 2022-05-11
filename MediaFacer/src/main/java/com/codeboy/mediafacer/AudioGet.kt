package com.codeboy.mediafacer

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.codeboy.mediafacer.MediaFacer.Companion.externalAudioContent
import com.codeboy.mediafacer.MediaFacer.Companion.internalAudioContent
import com.codeboy.mediafacer.models.AudioAlbumContent
import com.codeboy.mediafacer.models.AudioArtistContent
import com.codeboy.mediafacer.models.AudioBucketContent
import com.codeboy.mediafacer.models.AudioContent
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

internal interface AudioGet {

 val audioProjections: Array<String>
  @SuppressLint("InlinedApi")
  get() = arrayOf(
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

 val artistProjection: Array<String>
  get() = arrayOf(MediaStore.Audio.Media.ARTIST,
   MediaStore.Audio.Artists.ARTIST)

 val searchSelectionTypeAlbum: String
  get() = MediaStore.Audio.Media.ALBUM

 val searchSelectionTypeArtist: String
  get() = MediaStore.Audio.Media.ARTIST

 val searchSelectionTypeTitle: String
  get() = MediaStore.Audio.Media.TITLE

 val audioSelection: String
  get() = MediaStore.Audio.Media.IS_MUSIC + " != 0"

 fun getAudios(context: Context, contentMedium: Uri): ArrayList<AudioContent> {
  val allAudio: ArrayList<AudioContent> = ArrayList()
  val cursor = context.contentResolver.query(contentMedium, audioProjections, audioSelection, null,
   "LOWER (" + MediaStore.Audio.Media.TITLE + ") ASC")!! //"LOWER ("+MediaStore.Audio.Media.TITLE + ") ASC"
  //try {
  when {
      cursor.moveToFirst() -> {
       do {
        val audio = AudioContent()
        audio.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
        audio.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))

        val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
        audio.musicId = id

        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
        audio.musicUri = contentUri.toString()

        audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
        audio.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        audio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
        audio.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))))

        val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        audio.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

        audio.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
        try {
         audio.composer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER))
        } catch (ex: Exception) {
         ex.printStackTrace()
        }

        var genreVolume = ""
        if (contentMedium == externalAudioContent) {
         genreVolume = "external"
        } else if (contentMedium == internalAudioContent) {
         genreVolume = "internal"
        }

        audio.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)), genreVolume, context)
        allAudio.add(audio)
       } while (cursor.moveToNext())
      }
  }

  /* }catch (e: Exception){
    e.printStackTrace()
   }*/
  cursor.close()
  return allAudio
 }

 fun getAlbums(context: Context, contentMedium: Uri): ArrayList<AudioAlbumContent> {
  val albums = ArrayList<AudioAlbumContent>()
  val albumIds = ArrayList<String>()
  val cursor = context.contentResolver.query(
   contentMedium,
   audioProjections,
   audioSelection,
   null,
   "LOWER (" + MediaStore.Audio.Media.DATE_MODIFIED + ") ASC")!!

  //try {
  when {
      cursor.moveToFirst() -> {
       do {
        val albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

        if (!albumIds.contains(albumId)) {
         val album = AudioAlbumContent()
         val albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
         album.albumName = albumName

         val albumArtist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST))
         } else {
          cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
         }
         album.albumArtist = albumArtist

         albumIds.add(albumId)
         val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
         val imageUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())
         album.albumArtUri = imageUri
         album.albumId = albumId

         val audios = getAlbumAudios(context, contentMedium, albumId)
         album.albumAudios = audios
         albums.add(album)
        }
       } while (cursor.moveToNext())
      }
  }
  /* }catch (ex: Exception){
    ex.printStackTrace()
   }*/
  cursor.close()
  return albums
 }

 fun getAlbumAudios(context: Context, contentMedium: Uri, album: String): ArrayList<AudioContent> {
  val albumAudios = ArrayList<AudioContent>()
  val cursor = context.contentResolver.query(contentMedium, audioProjections,
   MediaStore.Audio.Media.ALBUM_ID + " like ? ", arrayOf("%$album%"),
   "LOWER (" + MediaStore.Audio.Media.TITLE + ") ASC")!! //"LOWER ("+MediaStore.Audio.Media.TITLE + ") ASC"

  //try {
  when {
      cursor.moveToFirst() -> {
       do {
        val audio = AudioContent()
        audio.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
        audio.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))

        val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
        audio.musicId = id

        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
        audio.musicUri = contentUri.toString()

        audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
        audio.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        audio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
        audio.dateModified =
         Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))))

        val albumId: Long =
         cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        audio.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

        audio.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
        try {
         audio.composer =
          cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER))
        } catch (ex: Exception) {
         ex.printStackTrace()
        }

        var genreVolume = ""
        if (contentMedium == externalAudioContent) {
         genreVolume = "external"
        } else if (contentMedium == internalAudioContent) {
         genreVolume = "internal"
        }

        audio.genre = getGenre(
         cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
         genreVolume,
         context
        )
        albumAudios.add(audio)
       } while (cursor.moveToNext())
      }
  }
  /* }catch (ex: java.lang.Exception){
    ex.printStackTrace()
   }*/
  cursor.close()
  return albumAudios
 }

 fun getBuckets(context: Context, contentMedium: Uri): ArrayList<AudioBucketContent> {
  val buckets = ArrayList<AudioBucketContent>()
  val bucketIdsOrPaths = ArrayList<String>()
  val cursor = context.contentResolver.query(contentMedium, audioProjections, audioSelection, null,
   "LOWER (" + MediaStore.Audio.Media.TITLE + ") ASC")!!

  //try {
  when {
      cursor.moveToFirst() -> {
       do {
        val audioBucket = AudioBucketContent()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
             val bucketIdOrPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_ID))
             val folderNameQ = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME))

             audioBucket.bucketId = bucketIdOrPath
             audioBucket.bucketName = folderNameQ

             when {
                 !bucketIdsOrPaths.contains(bucketIdOrPath) -> {
                  val folderAudios = getBucketAudios(context,contentMedium,bucketIdOrPath,"id")
                  audioBucket.audios = folderAudios
                 }
             }
            }
         else -> {
          val dataPath: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
          val path = File(dataPath)
          val parent = File(path.parent!!)
          val folderName = parent.name

          var folderPath = dataPath.substring(0, dataPath.lastIndexOf("$folderName/"))
          folderPath = "$folderPath$folderName/"

          audioBucket.bucketPath = folderPath
          audioBucket.bucketName = folderName

          when {
              !bucketIdsOrPaths.contains(folderPath) -> {
               val folderAudios = getBucketAudios(context,contentMedium,folderPath,"path")
               audioBucket.audios = folderAudios
              }
          }
         }
        }
       } while (cursor.moveToNext())
      }
  }
  /*} catch (ex: Exception) {
   ex.printStackTrace()
  }*/
  cursor.close()
  return buckets
 }

 @SuppressLint("InlinedApi")
 private fun getBucketAudios(context: Context, contentMedium: Uri, bucketIdOrPath: String, type: String): ArrayList<AudioContent> {
  val audios = ArrayList<AudioContent>()
  val cursor = context.contentResolver.query(contentMedium, audioProjections, audioSelection, null,
   "LOWER (" + MediaStore.Audio.Media.TITLE + ") ASC")!!

  try {
   when {
       cursor.moveToFirst() -> {
        do {
         val folderAudio = AudioContent()
         when (type) {
             "id" -> {
              val bucketId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_ID))
              if(bucketId == bucketIdOrPath){
               folderAudio.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
               folderAudio.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))

               val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
               folderAudio.musicId = id

               val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
               folderAudio.musicUri = contentUri.toString()

               folderAudio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
               folderAudio.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
               folderAudio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
               folderAudio.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))))

               val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
               val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
               folderAudio.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

               folderAudio.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
               try {
                folderAudio.composer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER))
               } catch (ex: Exception) {
                ex.printStackTrace()
               }

               var genreVolume = ""
               if (contentMedium == externalAudioContent) {
                genreVolume = "external"
               } else if (contentMedium == internalAudioContent) {
                genreVolume = "internal"
               }

               folderAudio.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)), genreVolume, context)
              }
             }
             "path" -> {
              val dataPath: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
              val path = File(dataPath)
              val parent = File(path.parent!!)
              val folderName = parent.name

              if(folderName == bucketIdOrPath){
               folderAudio.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
               folderAudio.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))

               val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
               folderAudio.musicId = id

               val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
               folderAudio.musicUri = contentUri.toString()

               folderAudio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
               folderAudio.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
               folderAudio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
               folderAudio.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))))

               val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
               val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
               folderAudio.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

               folderAudio.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
               try {
                folderAudio.composer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER))
               } catch (ex: Exception) {
                ex.printStackTrace()
               }

               var genreVolume = ""
               if (contentMedium == externalAudioContent) {
                genreVolume = "external"
               } else if (contentMedium == internalAudioContent) {
                genreVolume = "internal"
               }

               folderAudio.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)), genreVolume, context)
              }
             }
         }
         audios.add(folderAudio)
        }while (cursor.moveToNext())
       }
   }
  }catch (ex: Exception){
   ex.printStackTrace()
  }

  cursor.close()
  return audios
 }

 fun getArtists(context: Context, contentMedium: Uri): ArrayList<AudioArtistContent> {
  val artists = ArrayList<AudioArtistContent>()
  val artistNames = ArrayList<String>()
  val cursor = context.contentResolver.query(contentMedium, artistProjection, audioSelection, null,
   "LOWER (" + MediaStore.Audio.Artists.ARTIST + ") ASC")!!

  try {
   when {
       cursor.moveToFirst() -> {
        do {
         val artist = AudioArtistContent()
         val artistName: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
         when {
             !artistNames.contains(artistName) -> {
              artistNames.add(artistName)
              artist.artistName = artistName
              artist.albums = getArtistAlbums(context,contentMedium,artistName)
              artists.add(artist)
             }
         }
        }while (cursor.moveToNext())
       }
   }
  }catch (ex: Exception){
   ex.printStackTrace()
  }

  cursor.close()
  return artists
 }

 fun getArtistAlbums(context: Context, contentMedium: Uri, artistName: String): ArrayList<AudioAlbumContent>{
  val artistAlbums = ArrayList<AudioAlbumContent>()
  val albumIds = ArrayList<String>()
  val cursor = context.contentResolver.query(contentMedium, audioProjections,
   MediaStore.Audio.Artists.ARTIST + " like ? ",
   arrayOf("%$artistName%"),
   "LOWER (" + MediaStore.Audio.Artists.ARTIST + ") ASC")!!

  when {
      cursor.moveToFirst() -> {
       do {
        val album = AudioAlbumContent()
        val audio = AudioContent()
        audio.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
        audio.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))

        val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
        audio.musicId = id

        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
        audio.musicUri = contentUri.toString()

        audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
        audio.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        audio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
        audio.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))))

        val albumId: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        val artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())
        audio.artUri = artUri

        audio.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
        try {
         audio.composer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER))
        } catch (ex: Exception) {
         ex.printStackTrace()
        }

        var genreVolume = ""
        if (contentMedium == externalAudioContent) {
         genreVolume = "external"
        } else if (contentMedium == internalAudioContent) {
         genreVolume = "internal"
        }

        val albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        val albumArtist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
         cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST))
        } else {
         cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        }

        audio.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)), genreVolume, context)

        when {
            !albumIds.contains(albumId) -> {
             albumIds.add(albumId)
             album.albumId = albumId
             album.albumName = albumName
             album.albumArtist = albumArtist
             album.albumArtUri = artUri
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

       }while (cursor.moveToNext())
      }
  }

  cursor.close()
  return artistAlbums
 }

 fun getAudioGenres(context: Context, contentMedium: Uri) {

 }

 fun searchAudios(context: Context, contentMedium: Uri, selectionType: String, selectionValue: String): ArrayList<AudioContent> {
  val foundAudios = ArrayList<AudioContent>()
  val cursor = context.contentResolver.query(contentMedium, audioProjections,
   "$selectionType like ? ",
   arrayOf("%$selectionValue%"),
   "LOWER (" + MediaStore.Audio.Media.TITLE + ") ASC")!!

  when {
      cursor.moveToFirst() -> {
       do {
        val audio = AudioContent()
        audio.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
        audio.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))

        val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
        audio.musicId = id

        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
        audio.musicUri = contentUri.toString()

        audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
        audio.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        audio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
        audio.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))))

        val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        audio.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

        audio.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
        try {
         audio.composer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER))
        } catch (ex: Exception) {
         ex.printStackTrace()
        }

        var genreVolume = ""
        if (contentMedium == externalAudioContent) {
         genreVolume = "external"
        } else if (contentMedium == internalAudioContent) {
         genreVolume = "internal"
        }

        audio.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)), genreVolume, context)
        foundAudios.add(audio)
       }while (cursor.moveToNext())
      }
  }

  cursor.close()
  return foundAudios
 }

 fun getGenre(media_id: Int, volumeName: String, context: Context): String {
  val genresProj = arrayOf(
   MediaStore.Audio.Genres.NAME,
   MediaStore.Audio.Genres._ID
  )
  val uri = MediaStore.Audio.Genres.getContentUriForAudioId(volumeName, media_id)
  val genresCursor: Cursor = context.contentResolver.query(uri, genresProj, null, null, null)!!
  val genreIndex = genresCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
  var genre = ""
  while (genresCursor.moveToNext()) {
   genre = genresCursor.getString(genreIndex)
  }
  genresCursor.close()
  return genre
 }


}