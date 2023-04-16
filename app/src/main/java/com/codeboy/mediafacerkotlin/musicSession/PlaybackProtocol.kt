package com.codeboy.mediafacerkotlin.musicSession

import android.net.Uri
import android.view.View
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
import androidx.media3.common.MediaItem
import kotlinx.coroutines.*

object PlaybackProtocol: ViewModel() {

    //check if the music service is running
    var isMusicServiceRunning: Boolean = false

    private val _isMusicPlaying : MutableLiveData<Boolean> = MutableLiveData()
    var isMusicPlaying: LiveData<Boolean> = _isMusicPlaying
    fun setIsMusicPlaying(status: Boolean){
        _isMusicPlaying.value = status
    }

    private val _musicList: MutableLiveData<ArrayList<AudioContent>> = MutableLiveData()
    val musicList: LiveData<ArrayList<AudioContent>> = _musicList

    fun setMusicList(musicList: ArrayList<AudioContent>){
        _musicList.value = musicList
    }

    private val _mediaItemList: MutableLiveData<ArrayList<MediaItem>> = MutableLiveData()
    val mediaItemList: LiveData<ArrayList<MediaItem>> = _mediaItemList

    fun updateMediaList(mediaList: ArrayList<MediaItem>){
        _mediaItemList.value = mediaList
    }


    private val _currentMedia: MutableLiveData<MediaItem> = MutableLiveData()
    val currentMedia: LiveData<MediaItem> = _currentMedia

    fun updateCurrentMedia(music: MediaItem){
        _currentMedia.value = music
    }


    object ProfilePicBindingAdapter {
        @BindingAdapter("MusicArt")
        @JvmStatic
        fun setMusicArt(view: AppCompatImageView, link: Uri?) {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Main){
                    Glide.with(view)
                        .load(link ?: R.drawable.music_placeholder)
                        .centerCrop().circleCrop()
                        .placeholder(R.drawable.music_placeholder)
                        .into(view)
                }.waitForLayout()
            }
        }
    }

    object PlayButtonBindingAdapter {
        @BindingAdapter("PlayButton")
        @JvmStatic
        fun setPlayButton(view: AppCompatImageButton, status: Boolean) {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Main){
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
    }


  /*  object PlayingIndicatorAdapter{
        @BindingAdapter("PlayIndicator")
        @JvmStatic
        fun updatePlayerIndicator(equalizer: AppCompatImageView, mediaId: String?){
            if(mediaId != null){
                if (equalizer.tag == mediaId && isMusicPlaying.value == true){
                    Glide.with(equalizer)
                        .load(R.drawable.equalizer)
                        .into(equalizer)
                    equalizer.visibility = View.VISIBLE
                }else{
                    equalizer.visibility = View.GONE
                }
            }
        }
    }*/

   /* object PlayButtonAdapter{
        @BindingAdapter("ItemPlayButton")
        @JvmStatic
        fun updateItemPlayButton(playButton: AppCompatImageButton, mediaId: String){
            if (playButton.tag == mediaId){
                playButton.visibility = View.GONE
            }else{
                playButton.visibility = View.VISIBLE
            }
        }
    }*/


}