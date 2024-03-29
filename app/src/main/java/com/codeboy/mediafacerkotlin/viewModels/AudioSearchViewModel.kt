package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.AudioContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class AudioSearchViewModel: ViewModel() {

    private val _audios: MutableLiveData<ArrayList<AudioContent>> = MutableLiveData()
    val audios: LiveData<ArrayList<AudioContent>> = _audios
    var audiosList = ArrayList<AudioContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, selectionType: String, selectionValue: String){
        CoroutineScope(Dispatchers.IO).async {
            audiosList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit)
                    .searchAudios(context, MediaFacer.externalAudioContent,selectionType,selectionValue)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post {
                    _audios.value = audiosList
                }
        }
    }

}