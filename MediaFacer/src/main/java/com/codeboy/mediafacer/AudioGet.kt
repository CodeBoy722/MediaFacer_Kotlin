package com.codeboy.mediafacer

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.codeboy.mediafacer.MediaFacer.Companion.externalAudioContent
import com.codeboy.mediafacer.MediaFacer.Companion.internalAudioContent
import com.codeboy.mediafacer.models.AudioAlbumContent
import com.codeboy.mediafacer.models.AudioContent
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


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

 val searchSelectionTypeAlbum: String
  get() = MediaStore.Audio.Media.ALBUM

 val searchSelectionTypeArtist: String
  get() = MediaStore.Audio.Media.ARTIST

 val searchSelectionTypeTitle: String
  get() = MediaStore.Audio.Media.TITLE

 fun getAudios(context: Context, contentMedium: Uri): ArrayList<AudioContent>{
  val allAudio: ArrayList<AudioContent> = ArrayList()
  val audioSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
  val cursor = context.contentResolver.query(contentMedium
   ,audioProjections
   ,audioSelection,
   null,
   "LOWER (" + MediaStore.Audio.Media.TITLE + ") ASC")!! //"LOWER ("+MediaStore.Audio.Media.TITLE + ") ASC"
  //try {
   if (cursor.moveToFirst()) {
    do {
     val audioContent = AudioContent()

     audioContent.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))

     audioContent.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))

     val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
     audioContent.musicId = id

     val contentUri = Uri.withAppendedPath(contentMedium, id.toString())
     audioContent.musicUri = contentUri.toString()

     audioContent.musicSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))

     audioContent.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))

     audioContent.duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))

     audioContent.dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))))

     val albumId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
     val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
     audioContent.artUri = Uri.withAppendedPath(sArtworkUri, albumId.toString())

     audioContent.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))

     try{
      audioContent.composer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER))
     }catch (ex: Exception){ex.printStackTrace()}

     var genreVolume = ""
     if(contentMedium == externalAudioContent){
      genreVolume = "external"
     }else if(contentMedium == internalAudioContent){
      genreVolume = "internal"
     }

     audioContent.genre = getGenre(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)), genreVolume, context)
     allAudio.add(audioContent)
    } while (cursor.moveToNext())
   }

 /* }catch (e: Exception){
   e.printStackTrace()
  }*/
  cursor.close()
  return allAudio
 }

 fun getAudioAlbums(context: Context, contentMedium: Uri): ArrayList<AudioAlbumContent>{
  val albums = ArrayList<AudioAlbumContent>()

  return albums
 }

 fun getAlbumAudios(context: Context, contentMedium: Uri, album: String):ArrayList<AudioContent>{
  val audios = ArrayList<AudioContent>()

  return audios
 }

 //this method does not support pagination
 fun getAbsoluteAudioAlbums(context: Context, contentMedium: Uri): ArrayList<AudioAlbumContent>{
  val albums = ArrayList<AudioAlbumContent>()

  return albums
 }

 fun getAudioArtist(context: Context, contentMedium: Uri){

 }

 fun getAudioBuckets(context: Context, contentMedium: Uri){

 }

 fun getAudioGenres(context: Context, contentMedium: Uri){

 }

 fun searchAudios(context: Context, contentMedium: Uri, selectionType: String, selectionValue: String): ArrayList<AudioContent>{
  val foundAudios = ArrayList<AudioContent>()
  val cursor = context.contentResolver.query(contentMedium, audioProjections,
   "$selectionType like ? ",
   arrayOf("%$selectionValue%"),
   "LOWER (" + MediaStore.Audio.Media.TITLE + ") ASC")!!

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