package com.codeboy.mediafacerkotlin.viewAdapters

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.AudioItemBinding
import com.codeboy.mediafacerkotlin.listeners.AudioActionListener
import com.codeboy.mediafacerkotlin.musicSession.PlaybackProtocol
import com.codeboy.mediafacerkotlin.utils.MusicDataUtil
import kotlinx.coroutines.*

class AudioViewAdapter(private val listener: AudioActionListener, private val parentLiveCycle: LifecycleOwner)
    : ListAdapter<AudioContent, AudioViewAdapter.AudioViewHolder>(AudioDiffUtil()) {

    var lastPosition = -1
    var playingState = false
    var playingPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView: AudioItemBinding = AudioItemBinding.inflate(inflater, parent, false)
      /*  PlaybackProtocol.isMusicPlaying.observe(parentLiveCycle, Observer {
            playingState = it
            //notifyItemChanged(playingPosition)
        })*/
        return AudioViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        if(holder.layoutPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.layoutPosition
        }
        holder.item = getItem(position)
        holder.itemPosition = position
        holder.bind()
    }

    private class AudioDiffUtil : DiffUtil.ItemCallback<AudioContent>() {
        override fun areItemsTheSame(oldItem: AudioContent, newItem: AudioContent): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: AudioContent, newItem: AudioContent): Boolean {
            return oldItem.musicId == newItem.musicId
        }
    }


    inner class AudioViewHolder(private val bindings: AudioItemBinding)
        : RecyclerView.ViewHolder(bindings.root), View.OnClickListener, View.OnLongClickListener{
        lateinit var item: AudioContent
        var itemPosition = 0
        fun bind(){
            bindings.root.setOnLongClickListener(this)
            bindings.root.setOnClickListener(this)
            Glide.with(bindings.art)
                .load(Uri.parse(item.artUri))
                .apply(RequestOptions().centerCrop().circleCrop())
                .placeholder(R.drawable.music_placeholder)
                .into(bindings.art)

            if(PlaybackProtocol.currentMedia.value != null){
                if ((PlaybackProtocol.currentMedia.value!!.mediaId == item.musicId.toString()) && playingState){
                    playingPosition = itemPosition
                    Glide.with(bindings.playIndicator)
                        .load(R.drawable.equalizer)
                        .into(bindings.playIndicator)
                    bindings.playIndicator.colorFilter =
                        PorterDuffColorFilter(
                            ContextCompat.getColor(bindings.root.context, R.color.bright_navy_blue),
                            PorterDuff.Mode.SRC_ATOP
                        )
                    bindings.playIndicator.visibility = View.VISIBLE
                }else{ bindings.playIndicator.visibility = View.GONE }
            }else{
                bindings.playIndicator.visibility = View.GONE
            }


            bindings.artist.text = item.artist
            bindings.title.text = item.title
        }


        override fun onClick(v: View?) {
            // add to playlist
            listener.onAudioItemClicked(item, itemPosition)
           /* notifyItemChanged(playingPosition)
            playingPosition = itemPosition
            notifyItemChanged(itemPosition)*/
        }

        override fun onLongClick(v: View?): Boolean {
            // show details
            listener.onAudioItemLongClicked(item, itemPosition)
            return true
        }
    }

}