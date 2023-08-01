package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.ImageFolderContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class ImageFolderViewModel: ViewModel() {

    private val _imageFolders: MutableLiveData<ArrayList<ImageFolderContent>> = MutableLiveData()
    val imageFolders: LiveData<ArrayList<ImageFolderContent>> = _imageFolders
    private var imageFoldersList = ArrayList<ImageFolderContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int){
        CoroutineScope(Dispatchers.IO).async {
            imageFoldersList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit)
                    .getImageFolders(context, MediaFacer.externalImagesContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    _imageFolders.value = imageFoldersList
                }
        }
    }

}