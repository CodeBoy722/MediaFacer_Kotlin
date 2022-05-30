package com.codeboy.mediafacerkotlin.dialogs

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codeboy.mediafacer.MediaDataUtils
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentImageDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.DateFormat

class ImageDetails() : BottomSheetDialogFragment() {

    lateinit var bindings: FragmentImageDetailsBinding
    lateinit var imageItem: ImageContent
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    constructor(imageItem: ImageContent):this(){
        this.imageItem = imageItem
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentImageDetailsBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        //setting Peek at the 16:9 ratio key line of its parent.
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        view.minimumHeight = (Resources.getSystem().displayMetrics.heightPixels)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        initContent()
    }

    private fun initContent(){
        bindings.imageName.text = imageItem.name
        bindings.imagePath.text = imageItem.filePath
        bindings.imageSize.text = MediaDataUtils.convertBytes(imageItem.size)
        bindings.dateModified.text = DateFormat.getDateInstance().format(imageItem.dateModified)
    }

}