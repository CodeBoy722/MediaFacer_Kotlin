package com.codeboy.mediafacer.adapters

import android.content.res.ColorStateList
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
import com.codeboy.mediafacer.MediaDataUtils
import com.codeboy.mediafacer.MediaSelectionViewModel
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.databinding.VideoSelectItemBinding
import com.codeboy.mediafacer.models.VideoContent

internal class VideoContentAdapter(private val listener: MediaSelectionViewModel, private val pickerColor: Int)
    : ListAdapter<VideoContent, VideoContentAdapter.VideoSelectViewHolder>(VideoDiffUtil()){

    var lastPosition = -1
    //var selectionIndicators = ArrayList<Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoSelectViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = VideoSelectItemBinding.inflate(inflater,parent,false)
        return VideoSelectViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: VideoSelectViewHolder, position: Int) {
        if(holder.adapterPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.adapterPosition
        }
        holder.item = getItem(position)
        holder.bind()
    }

    private class VideoDiffUtil : DiffUtil.ItemCallback<VideoContent>() {
        override fun areItemsTheSame(oldItem: VideoContent, newItem: VideoContent): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: VideoContent, newItem: VideoContent): Boolean {
            return oldItem.id == newItem.id
        }
    }

    inner class VideoSelectViewHolder(private val bindings: VideoSelectItemBinding)
        : RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: VideoContent
        var itemPosition = 0
        fun bind(){
            bindings.root.setOnClickListener(this)
            Glide.with(bindings.videoPreview)
                .load(item.videoUri)
                .apply(RequestOptions().centerCrop())
                .into(bindings.videoPreview)

            val stateDrawable = StateListDrawable()
            stateDrawable.addState( intArrayOf(-android.R.attr.state_checked),
                ResourcesCompat.getDrawable(bindings.root.resources, R.drawable.ic_media_uncheck, null));
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

            bindings.playIndicator.setColorFilter(ResourcesCompat.getColor(bindings.root.resources, pickerColor,null))
            bindings.length.setTextColor(ResourcesCompat.getColor(bindings.root.resources, pickerColor,null))

            val length = ": " + MediaDataUtils.milliSecondsToTimer(item.duration)
            bindings.length.text = length
            val found = listener.selectedVideos.value!!.filter { it.id == item.id }.size == 1
            bindings.selector.isChecked = found
        }

        override fun onClick(v: View?) {
            bindings.selector.isChecked = !bindings.selector.isChecked
            if(bindings.selector.isChecked){
                listener.addOrRemoveVideoItem(item, listener.actionAdd)
            }else{
                listener.addOrRemoveVideoItem(item, listener.actionRemove)
            }
        }

    }

}