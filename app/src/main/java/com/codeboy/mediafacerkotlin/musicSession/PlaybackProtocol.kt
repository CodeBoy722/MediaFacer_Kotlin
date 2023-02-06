package com.codeboy.mediafacerkotlin.musicSession

import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.R
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat

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

    private val _musicList: MutableLiveData<ArrayList<AudioContent>> = MutableLiveData()
    val musicList: LiveData<ArrayList<AudioContent>> = _musicList

    fun setMusicList(musicList: ArrayList<AudioContent>){
        _musicList.value = musicList
    }

    object ProfilePicBindingAdapter {
        @BindingAdapter("MusicArt")
        @JvmStatic
        fun setMusicArt(view: AppCompatImageView, link: Uri?) {
            Glide.with(view)
                .load(link?: R.drawable.music_placeholder)
                .centerCrop().circleCrop()
                .placeholder(R.drawable.music_placeholder)
                .into(view)
        }
    }

    object PlayButtonBindingAdapter {
        @BindingAdapter("PlayButton")
        @JvmStatic
        fun setPlayButton(view: AppCompatImageButton, status: Boolean) {
            if (status){
                val drawable = ResourcesCompat.getDrawable(view.context.resources, R.drawable.ic_pause, null)
                view.setImageDrawable(drawable)
            }else{
                val drawable = ResourcesCompat.getDrawable(view.context.resources, R.drawable.ic_play, null)
                view.setImageDrawable(drawable)
            }
        }
    }


}