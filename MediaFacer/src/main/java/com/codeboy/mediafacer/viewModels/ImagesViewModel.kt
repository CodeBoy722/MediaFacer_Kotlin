package com.codeboy.mediafacer.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.models.ImageFolderContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

internal object ImagesViewModel: ViewModel() {

    var images: MutableLiveData<ArrayList<ImageContent>> = MutableLiveData()
    var imageFolders: MutableLiveData<ArrayList<ImageFolderContent>> = MutableLiveData()
    private var folders = ArrayList<ImageFolderContent>()
    private var imagesList = ArrayList<ImageContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
        CoroutineScope(Dispatchers.IO).async {
            imagesList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .getImages(context, MediaFacer.externalImagesContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    images.value = imagesList
                }
        }
    }

    fun loadFolders(context: Context){
        CoroutineScope(Dispatchers.IO).async {
            folders.addAll(
                MediaFacer.getImageFolders(context,MediaFacer.externalImagesContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    imageFolders.value = folders
                }
        }
    }

    override fun onCleared() {
        //todo dispose model here
        super.onCleared()
    }
}