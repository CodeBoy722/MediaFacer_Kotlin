package com.codeboy.mediafacer.models

import java.io.Serializable

class AudioGenreContent: Serializable {

    var audios = ArrayList<AudioContent>()
    var genreName: String = ""
    var genreId: String = ""
    var numOfSongs = audios.size

}