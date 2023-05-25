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
import com.codeboy.mediafacer.databinding.ImageSelectItemBinding
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.tools.MediaSelectionListener

internal class ImageContentAdapter(private val listener: MediaSelectionViewModel)
    : ListAdapter<ImageContent, ImageContentAdapter.ImageSelectViewHolder>(ImageDiffUtil()){

    var lastPosition = -1
    var selectionIndicators = ArrayList<Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSelectViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = ImageSelectItemBinding.inflate(inflater,parent,false)
        return ImageSelectViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: ImageSelectViewHolder, position: Int) {
        if(holder.adapterPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.adapterPosition
        }

        holder.itemPosition = position
        holder.item = getItem(position)
        holder.bind()
    }

    override fun submitList(list: MutableList<ImageContent>?) {
        super.submitList(list)
        list?.forEachIndexed { index, imageContent ->
            if(selectionIndicators.size-1 <= index){
                selectionIndicators.add(false)
            }else if(selectionIndicators.size-1 >= index && !selectionIndicators[index]){
                selectionIndicators[index] = false
            }
        }
    }

    private class ImageDiffUtil : DiffUtil.ItemCallback<ImageContent>() {
        override fun areItemsTheSame(oldItem: ImageContent, newItem: ImageContent): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ImageContent, newItem: ImageContent): Boolean {
            return oldItem.imageId == newItem.imageId
        }
    }

    inner class ImageSelectViewHolder(private val bindings: ImageSelectItemBinding):
        RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: ImageContent
        var itemPosition = 0
        fun bind(){
            bindings.root.setOnClickListener(this)
            Glide.with(bindings.image)
                .load(item.imageUri)
                .apply(RequestOptions().centerCrop())
                .into(bindings.image)

            //bindings.selector.visibility = View.GONE
            bindings.selector.isChecked = selectionIndicators[itemPosition]
        }

        override fun onClick(p0: View?) {
            bindings.selector.isChecked = !bindings.selector.isChecked
            selectionIndicators[itemPosition] = bindings.selector.isChecked
            listener.addOrRemoveImageItem(item)
        }

    }

}