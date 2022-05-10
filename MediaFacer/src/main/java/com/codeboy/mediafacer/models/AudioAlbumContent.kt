package com.codeboy.mediafacer.models

import android.net.Uri

class AudioAlbumContent {

    var albumName: String = ""
    var albumId: String = ""
    var albumArtUri: Uri = Uri.EMPTY
    var albumArtist: String = ""
    var albumAudios: ArrayList<AudioContent> = ArrayList()
    var albumSize = albumAudios.size

}