package com.codeboy.mediafacer.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.AudioBucketContent
import com.codeboy.mediafacer.models.AudioContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

internal object AudiosViewModel: ViewModel() {

    private val AudioBuckets: MutableLiveData<ArrayList<AudioBucketContent>> = MutableLiveData()
    val audioBuckets: LiveData<ArrayList<AudioBucketContent>> = AudioBuckets
    private val Audios: MutableLiveData<ArrayList<AudioContent>> = MutableLiveData()
    val audios: LiveData<ArrayList<AudioContent>> = Audios
    var foundAudios: MutableLiveData<ArrayList<AudioContent>> = MutableLiveData()

    fun loadMoreAudioItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
        val audiosList = ArrayList<AudioContent>()
        CoroutineScope(Dispatchers.IO).async {
            audiosList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .getAudios(context, MediaFacer.externalAudioContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post {
                    Audios.value = audiosList
                }
        }
    }

    //search
    fun searchAudioItems(context: Context, paginationStart: Int,
                         paginationLimit: Int, shouldPaginate: Boolean,
                         selectionType: String, selectionValue: String){
        val found = ArrayList<AudioContent>()
        CoroutineScope(Dispatchers.IO).async {
            found.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .searchAudios(context, MediaFacer.externalAudioContent,selectionType,selectionValue)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post {
                    foundAudios.value = found
                }
        }
    }

    //buckets
    fun loadAudioBuckets(context: Context){
        val buckets = ArrayList<AudioBucketContent>()
        CoroutineScope(Dispatchers.IO).async {
            buckets.addAll(
                MediaFacer
                    .getBuckets(context, MediaFacer.externalAudioContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    AudioBuckets.value = buckets
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

}