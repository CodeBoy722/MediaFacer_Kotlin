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

internal class VideosViewModel: ViewModel() {

    private val _videoFolders: MutableLiveData<ArrayList<VideoFolderContent>> = MutableLiveData()
    val videoFolders: LiveData<ArrayList<VideoFolderContent>> = _videoFolders
    private val _foundVideos: MutableLiveData<ArrayList<VideoContent>> = MutableLiveData()
    val foundVideos: LiveData<ArrayList<VideoContent>> = _foundVideos
    private val _videos: MutableLiveData<ArrayList<VideoContent>> = MutableLiveData()
    val videos: LiveData<ArrayList<VideoContent>> = _videos

    private var videoList = ArrayList<VideoContent>()
    var foundList = ArrayList<VideoContent>()

    fun loadMoreVideoItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
        CoroutineScope(Dispatchers.IO).async {
            videoList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .getVideos(context, MediaFacer.externalVideoContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    _videos.value = videoList
                }
        }
    }

    //search
    fun searchVideoItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean,
                     selectionType: String, selectionValue: String){
        CoroutineScope(Dispatchers.IO).async {
            foundList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .searchVideos(context, MediaFacer.externalVideoContent,selectionType,selectionValue)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    _foundVideos.value = foundList
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
                    _videoFolders.value = folders
                }
        }
    }

}