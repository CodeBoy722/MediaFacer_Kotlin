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
import com.codeboy.mediafacer.databinding.VideoSelectItemBinding
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacer.tools.MediaSelectionListener

internal class VideoContentAdapter(private val listener: MediaSelectionViewModel)
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

    /*override fun submitList(list: MutableList<VideoContent>?) {
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

            val itemSort = listener.selectedVideos.value!!.sortedBy { it.id == item.id }
            bindings.selector.isChecked = (itemSort.isNotEmpty() && (itemSort[0].id == item.id))
        }

        override fun onClick(v: View?) {
            bindings.selector.isChecked = !bindings.selector.isChecked
            listener.addOrRemoveVideoItem(item)
        }

    }

}