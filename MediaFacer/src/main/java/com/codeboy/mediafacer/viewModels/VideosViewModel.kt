package com.codeboy.mediafacer.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacer.models.VideoFolderContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.lang.reflect.Array

internal object VideosViewModel: ViewModel() {

    var videoFolders: MutableLiveData<ArrayList<VideoFolderContent>> = MutableLiveData()
    var foundVideos: MutableLiveData<ArrayList<VideoContent>> = MutableLiveData()
    var videos: MutableLiveData<ArrayList<VideoContent>> = MutableLiveData()

    fun loadMoreVideoItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
        val videoList = ArrayList<VideoContent>()
        CoroutineScope(Dispatchers.IO).async {
            videoList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .getVideos(context, MediaFacer.externalVideoContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    videos.value = videoList
                }
        }
    }

    //search
    fun searchVideoItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean,
                     selectionType: String, selectionValue: String){
        val found = ArrayList<VideoContent>()
        CoroutineScope(Dispatchers.IO).async {
            found.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .searchVideos(context, MediaFacer.externalVideoContent,selectionType,selectionValue)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    foundVideos.value = found
                }
        }
    }

    //folders
    fun loadVideoBucketItems(context: Context){
        val folders = ArrayList<VideoFolderContent>()
        CoroutineScope(Dispatchers.IO).async {
            folders.addAll(
                MediaFacer
                    .getVideoFolders(context, MediaFacer.externalVideoContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    videoFolders.value = folders
                }
        }
    }

    override fun onCleared() {
        //todo dispose model here
        super.onCleared()
    }

}