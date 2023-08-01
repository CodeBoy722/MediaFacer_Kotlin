package com.codeboy.mediafacer.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.models.ImageFolderContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

internal class ImagesViewModel: ViewModel() {

    private val _images: MutableLiveData<ArrayList<ImageContent>> = MutableLiveData()
    val images: LiveData<ArrayList<ImageContent>> = _images
    private val _imageFolders: MutableLiveData<ArrayList<ImageFolderContent>> = MutableLiveData()
    val imageFolders: LiveData<ArrayList<ImageFolderContent>> = _imageFolders

    private var folders = ArrayList<ImageFolderContent>()
    var imagesList = ArrayList<ImageContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
        CoroutineScope(Dispatchers.IO).async {
            imagesList.addAll(
                MediaFacer
                    .withPagination(paginationStart, paginationLimit)
                    .getImages(context, MediaFacer.externalImagesContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    _images.value = imagesList
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
                    _imageFolders.value = folders
                }
        }
    }

}