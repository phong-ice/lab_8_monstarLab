package com.example.icemusic.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.icemusic.R
import com.example.icemusic.adapter.AdapterPagerPlaying
import com.example.icemusic.databinding.FragmentPlayingMusicBinding
import com.example.icemusic.fragment.ListPlaying
import com.example.icemusic.fragment.Playing
import com.example.icemusic.fragment.Relate
import com.example.icemusic.helper.CheckNetworkConnected

class PlayingMusic : AppCompatActivity() {

    private val binding: FragmentPlayingMusicBinding by lazy {
        FragmentPlayingMusicBinding.inflate(
            layoutInflater
        )
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewpagerPlaying.adapter = AdapterPagerPlaying(
            mutableListOf(ListPlaying(), Playing(), Relate()),
            supportFragmentManager
        )
        binding.viewpagerPlaying.offscreenPageLimit = 2
        binding.viewpagerPlaying.currentItem = 1
        binding.viewpagerPlaying.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.imgDotPlaylist.setImageResource(R.drawable.shape_redius_10dp_selected)
                        binding.imgDotPlaying.setImageResource(R.drawable.shape_radius_10dp)
                        binding.imgDotRelate.setImageResource(R.drawable.shape_radius_10dp)
                    }
                    1 -> {
                        binding.imgDotPlaylist.setImageResource(R.drawable.shape_radius_10dp)
                        binding.imgDotPlaying.setImageResource(R.drawable.shape_redius_10dp_selected)
                        binding.imgDotRelate.setImageResource(R.drawable.shape_radius_10dp)
                    }
                    2 -> {
                        binding.imgDotPlaylist.setImageResource(R.drawable.shape_radius_10dp)
                        binding.imgDotPlaying.setImageResource(R.drawable.shape_radius_10dp)
                        binding.imgDotRelate.setImageResource(R.drawable.shape_redius_10dp_selected)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })

        binding.btnBack.setOnClickListener {
            Intent(this, Content::class.java).also {
                startActivity(it)
            }
            finish()
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (CheckNetworkConnected.isConnectedInternet(this@PlayingMusic)) {
                false -> binding.tvNoInternet.visibility = View.VISIBLE
                true -> binding.tvNoInternet.visibility = View.GONE
            }
        }
    }

}