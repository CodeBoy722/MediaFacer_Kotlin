package com.codeboy.mediafacer.models

class AudioArtistContent {

    var artistName: String = ""
    var albums = ArrayList<AudioAlbumContent>()

    fun musicCount(): Int{
        var count = 0
        albums.forEach { album: AudioAlbumContent ->
            count += album.albumAudios.size
        }
        return count
    }

}