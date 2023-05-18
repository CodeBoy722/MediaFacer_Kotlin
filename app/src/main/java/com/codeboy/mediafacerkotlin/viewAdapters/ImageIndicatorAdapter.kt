package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.ImageIndicatorItemBinding
import com.codeboy.mediafacerkotlin.databinding.ImageItemBinding
import com.codeboy.mediafacerkotlin.listeners.ImageActionListener
import com.codeboy.mediafacerkotlin.utils.Utils

class ImageIndicatorAdapter(private val listener: ImageActionListener)
    : ListAdapter<ImageContent, ImageIndicatorAdapter.ImageIndicatorViewHolder>(Utils.ImageDiffUtil()) {

    private var lastPosition = -1
    var selectedPosition = 0
    var unselected = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageIndicatorViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = ImageIndicatorItemBinding.inflate(inflater,parent,false)
        return ImageIndicatorViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: ImageIndicatorViewHolder, position: Int) {
        if(holder.layoutPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.layoutPosition
        }
        holder.item = getItem(position)
        holder.itemPosition = position
        holder.bind()
    }

    fun setSelected(position: Int){
        unselected = selectedPosition
        selectedPosition = position

        notifyItemChanged(unselected)
        notifyItemChanged(selectedPosition)
    }

    inner class ImageIndicatorViewHolder(private val bindings: ImageIndicatorItemBinding):
        RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: ImageContent
        var itemPosition = 0
        fun bind(){
            bindings.root.setOnClickListener(this)
            Glide.with(bindings.image)
                .load(item.imageUri)
                .apply(RequestOptions().centerCrop())
                .into(bindings.image)

            if(selectedPosition == itemPosition){
                lightUp()
            }else{
                lightOff()
            }
        }

        private fun lightUp(){
            bindings.selectIndicator.visibility = View.GONE
        }

        private fun lightOff(){
            bindings.selectIndicator.visibility = View.VISIBLE
        }

        override fun onClick(p0: View?) {
            val imageList = ArrayList<ImageContent>()
            imageList.addAll(currentList)
            listener.onImageItemClicked(itemPosition, imageList)
        }

    }


}