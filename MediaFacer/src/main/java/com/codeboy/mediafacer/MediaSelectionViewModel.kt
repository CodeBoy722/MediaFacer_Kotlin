package com.codeboy.mediafacer

import androidx.core.util.Predicate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.models.VideoContent

internal class MediaSelectionViewModel: ViewModel() {

    val actionAdd = "add"
    val actionRemove = "remove"

    private val _numItemsSelected: MutableLiveData<Int> = MutableLiveData()
    val numItemsSelected: LiveData<Int> = _numItemsSelected

    private val _selectedAudios: MutableLiveData<ArrayList<AudioContent>> = MutableLiveData()
    val selectedAudios: LiveData<ArrayList<AudioContent>> = _selectedAudios

    private val _selectedVideos: MutableLiveData<ArrayList<VideoContent>> = MutableLiveData()
    val selectedVideos: LiveData<ArrayList<VideoContent>> = _selectedVideos

    private val _selectedPhotos: MutableLiveData<ArrayList<ImageContent>> = MutableLiveData()
    val selectedPhotos: LiveData<ArrayList<ImageContent>> = _selectedPhotos

    private fun updateNumberSelected(action: String){
        when(action){
            actionAdd ->{
                _numItemsSelected.value = _numItemsSelected.value?.plus(1)
            }
            actionRemove ->{
             _numItemsSelected.value = _numItemsSelected.value?.minus(1)
            }
        }
    }

    private fun <T> remove(list: ArrayList<T>, predicate: Predicate<T>) {
        list.filter { predicate.test(it) }.forEach { list.remove(it) }
    }

    fun emptySelections(){
        _numItemsSelected.value = 0
        _selectedAudios.value = ArrayList()
        _selectedVideos.value = ArrayList()
        _selectedPhotos.value = ArrayList()
    }

    fun addOrRemoveAudioItem(audio: AudioContent, action: String){
        when(action){
            actionAdd ->{
                _selectedAudios.value?.add(audio)
                updateNumberSelected(actionAdd)
            }
            actionRemove ->{
                val item = Predicate { music: AudioContent -> music.musicId == audio.musicId}
                remove(_selectedAudios.value!!, item)
                updateNumberSelected(actionRemove)
            }
        }
    }


    fun addOrRemoveVideoItem(video: VideoContent, action: String){

        when(action){
            actionAdd ->{
                _selectedVideos.value?.add(video)
                updateNumberSelected(actionAdd)
            }
            actionRemove ->{
                val item = Predicate { videoX: VideoContent -> videoX.id == video.id}
                remove(_selectedVideos.value!!, item)
                updateNumberSelected(actionRemove)
            }
        }
    }


    fun addOrRemoveImageItem(image: ImageContent, action: String){

        when(action){
            actionAdd ->{
                _selectedPhotos.value?.add(image)
                updateNumberSelected(actionAdd)
            }
            actionRemove ->{
                val item = Predicate { imageX: ImageContent -> imageX.imageId == image.imageId}
                remove(_selectedPhotos.value!!, item)
                updateNumberSelected(actionRemove)
            }
        }
    }


}