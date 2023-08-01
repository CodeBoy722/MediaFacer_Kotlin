package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.AudioArtistContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class ArtistViewModel {
    private val  _audioArtists: MutableLiveData<ArrayList<AudioArtistContent>> = MutableLiveData()
    val audioArtists: LiveData<ArrayList<AudioArtistContent>> = _audioArtists
    private var audioArtistList = ArrayList<AudioArtistContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int){
        CoroutineScope(Dispatchers.IO).async {
            audioArtistList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit)
                    .getArtists(context, MediaFacer.externalAudioContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post {
                    _audioArtists.value = audioArtistList
                }
        }
    }

}