package com.codeboy.mediafacerkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.MediaFacer.Companion.externalAudioContent
import com.codeboy.mediafacer.MediaFacer.Companion.externalImagesContent
import com.codeboy.mediafacer.MediaFacer.Companion.externalVideoContent
import com.codeboy.mediafacer.models.*
import com.codeboy.mediafacerkotlin.databinding.ActivityMainBinding
import com.codeboy.mediafacerkotlin.fragments.AudiosFragment
import com.codeboy.mediafacerkotlin.fragments.ImagesFragment
import com.codeboy.mediafacerkotlin.fragments.MediaTools
import com.codeboy.mediafacerkotlin.fragments.VideosFragment
import com.codeboy.mediafacerkotlin.viewAdapters.MainPagerFragmentAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class MainActivity : AppCompatActivity() {

    lateinit var bindings: ActivityMainBinding
    var folderList = ArrayList<AudioBucketContent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bindings.lifecycleOwner = this
        setUpBottomMenu()
        //testFacer()
    }

    private fun testFacer(){
        var folders = ArrayList<AudioBucketContent>()
        CoroutineScope(Dispatchers.Main).async {
            folders = MediaFacer().getBuckets(this@MainActivity, externalAudioContent)
        }.invokeOnCompletion {
            Handler(Looper.getMainLooper())
                .post{
                    folderList = folders
                    Toast.makeText(
                        this@MainActivity,
                        "buckets " + folders.size.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun setUpBottomMenu(){
        val introFragmentList = ArrayList<Fragment>()
        introFragmentList.add(AudiosFragment())
        introFragmentList.add(ImagesFragment())
        introFragmentList.add(VideosFragment())
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
                    1 -> bindings.bottomMenu.selectedItemId = R.id.images
                    2 -> bindings.bottomMenu.selectedItemId = R.id.videos
                    3 -> bindings.bottomMenu.selectedItemId = R.id.tools
                }
            }
        })

        bindings.bottomMenu.setOnItemSelectedListener{
            when (it.itemId) {
                R.id.audios -> bindings.bodyPager.setCurrentItem(0, true)
                R.id.images -> bindings.bodyPager.setCurrentItem(1, true)
                R.id.videos -> bindings.bodyPager.setCurrentItem(2, true)
                R.id.tools -> bindings.bodyPager.setCurrentItem(3, true)
            }
            true
        }
    }

}