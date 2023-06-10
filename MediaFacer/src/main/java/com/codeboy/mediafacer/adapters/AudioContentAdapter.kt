package com.codeboy.mediafacer.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codeboy.mediafacer.MediaSelectionViewModel
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.databinding.AudioSelectItemBinding
import com.codeboy.mediafacer.models.AudioContent


internal class AudioContentAdapter(private val defaultArt: Int,private val listener: MediaSelectionViewModel, private val pickerColor: Int)
    : ListAdapter<AudioContent, AudioContentAdapter.AudioSelectViewHolder>(AudioDiffUtil()) {

    var lastPosition = -1

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

            val stateDrawable = StateListDrawable()
            stateDrawable.addState( intArrayOf(-android.R.attr.state_checked),ResourcesCompat.getDrawable(bindings.root.resources, R.drawable.ic_media_uncheck, null));
            stateDrawable.addState(intArrayOf(android.R.attr.state_checked), ResourcesCompat.getDrawable(bindings.root.resources, R.drawable.ic_media_check, null))

            val states = arrayOf(
                intArrayOf(android.R.attr.state_checked), // checked
                intArrayOf(-android.R.attr.state_checked), // unchecked
            )

            val colors = intArrayOf(
                ResourcesCompat.getColor(bindings.root.resources, pickerColor,null),
                ResourcesCompat.getColor(bindings.root.resources, R.color.material_grey_400,null),
            )

            val checkColorList = ColorStateList(states, colors)

            bindings.selector.buttonDrawable = stateDrawable
            bindings.selector.buttonTintList = checkColorList

            val found = listener.selectedAudios.value!!.filter { it.musicId == item.musicId }.size == 1
            bindings.selector.isChecked = found

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