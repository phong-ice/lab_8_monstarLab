package com.example.icemusic.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.icemusic.fragment.ListPlaying
import com.example.icemusic.fragment.Playing
import com.example.icemusic.fragment.Relate

class AdapterPagerPlaying(private val listFragment:MutableList<Fragment>, frManager: FragmentManager) : FragmentStatePagerAdapter(
    frManager
) {
    override fun getCount(): Int {
        return listFragment.size
    }

    override fun getItem(position: Int): Fragment {
        return listFragment[position]
    }


}