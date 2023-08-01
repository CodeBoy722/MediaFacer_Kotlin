package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.MediaFacer.externalVideoContent
import com.codeboy.mediafacer.models.VideoContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class VideoViewModel : ViewModel() {

    private val _videos: MutableLiveData<ArrayList<VideoContent>> = MutableLiveData()
    val videos: LiveData<ArrayList<VideoContent>> = _videos
    private var videosList = ArrayList<VideoContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int){
        CoroutineScope(Dispatchers.IO).async {// run code on IO thread with CoroutineScope
            videosList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit)
                    .getVideos(context, externalVideoContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())// use handler to post result back on main thread
                .post{
                    // continue your work here
                   _videos.value = videosList
                }
        }
    }



}