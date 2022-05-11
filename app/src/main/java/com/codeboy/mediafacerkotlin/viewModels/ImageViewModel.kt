package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.MediaFacer.Companion.externalImagesContent
import com.codeboy.mediafacer.models.ImageContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class ImageViewModel : ViewModel() {

    var images: MutableLiveData<ArrayList<ImageContent>> = MutableLiveData()
    private var imagesList = ArrayList<ImageContent>()

     fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
         CoroutineScope(Dispatchers.Main).async {
             imagesList.addAll(
                 MediaFacer()
                     .withPagination(paginationStart, paginationLimit, shouldPaginate)
                     .getImages(context, externalImagesContent)
             )
         }.invokeOnCompletion {
             Handler(Looper.getMainLooper())
                 .post{
                     images.value = imagesList
                 }
         }
     }

}