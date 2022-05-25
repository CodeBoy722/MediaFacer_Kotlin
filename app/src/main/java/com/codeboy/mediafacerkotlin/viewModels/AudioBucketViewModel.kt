package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.AudioBucketContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class AudioBucketViewModel: ViewModel() {

    var audioBuckets: MutableLiveData<ArrayList<AudioBucketContent>> = MutableLiveData()
    private var audiosList = ArrayList<AudioBucketContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
        CoroutineScope(Dispatchers.IO).async {
            audiosList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .getBuckets(context, MediaFacer.externalAudioContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    audioBuckets.value = audiosList
                }
        }
    }


}