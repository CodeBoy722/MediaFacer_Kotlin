package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.MediaFacer.Companion.externalVideoContent
import com.codeboy.mediafacer.models.VideoContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class VideoViewModel : ViewModel() {

    var videos: MutableLiveData<ArrayList<VideoContent>> = MutableLiveData()
    private var videosList = ArrayList<VideoContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
        CoroutineScope(Dispatchers.IO).async {
            videosList.addAll(
                MediaFacer()
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .getVideos(context, externalVideoContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                   videos.value = videosList
                }
        }
    }

}