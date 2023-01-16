package com.codeboy.mediafacerkotlin

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacerkotlin.databinding.ActivityPlayerBinding

//exo player activity
class PlayerActivity : AppCompatActivity() {

    private lateinit var bindings: ActivityPlayerBinding
    private lateinit var player: ExoPlayer
    private val TAG = "player_activity"

    private var mediaItems = ArrayList<MediaItem>()
    private var rawVideos = ArrayList<VideoContent>()

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    private val playbackStateListener: Player.Listener = playbackStateListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = DataBindingUtil.setContentView(this,R.layout.activity_player)
        bindings.lifecycleOwner = this

        currentItem = intent.getIntExtra("play_position", 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rawVideos = intent.getParcelableArrayListExtra("videos",VideoContent::class.java)!!
        }else{
            rawVideos = intent.getParcelableArrayListExtra("videos")!!
        }

        for (item: VideoContent in rawVideos){
            mediaItems.add(MediaItem.fromUri(item.videoUri))
        }
        initializePlayer()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun initializePlayer() {
        //for adaptive streaming
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }

        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                bindings.videoView.player = exoPlayer
               /* for(item: VideoContent in rawVideos){
                    val adaptiveMediaItem = MediaItem.Builder()
                        .setUri(item.videoUri)
                        .setMimeType(MimeTypes.APPLICATION_MPD)
                        .build()
                    exoPlayer.addMediaItem(adaptiveMediaItem)
                }*/

                //exoPlayer.setMediaItem( mediaItems[playPosition])
                exoPlayer.addMediaItems(mediaItems)
                exoPlayer.seekTo(currentItem, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.prepare()
            }

    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d(TAG, "changed state to $stateString")
            when(playbackState){
                ExoPlayer.STATE_READY -> {
                    playWhenReady = true
                    player.playWhenReady = playWhenReady
                }
                ExoPlayer.STATE_BUFFERING -> {
                    //show a toast to tell user it buffering or unstable internet
                }
                ExoPlayer.STATE_ENDED -> {
                    playWhenReady = false
                }
                ExoPlayer.STATE_IDLE -> {
                    //here you can set items and prepare
                }
            }
        }
    }

   /* @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    public override fun onStart() {
        super.onStart()
        initializePlayer()
    }*/

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (player == null) {
            initializePlayer()
        }else{
            playWhenReady = true
            player.playWhenReady = playWhenReady
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    public override fun onPause() {
        super.onPause()
        playWhenReady = false
        player.playWhenReady = playWhenReady
    }

    override fun onDestroy() {
        super.onDestroy()
        playWhenReady = false
        player.playWhenReady = playWhenReady
        releasePlayer()
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
        player.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player.stop()
    }

}