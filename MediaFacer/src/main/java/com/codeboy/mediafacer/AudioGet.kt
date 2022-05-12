package com.codeboy.mediafacer

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore.Audio
import com.codeboy.mediafacer.MediaFacer.Companion.externalAudioContent
import com.codeboy.mediafacer.MediaFacer.Companion.internalAudioContent
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
   Audio.Media.COMPOSER,
   Audio.Media.SIZE,
   Audio.Media._ID,
   Audio.Media.DURATION,
   Audio.Media.BUCKET_ID,
   Audio.Media.DATE_MODIFIED)

 val artistProjection: Array<String>
  get() = arrayOf(Audio.Media.ARTIST,
   Audio.Artists.ARTIST)

 val searchSelectionTypeAlbum: String
  get() = Audio.Media.ALBUM

 val searchSelectionTypeArtist: String
  get() = Audio.Media.ARTIST

 val searchSelectionTypeTitle: String
  get() = Audio.Media.TITLE

 val audioSelection: String
  get() = Audio.Media.IS_MUSIC + " != 0"

 val genresProj: Array<String>
  get() = arrayOf(
   Audio.Genres.NAME,
   Audio.Genres._ID)

 fun getAudios(context: Context, contentMedium: Uri): ArrayList<AudioContent> {
  val allAudio: ArrayList<AudioContent> = ArrayList()
  val cursor = context.contentResolver.query(contentMedium, audioProjections, audioSelection, null,
   "LOWER (" + Audio.Media.TITLE + ") ASC")!! //"LOWER ("+Audio.Media.TITLE + ") ASC"
  //try {
  when {
      cursor.moveToFirst() -> {
       do {
        val audio = AudioContent()
        audio.name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
        audio.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

        val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID))
        audio.musicId = id

        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
        audio.musicUri = contentUri.toString()

        audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
        audio.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
        audio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
        audio.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DATE_MODIFIED))))

        val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        audio.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

        audio.artist = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

        var genreVolume = ""
        if (contentMedium == externalAudioContent) {
         genreVolume = "external"
        } else if (contentMedium == internalAudioContent) {
         genreVolume = "internal"
        }

        audio.genre = getGenre(id, genreVolume, context)
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
   "LOWER (" + Audio.Media.DATE_MODIFIED + ") ASC")!!

  //try {
  when {
      cursor.moveToFirst() -> {
       do {
        val albumId = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))

        if (!albumIds.contains(albumId)) {
         val album = AudioAlbumContent()
         val albumName = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
         album.albumName = albumName

         val albumArtist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ARTIST))
         } else {
          cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
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
   Audio.Media.ALBUM_ID + " like ? ", arrayOf("%$album%"),
   "LOWER (" + Audio.Media.TITLE + ") ASC")!! //"LOWER ("+Audio.Media.TITLE + ") ASC"

  //try {
  when {
      cursor.moveToFirst() -> {
       do {
        val audio = AudioContent()
        audio.name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
        audio.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

        val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID))
        audio.musicId = id

        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
        audio.musicUri = contentUri.toString()

        audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
        audio.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
        audio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
        audio.dateModified =
         Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DATE_MODIFIED))))

        val albumId: Long =
         cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        audio.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

        audio.artist = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

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
   "LOWER (" + Audio.Media.TITLE + ") ASC")!!

  //try {
  when {
      cursor.moveToFirst() -> {
       do {
        val audioBucket = AudioBucketContent()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
             val bucketIdOrPath = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.BUCKET_ID))
             val folderNameQ = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.BUCKET_DISPLAY_NAME))

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
          val dataPath: String = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))
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
   "LOWER (" + Audio.Media.TITLE + ") ASC")!!

  try {
   when {
       cursor.moveToFirst() -> {
        do {
         val folderAudio = AudioContent()
         when (type) {
             "id" -> {
              val bucketId = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.BUCKET_ID))
              if(bucketId == bucketIdOrPath){
               folderAudio.name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
               folderAudio.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

               val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID))
               folderAudio.musicId = id

               val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
               folderAudio.musicUri = contentUri.toString()

               folderAudio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
               folderAudio.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
               folderAudio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
               folderAudio.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DATE_MODIFIED))))

               val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
               val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
               folderAudio.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

               folderAudio.artist = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

               var genreVolume = ""
               if (contentMedium == externalAudioContent) {
                genreVolume = "external"
               } else if (contentMedium == internalAudioContent) {
                genreVolume = "internal"
               }

               folderAudio.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID)), genreVolume, context)
              }
             }
             "path" -> {
              val dataPath: String = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DATA))
              val path = File(dataPath)
              val parent = File(path.parent!!)
              val folderName = parent.name

              if(folderName == bucketIdOrPath){
               folderAudio.name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
               folderAudio.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

               val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID))
               folderAudio.musicId = id

               val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
               folderAudio.musicUri = contentUri.toString()

               folderAudio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
               folderAudio.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
               folderAudio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
               folderAudio.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DATE_MODIFIED))))

               val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
               val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
               folderAudio.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

               folderAudio.artist = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

               var genreVolume = ""
               if (contentMedium == externalAudioContent) {
                genreVolume = "external"
               } else if (contentMedium == internalAudioContent) {
                genreVolume = "internal"
               }

               folderAudio.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID)), genreVolume, context)
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
   "LOWER (" + Audio.Artists.ARTIST + ") ASC")!!

  try {
   when {
       cursor.moveToFirst() -> {
        do {
         val artist = AudioArtistContent()
         val artistName: String = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Artists.ARTIST))
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
   Audio.Artists.ARTIST + " like ? ",
   arrayOf("%$artistName%"),
   "LOWER (" + Audio.Artists.ARTIST + ") ASC")!!

  when {
      cursor.moveToFirst() -> {
       do {
        val album = AudioAlbumContent()
        val audio = AudioContent()
        audio.name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
        audio.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

        val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID))
        audio.musicId = id

        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
        audio.musicUri = contentUri.toString()

        audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
        audio.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
        audio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
        audio.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DATE_MODIFIED))))

        val albumId: String = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        val artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())
        audio.artUri = artUri

        audio.artist = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

        var genreVolume = ""
        if (contentMedium == externalAudioContent) {
         genreVolume = "external"
        } else if (contentMedium == internalAudioContent) {
         genreVolume = "internal"
        }

        val albumName = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
        val albumArtist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
         cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ARTIST))
        } else {
         cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
        }

        audio.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID)), genreVolume, context)

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

 fun getGenres(context: Context, contentMedium: Uri): ArrayList<AudioGenreContent> {
  val genres = ArrayList<AudioGenreContent>()
  val genreNames = ArrayList<String>()
  val cursor = context.contentResolver
   .query(contentMedium, audioProjections, audioSelection, null,
    "LOWER (" + Audio.Media.TITLE + ") ASC")!!

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
        val genresCursor: Cursor = context.contentResolver.query(uri, genresProj, null, null, null)!!
        while (genresCursor.moveToNext()) {
         genre = genresCursor.getString(genresCursor.getColumnIndexOrThrow(Audio.Genres.NAME))
         genreId = genresCursor.getString(genresCursor.getColumnIndexOrThrow(Audio.Genres._ID))
        }
        genresCursor.close()
        when {
            genre.trim().isEmpty() -> {
             genre = "unknown"
            }
        }

        if(!genreNames.contains(genre)){
         genreNames.add(genre)
         audioGenre.genreName = genre
         audioGenre.genreId = genreId
         audioGenre.audios = getGenreAudios(context,contentMedium,genreVolume,genreId,genre)
         genres.add(audioGenre)
        }
       }while (cursor.moveToNext())
      }
  }

  cursor.close()
  return genres
 }

 fun getGenreAudios(context: Context, contentMedium: Uri, volume: String, genreId: String, genreName: String): ArrayList<AudioContent>{
  val audios = ArrayList<AudioContent>()
  val genreAudiosUri = Audio.Genres.Members.getContentUri("external", genreId.toLong())
  val cursor = context.contentResolver.query(genreAudiosUri, audioProjections, null, null,
   "LOWER (" + Audio.Media.TITLE + ") ASC")!!

  if(cursor.moveToFirst()){
   do {

    val audio = AudioContent()
    audio.name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
    audio.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

    val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID))
    audio.musicId = id

    val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
    audio.musicUri = contentUri.toString()

    audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
    audio.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
    audio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
    audio.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DATE_MODIFIED))))

    val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
    val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
    audio.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

    audio.artist = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))
    audio.genre = genreName

    audios.add(audio)

   }while (cursor.moveToNext())
  }

  cursor.close()
  return audios
 }

 fun searchAudios(context: Context, contentMedium: Uri, selectionType: String, selectionValue: String): ArrayList<AudioContent> {
  val foundAudios = ArrayList<AudioContent>()
  val cursor = context.contentResolver.query(contentMedium, audioProjections,
   "$selectionType like ? ",
   arrayOf("%$selectionValue%"),
   "LOWER (" + Audio.Media.TITLE + ") ASC")!!

  when {
      cursor.moveToFirst() -> {
       do {
        val audio = AudioContent()
        audio.name = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.DISPLAY_NAME))
        audio.title = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.TITLE))

        val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID))
        audio.musicId = id

        val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
        audio.musicUri = contentUri.toString()

        audio.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.SIZE))
        audio.album = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM))
        audio.duration = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DURATION))
        audio.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.DATE_MODIFIED))))

        val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(Audio.Media.ALBUM_ID))
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        audio.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

        audio.artist = cursor.getString(cursor.getColumnIndexOrThrow(Audio.Media.ARTIST))

        var genreVolume = ""
        if (contentMedium == externalAudioContent) {
         genreVolume = "external"
        } else if (contentMedium == internalAudioContent) {
         genreVolume = "internal"
        }

        audio.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(Audio.Media._ID)), genreVolume, context)
        foundAudios.add(audio)
       }while (cursor.moveToNext())
      }
  }

  cursor.close()
  return foundAudios
 }

 fun getGenre(media_id: Int, volumeName: String, context: Context): String {
  val uri = Audio.Genres.getContentUriForAudioId(volumeName, media_id)
  val genresCursor: Cursor = context.contentResolver.query(uri, genresProj, null, null, null)!!
  var genre = ""
  while (genresCursor.moveToNext()) {
   genre = genresCursor.getString(genresCursor.getColumnIndexOrThrow(Audio.Genres.NAME))
  }
  genresCursor.close()
  if(genre.trim().isEmpty()){
   genre = "unknown"
  }
  return genre
 }


}