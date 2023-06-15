package com.codeboy.mediafacerkotlin.fragments

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
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics

class About() : Fragment() {

    lateinit var bindings: FragmentAboutBinding
    var addControl = false

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

        bindings.gitLink.setOnClickListener {
            requireActivity().startActivity(Intent(Intent.ACTION_VIEW,  Uri.parse("https://github.com/CodeBoy722/MediaFacer_Kotlin")))
        }
    }

    override fun onDetach() {
        super.onDetach()
        //make bottom navigation visible again
        (requireActivity() as MainActivity).showBottomMenu()
    }
    private fun loadInterstitial() {
        InterstitialAd.load(requireActivity(), getString(R.string.interstitial), AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                interstitialAd.fullScreenContentCallback = object :
                    FullScreenContentCallback() {

                    override fun onAdImpression() {
                        super.onAdImpression()
                        val bundle = Bundle()
                        bundle.putString(
                            FirebaseAnalytics.Param.METHOD,
                            "onAdImpression_Banner"
                        )
                        FirebaseAnalytics.getInstance(requireActivity())
                            .logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle)
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        Snackbar.make(requireView(),"Ad failed to load try later",Snackbar.LENGTH_LONG).show()
                    }
                }
                interstitialAd.show(requireActivity())
            }
        })
    }

}