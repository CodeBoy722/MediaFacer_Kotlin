package com.codeboy.mediafacer.tools

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.databinding.FragmentMediaFacerMediaPickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MediaFacerMediaPicker : BottomSheetDialogFragment() {

    lateinit var bindings: FragmentMediaFacerMediaPickerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentMediaFacerMediaPickerBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
    }

    private fun initPicker(){

    }

}