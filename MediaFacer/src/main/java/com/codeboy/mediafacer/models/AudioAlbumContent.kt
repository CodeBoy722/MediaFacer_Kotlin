package com.codeboy.mediafacer.models

import android.net.Uri
import java.io.Serializable

class AudioAlbumContent: Serializable {

    var albumName: String = ""
    var albumId: String = ""
    var albumArtUri: String = ""
    var albumArtist: String = ""
    var albumAudios: ArrayList<AudioContent> = ArrayList()
    var albumSize = albumAudios.size

}