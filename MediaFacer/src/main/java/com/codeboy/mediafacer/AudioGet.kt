package com.codeboy.mediafacer

import android.annotation.SuppressLint
import android.provider.MediaStore


internal interface AudioGet {

 val audioProjections: Array<String>
  get() = arrayOf(
   MediaStore.Audio.Media.TITLE,
   MediaStore.Audio.Media.DISPLAY_NAME,
   MediaStore.Audio.Media.ARTIST,
   MediaStore.Audio.Media.ARTIST_ID,
   MediaStore.Audio.Media.ALBUM,
   MediaStore.Audio.Media.ALBUM_ID,
   MediaStore.Audio.Media.COMPOSER,
   MediaStore.Audio.Media.SIZE,
   MediaStore.Audio.Media._ID,
   MediaStore.Audio.Media.DURATION,
   MediaStore.Audio.Media.BUCKET_ID
  )

 fun getAudios(){

 }

 fun getAudioAlbums(){

 }

 fun getAudioArtist(){

 }

 fun getAudioBuckets(){

 }

 fun getAudioGenres(){

 }

}