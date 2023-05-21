package com.codeboy.mediafacer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.models.VideoContent

internal class MediaSelectionViewModel: ViewModel() {

    private val actionAdd = "add"
    private val actionRemove = "remove"

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
                _numItemsSelected.value = _numItemsSelected.value?.minus(1)
            }
            actionRemove ->{
             _numItemsSelected.value = _numItemsSelected.value?.plus(1)
            }
        }
    }

    fun emptySelections(){
        _numItemsSelected.value = 0
        _selectedAudios.value = ArrayList()
        _selectedVideos.value = ArrayList()
        _selectedPhotos.value = ArrayList()
    }

    fun addOrRemoveAudioItem(audio: AudioContent){
        val rm = _selectedAudios.value?.remove(audio)
        if(!rm!!){
            _selectedAudios.value?.add(audio)
            updateNumberSelected(actionAdd)
        }else{
            updateNumberSelected(actionRemove)
        }
    }


    fun addOrRemoveVideoItem(video: VideoContent){
        val rm = _selectedVideos.value?.remove(video)
        if(!rm!!){
            _selectedVideos.value?.add(video)
            updateNumberSelected(actionAdd)
        }else{
            updateNumberSelected(actionRemove)
        }
    }


    fun addOrRemoveImageItem(image: ImageContent){
        val rm = _selectedPhotos.value?.remove(image)
        if(!rm!!){
            _selectedPhotos.value?.add(image)
            updateNumberSelected(actionAdd)
        }else{
            updateNumberSelected(actionRemove)
        }
    }


}