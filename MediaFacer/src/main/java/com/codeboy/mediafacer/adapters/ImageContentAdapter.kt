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
import com.codeboy.mediafacer.MediaSelectionViewModel
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.databinding.ImageSelectItemBinding
import com.codeboy.mediafacer.models.ImageContent

internal class ImageContentAdapter(private val listener: MediaSelectionViewModel, private val pickerColor: Int)
    : ListAdapter<ImageContent, ImageContentAdapter.ImageSelectViewHolder>(ImageDiffUtil()){

    var lastPosition = -1
    //var selectionIndicators = ArrayList<Boolean>()

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

            val found = listener.selectedPhotos.value!!.filter { it.imageId == item.imageId }.size == 1
            bindings.selector.isChecked = found
        }

        override fun onClick(p0: View?) {
            bindings.selector.isChecked = !bindings.selector.isChecked
            if(bindings.selector.isChecked){
                listener.addOrRemoveImageItem(item, listener.actionAdd)
            }else{
                listener.addOrRemoveImageItem(item, listener.actionRemove)
            }
        }

    }

}