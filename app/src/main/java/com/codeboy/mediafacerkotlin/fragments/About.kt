package com.codeboy.mediafacerkotlin.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentAboutBinding
import com.google.android.material.snackbar.Snackbar


class About() : Fragment() {

    lateinit var bindings: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentAboutBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.githubLink.setOnClickListener {
            requireActivity().startActivity(Intent(Intent.ACTION_VIEW,  Uri.parse("https://github.com/CodeBoy722/MediaFacer_Kotlin")))
        }

        bindings.kofi.setOnClickListener {
            requireActivity().startActivity(Intent(Intent.ACTION_VIEW,  Uri.parse("https://ko-fi.com/codeboy722")))
        }

        bindings.watchAd.setOnClickListener {
            (requireActivity() as MainActivity).addControl = true
            (requireActivity() as MainActivity).loadInterstitial()
        }

        bindings.copyBnb.setOnClickListener {
            val clip = ClipData.newPlainText("bnb address", "0xCE504c3Ab64d8f87BF7b0bC80d2BBE062890124A")
            (requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?)?.setPrimaryClip(clip)
            Snackbar.make(requireView(), "Address Copied", Snackbar.LENGTH_LONG).show()
        }

    }

    override fun onDetach() {
        super.onDetach()
        //make bottom navigation visible again
        (requireActivity() as MainActivity).showBottomMenu()
    }


}