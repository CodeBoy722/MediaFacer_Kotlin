package com.codeboy.mediafacerkotlin.viewAdapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.ArrayList

class MainPagerFragmentAdapter(fragmentActivity: FragmentActivity, private val fragmentList : ArrayList<Fragment>) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

}