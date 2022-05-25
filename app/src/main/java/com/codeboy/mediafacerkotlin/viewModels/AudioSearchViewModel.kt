package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.AudioContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class AudioSearchViewModel: ViewModel() {

    var audios: MutableLiveData<ArrayList<AudioContent>> = MutableLiveData()
    var audiosList = ArrayList<AudioContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean,
                     selectionType: String, selectionValue: String){
        CoroutineScope(Dispatchers.IO).async {
            audiosList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .searchAudios(context, MediaFacer.externalAudioContent,selectionType,selectionValue)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post {
                    audios.value = audiosList
                }
        }
    }

}