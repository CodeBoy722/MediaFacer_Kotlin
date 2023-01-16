package com.codeboy.mediafacerkotlin.musicSession

import android.widget.ImageView
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

    var isMusicPlaying: Boolean = false

    private val _currentMusic: MutableLiveData<AudioContent> = MutableLiveData()
    val currentMusic: LiveData<AudioContent> = _currentMusic

    fun setCurrentMusic(music: AudioContent){
        _currentMusic.value = music
    }


    object ProfilePicBindingAdapter {
        @BindingAdapter("ProfilePic")
        @JvmStatic
        fun setProfilePic(view: ImageView?, link: String?) {
            Glide.with(view!!)
                .load(link)
                .centerCrop().circleCrop()
                .placeholder(R.drawable.music_placeholder)
                .into(view)
        }
    }


}