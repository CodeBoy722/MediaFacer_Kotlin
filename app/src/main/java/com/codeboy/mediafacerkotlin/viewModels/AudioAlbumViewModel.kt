package com.codeboy.mediafacerkotlin.viewModels

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.AudioAlbumContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class AudioAlbumViewModel {

    var audioAlbums: MutableLiveData<ArrayList<AudioAlbumContent>> = MutableLiveData()
    private var audioAlbumList = ArrayList<AudioAlbumContent>()

    fun loadNewItems(context: Context, paginationStart: Int, paginationLimit: Int, shouldPaginate: Boolean){
        CoroutineScope(Dispatchers.IO).async {
            audioAlbumList.addAll(
                MediaFacer()
                    .withPagination(paginationStart, paginationLimit, shouldPaginate)
                    .getAlbums(context, MediaFacer.externalAudioContent)
            )
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post {
                    audioAlbums.value = audioAlbumList
                }
        }
    }

}