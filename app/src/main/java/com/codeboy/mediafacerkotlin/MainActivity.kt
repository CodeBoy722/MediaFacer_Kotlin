package com.codeboy.mediafacerkotlin

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.transition.Slide
import androidx.viewpager2.widget.ViewPager2
import com.codeboy.mediafacer.models.*
import com.codeboy.mediafacerkotlin.databinding.ActivityMainBinding
import com.codeboy.mediafacerkotlin.fragments.*
import com.codeboy.mediafacerkotlin.musicSession.MediaLibrary
import com.codeboy.mediafacerkotlin.viewAdapters.MainPagerFragmentAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : AppCompatActivity() {

    lateinit var bindings: ActivityMainBinding
    private var folderList = ArrayList<AudioBucketContent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bindings.lifecycleOwner = this

        bindings.aboutMenu.setOnClickListener {
            hideBottomMenu()
            val about = About()
            val slideOutFromTop = Slide(Gravity.TOP)
            val slideInFromBottom = Slide(Gravity.BOTTOM)
            about.enterTransition = slideInFromBottom
            about.exitTransition = slideOutFromTop
            val anim: Animation = AnimationUtils.loadAnimation(this, R.anim.animation_fall_down)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.activity_parent, about, about.javaClass.canonicalName)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit()
            about.view?.startAnimation(anim)
        }

        initializeBannerAd()
        setUpBottomMenu()
    }

    fun hideBottomMenu(){
        bindings.bottomMenu.visibility = View.GONE
    }

    fun showBottomMenu(){
        bindings.bottomMenu.visibility = View.VISIBLE
    }

    private fun setUpBottomMenu(){
        val introFragmentList = ArrayList<Fragment>()
        introFragmentList.add(AudiosFragment())
        introFragmentList.add(VideosFragment())
        introFragmentList.add(ImagesFragment())
        introFragmentList.add(MediaTools())

        val welcomeFragments = MainPagerFragmentAdapter(this,introFragmentList)
        bindings.bodyPager.offscreenPageLimit = 4
        bindings.bodyPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        bindings.bodyPager.adapter = welcomeFragments
        bindings.bodyPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> bindings.bottomMenu.selectedItemId = R.id.audios
                    1 -> bindings.bottomMenu.selectedItemId = R.id.videos
                    2 -> bindings.bottomMenu.selectedItemId = R.id.images
                    3 -> bindings.bottomMenu.selectedItemId = R.id.tools
                }
            }
        })

        bindings.bottomMenu.setOnItemSelectedListener{
            when (it.itemId) {
                R.id.audios -> bindings.bodyPager.setCurrentItem(0, true)
                R.id.videos -> bindings.bodyPager.setCurrentItem(1, true)
                R.id.images -> bindings.bodyPager.setCurrentItem(2, true)
                R.id.tools -> bindings.bodyPager.setCurrentItem(3, true)
            }
            true
        }
    }

    private fun initializeBannerAd() {
        val mAdView = AdView(this)
        mAdView.setAdSize(getAdSize())
        mAdView.adUnitId = getString(R.string.banner)
        bindings.adzone.addView(mAdView)
        val adRequest = AdRequest.Builder()
            .build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                bindings.adzone.visibility = View.VISIBLE
            }
        }
    }

    private fun getAdSize(): AdSize {
        val outMetrics = DisplayMetrics()
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = this.display
            @Suppress("DEPRECATION")
            display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = this.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(outMetrics)
        }
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }

    fun loadInterstitial() {
        InterstitialAd.load(this, getString(R.string.interstitial), AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
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
                            FirebaseAnalytics.getInstance(this@MainActivity)
                                .logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle)
                        }
                    }
                    interstitialAd.show(this@MainActivity)
                }
        })
    }

}