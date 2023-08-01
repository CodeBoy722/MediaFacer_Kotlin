package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.MediaFacer.externalImagesContent
import com.codeboy.mediafacer.models.ImageContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class ImageViewModel : ViewModel() {

    private val _images: MutableLiveData<ArrayList<ImageContent>> = MutableLiveData()
    val images: LiveData<ArrayList<ImageContent>> = _images
    private var imagesList = ArrayList<ImageContent>()

     fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int){
         CoroutineScope(Dispatchers.IO).async {
             imagesList.addAll(
                 MediaFacer
                     .withPagination(paginationStart, paginationLimit)
                     .getImages(context, externalImagesContent)
             )
         }.invokeOnCompletion {
             Handler(Looper.getMainLooper())
                 .post{
                     _images.value = imagesList
                 }
         }
     }

}