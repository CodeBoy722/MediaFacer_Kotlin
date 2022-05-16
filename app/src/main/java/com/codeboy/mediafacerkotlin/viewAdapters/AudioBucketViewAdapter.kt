package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.models.AudioBucketContent
import com.codeboy.mediafacerkotlin.databinding.AudioFolderItemBinding

class AudioBucketViewAdapter: ListAdapter<AudioBucketContent, AudioBucketViewAdapter.AudioBucketViewHolder>(
    AudioBucketViewAdapter.AudioBucketDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioBucketViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = AudioFolderItemBinding.inflate(inflater,parent,false)
        return AudioBucketViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: AudioBucketViewHolder, position: Int) {
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

    class AudioBucketViewHolder(private val bindings: AudioFolderItemBinding)
        : RecyclerView.ViewHolder(bindings.root){
        lateinit var item: AudioBucketContent
            fun bind(){
                bindings.folderName.text = item.bucketName
                val bucketSize = item.audios.size.toString()
                val bucketSizeText = "$bucketSize Songs in this folder"
                bindings.numOfSongs.text = bucketSizeText
            }
    }

}