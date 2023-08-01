package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.AudioBucketContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class AudioBucketViewModel: ViewModel() {

    private val _audioBuckets: MutableLiveData<ArrayList<AudioBucketContent>> = MutableLiveData()
    val audioBuckets: LiveData<ArrayList<AudioBucketContent>> = _audioBuckets
    private var audiosList = ArrayList<AudioBucketContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int){
        CoroutineScope(Dispatchers.IO).async {
            audiosList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit)
                    .getBuckets(context, MediaFacer.externalAudioContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    _audioBuckets.value = audiosList
                }
        }
    }


}