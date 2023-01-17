package com.codeboy.mediafacerkotlin.musicSession

import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.R

object PlaybackProtocol: ViewModel() {

    //check if the music service is running
    var isMusicServiceRunning: Boolean = false

    private val _isMusicPlaying : MutableLiveData<Boolean> = MutableLiveData()
    var isMusicPlaying: LiveData<Boolean> = _isMusicPlaying
    fun setIsMusicPlaying(status: Boolean){
        _isMusicPlaying.value = status
    }

    private val _currentMusic: MutableLiveData<AudioContent> = MutableLiveData()
    val currentMusic: LiveData<AudioContent> = _currentMusic

    fun setCurrentMusic(music: AudioContent){
        _currentMusic.value = music
    }


    object ProfilePicBindingAdapter {
        @BindingAdapter("MusicArt")
        @JvmStatic
        private fun setMusicArt(view: ImageView, link: String) {
            Glide.with(view)
                .load(link)
                .centerCrop().circleCrop()
                .placeholder(R.drawable.music_placeholder)
                .into(view)
        }
    }

    object PlayButtonBindingAdapter {
        @BindingAdapter("PlayButton")
        @JvmStatic
        private fun setPlayButton(view: AppCompatImageButton, status: Boolean) {
            if (status){

            }else{

            }
        }
    }


}