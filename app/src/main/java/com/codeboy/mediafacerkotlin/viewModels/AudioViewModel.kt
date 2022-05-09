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

class AudioViewModel : ViewModel() {

    var audios: MutableLiveData<ArrayList<AudioContent>> = MutableLiveData()
    private var audiosList = ArrayList<AudioContent>()

     fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
         //can also use launch
         //Dispatchers.Main:
         // the recommended dispatcher for performing UI-related events such as showing lists in a RecyclerView, updating view
        CoroutineScope(Dispatchers.Main).async {
            audiosList.addAll(
                MediaFacer()
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .getAudios(context, MediaFacer.externalAudioContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post {
                    audios.value = audiosList
                }
        }
     }


}