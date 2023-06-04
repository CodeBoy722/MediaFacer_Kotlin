package com.codeboy.mediafacerkotlin.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentAboutBinding

class About() : Fragment() {

    lateinit var bindings: FragmentAboutBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentAboutBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.gitLink.setOnClickListener {
            requireActivity().startActivity(Intent(Intent.ACTION_VIEW,  Uri.parse("https://github.com/CodeBoy722/MediaFacer_Kotlin")))
        }
    }

    override fun onDetach() {
        super.onDetach()
        //make bottom navigation visible again
        (requireActivity() as MainActivity).showBottomMenu()
        (requireActivity() as MainActivity).loadInterstitial()
    }


}