package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.models.AudioBucketContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.AudioFolderItemBinding
import com.codeboy.mediafacerkotlin.listeners.AudioContainerActionListener

class AudioBucketViewAdapter(private val mediaListener: AudioContainerActionListener): ListAdapter<AudioBucketContent, AudioBucketViewAdapter.AudioBucketViewHolder>(AudioBucketDiffUtil()) {

    var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioBucketViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = AudioFolderItemBinding.inflate(inflater,parent,false)
        return AudioBucketViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: AudioBucketViewHolder, position: Int) {
        if(holder.layoutPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.layoutPosition
        }
        holder.item = getItem(position)
        holder.bind()
    }

    private class AudioBucketDiffUtil : DiffUtil.ItemCallback<AudioBucketContent>() {
        override fun areItemsTheSame(oldItem: AudioBucketContent, newItem: AudioBucketContent): Boolean {
            return oldItem.bucketName == newItem.bucketName
        }

        override fun areContentsTheSame(oldItem: AudioBucketContent, newItem: AudioBucketContent): Boolean {
            return oldItem.bucketName == newItem.bucketName
        }
    }

    inner class AudioBucketViewHolder(private val bindings: AudioFolderItemBinding)
        : RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: AudioBucketContent
            fun bind(){
                bindings.root.setOnClickListener(this)
                bindings.folderName.text = item.bucketName
                val bucketSize = item.audios.size.toString()
                val bucketSizeText = "$bucketSize Songs in this folder"
                bindings.numOfSongs.text = bucketSizeText
            }

        override fun onClick(v: View?) {
            mediaListener.onAudioContainerClicked("Bucket", item.bucketName, item.audios)
        }
    }

}