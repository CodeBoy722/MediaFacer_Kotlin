package com.codeboy.mediafacerkotlin

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacerkotlin.databinding.ActivityPlayerBinding

//exo player activity
class PlayerActivity : AppCompatActivity() {

    private lateinit var bindings: ActivityPlayerBinding
    private var player: ExoPlayer? = null

    private var playPosition  = 0
    private var mediaItems = ArrayList<MediaItem>()

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = DataBindingUtil.setContentView(this,R.layout.activity_player)
        bindings.lifecycleOwner = this

        playPosition = intent.getIntExtra("play_position", 0)
        val videos: ArrayList<VideoContent> = intent.getParcelableArrayListExtra("videos")!!

        for (item: VideoContent in videos){
            mediaItems.add(MediaItem.fromUri(item.videoUri))
        }
        initializePlayer()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                bindings.videoView.player = exoPlayer
                exoPlayer.setMediaItem( mediaItems[playPosition])
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentItem, playbackPosition)
                exoPlayer.prepare()
            }
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }


    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, bindings.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

}