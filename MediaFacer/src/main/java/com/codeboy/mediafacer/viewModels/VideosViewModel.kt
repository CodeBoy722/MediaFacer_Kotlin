package com.codeboy.mediafacer.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacer.models.VideoFolderContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

internal object VideosViewModel: ViewModel() {

    private val VideoFolders: MutableLiveData<ArrayList<VideoFolderContent>> = MutableLiveData()
    val videoFolders: LiveData<ArrayList<VideoFolderContent>> = VideoFolders
    private val FoundVideos: MutableLiveData<ArrayList<VideoContent>> = MutableLiveData()
    val foundVideos: LiveData<ArrayList<VideoContent>> = FoundVideos
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
                    FoundVideos.value = found
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
                    VideoFolders.value = folders
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

}