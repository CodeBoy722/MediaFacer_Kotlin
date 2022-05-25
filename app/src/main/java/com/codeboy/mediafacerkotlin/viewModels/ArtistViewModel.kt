package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.AudioArtistContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class ArtistViewModel {
    var audioArtists: MutableLiveData<ArrayList<AudioArtistContent>> = MutableLiveData()
    private var audioArtistList = ArrayList<AudioArtistContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
        CoroutineScope(Dispatchers.IO).async {
            audioArtistList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .getArtists(context, MediaFacer.externalAudioContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post {
                    audioArtists.value = audioArtistList
                }
        }
    }

}