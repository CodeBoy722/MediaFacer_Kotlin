package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.VideoFolderContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class VideoFolderViewModel: ViewModel() {

    var videoFolders: MutableLiveData<ArrayList<VideoFolderContent>> = MutableLiveData()
    private var videoFoldersList = ArrayList<VideoFolderContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
        CoroutineScope(Dispatchers.Main).async {
            videoFoldersList.addAll(
                MediaFacer()
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .getVideoFolders(context, MediaFacer.externalVideoContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    videoFolders.value = videoFoldersList
                }
        }
    }

}