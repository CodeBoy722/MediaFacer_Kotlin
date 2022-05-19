package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.AudioGenreContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class AudioGenreViewModel {

    var audioGenres: MutableLiveData<ArrayList<AudioGenreContent>> = MutableLiveData()
    private var audioGenreList = ArrayList<AudioGenreContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
        CoroutineScope(Dispatchers.Main).async {
            audioGenreList.addAll(
                MediaFacer()
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .getGenres(context, MediaFacer.externalAudioContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post {
                    audioGenres.value = audioGenreList
                }
        }
    }
    
}