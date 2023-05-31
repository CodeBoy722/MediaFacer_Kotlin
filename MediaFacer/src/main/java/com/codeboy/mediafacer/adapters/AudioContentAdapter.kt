package com.codeboy.mediafacer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codeboy.mediafacer.MediaSelectionViewModel
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.databinding.AudioSelectItemBinding
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.tools.MediaSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

internal class AudioContentAdapter(private val defaultArt: Int,private val listener: MediaSelectionViewModel)
    : ListAdapter<AudioContent, AudioContentAdapter.AudioSelectViewHolder>(AudioDiffUtil()) {

    var lastPosition = -1
    //var selectionIndicators = ArrayList<Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioSelectViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = AudioSelectItemBinding.inflate(inflater, parent, false)
        return AudioSelectViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: AudioSelectViewHolder, position: Int) {
        if(holder.adapterPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.adapterPosition
        }

        holder.itemPosition = position
        holder.item = getItem(position)
        holder.bind()
    }

    /*override fun submitList(list: MutableList<AudioContent>?) {
        super.submitList(list)
        if(list != null){
            if(selectionIndicators.size == 0){
                list.forEach{ it ->
                    selectionIndicators.add(false)
                }
            }else{
                val diff = list.size.minus(selectionIndicators.size)
                if(diff > 0){
                    for (i in 1..diff){
                        selectionIndicators.add(false)
                    }
                }
            }
        }
    }*/

    private class AudioDiffUtil : DiffUtil.ItemCallback<AudioContent>() {
        override fun areItemsTheSame(oldItem: AudioContent, newItem: AudioContent): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: AudioContent, newItem: AudioContent): Boolean {
            return oldItem.musicId == newItem.musicId
        }
    }

    inner class AudioSelectViewHolder(private val bindings: AudioSelectItemBinding)
        : RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: AudioContent
        var itemPosition = 0
        fun bind(){
            bindings.root.setOnClickListener(this)
            Glide.with(bindings.art)
                .load(item.artUri)
                .apply(RequestOptions().centerCrop().circleCrop())
                .placeholder(R.drawable.music_placeholder)
                .into(bindings.art)

            CoroutineScope(Dispatchers.Main).async {
                val itemSort = listener.selectedAudios.value!!.sortedBy { it.musicId == item.musicId }
                bindings.selector.isChecked = (itemSort.isNotEmpty() && (itemSort[0].musicId == item.musicId))
            }

            bindings.artist.text = item.artist
            bindings.title.text = item.title
        }

        override fun onClick(v: View?) {
            bindings.selector.isChecked = !bindings.selector.isChecked
            if(bindings.selector.isChecked){
                listener.addOrRemoveAudioItem(item, listener.actionAdd)
            }else{
                listener.addOrRemoveAudioItem(item, listener.actionRemove)
            }
        }
    }

}