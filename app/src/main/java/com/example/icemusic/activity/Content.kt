package com.example.icemusic.activity

import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.example.icemusic.R
import com.example.icemusic.adapter.AdapterPagerPlaying
import com.example.icemusic.databinding.ActivityContentBinding
import com.example.icemusic.fragment.Favorite
import com.example.icemusic.fragment.Home
import com.example.icemusic.fragment.MusicOffline
import com.example.icemusic.fragment.Search
import com.example.icemusic.helper.CheckNetworkConnected
import com.example.icemusic.service.MusicService
import com.squareup.picasso.Picasso

class Content : AppCompatActivity() {
    private val binding: ActivityContentBinding by lazy {
        ActivityContentBinding.inflate(
            layoutInflater
        )
    }
    private lateinit var musicService: MusicService

    override fun onStart() {
        super.onStart()
        registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Intent(this, MusicService::class.java).also {
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        binding.viewPager.adapter = AdapterPagerPlaying(
            mutableListOf(Home(), Favorite(), MusicOffline()),
            supportFragmentManager
        )
        binding.viewPager.offscreenPageLimit = 2
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.tvTitle.text = "#100"
                        binding.imgDotTop100.setImageResource(R.drawable.shape_redius_10dp_selected)
                        binding.imgDotFavorite.setImageResource(R.drawable.shape_radius_10dp)
                        binding.imgDotPlaylist.setImageResource(R.drawable.shape_radius_10dp)
                    }
                    1 -> {
                        binding.tvTitle.text = "Favorite Music"
                        binding.imgDotTop100.setImageResource(R.drawable.shape_radius_10dp)
                        binding.imgDotFavorite.setImageResource(R.drawable.shape_radius_10dp)
                        binding.imgDotPlaylist.setImageResource(R.drawable.shape_redius_10dp_selected)
                    }
                    2 -> {
                        binding.tvTitle.text = "Offline music"
                        binding.imgDotTop100.setImageResource(R.drawable.shape_radius_10dp)
                        binding.imgDotFavorite.setImageResource(R.drawable.shape_redius_10dp_selected)
                        binding.imgDotPlaylist.setImageResource(R.drawable.shape_radius_10dp)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}

        })
        binding.btnPlayMinimize.setOnClickListener {
            musicService.onPauseOrRePlay()
        }
        binding.layoutMinimize.setOnClickListener {
            Intent(this, PlayingMusic::class.java).also {
                startActivity(it)
            }
        }
        binding.btnSearch.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.layout_content, Search())
            transaction.addToBackStack(Search().tagName)
            transaction.commit()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iBinder = service as MusicService.MyBinder
            musicService = iBinder.getMusicService()
            musicService.getMusicPlaying().observe(this@Content, {
                binding.layoutMinimize.visibility = View.VISIBLE
                binding.tvNameMinimize.text = it.name
                if (it.uri != null) {
                    it.uri?.let { uri ->
                        binding.imgMinimize.setImageBitmap(getAlbumBitmap(this@Content, uri))
                    }
                } else {
                    Picasso.get().load(it.thumbnail).into(binding.imgMinimize)
                }
            })
            musicService.getIsPlayingMusic().observe(this@Content, {
                when (it) {
                    true -> binding.btnPlayMinimize.setImageResource(R.drawable.ic_baseline_pause_24)
                    false -> binding.btnPlayMinimize.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (CheckNetworkConnected.isConnectedInternet(this@Content)) {
                false -> binding.tvNoInternet.visibility = View.VISIBLE
                true -> binding.tvNoInternet.visibility = View.GONE

            }
        }
    }

    private fun getAlbumBitmap(context: Context, audioUri: Uri): Bitmap {
        val mmr = MediaMetadataRetriever()
        val art: Bitmap
        val bfo = BitmapFactory.Options()
        mmr.setDataSource(context, audioUri)
        val rawArt: ByteArray? = mmr.embeddedPicture
        return if (null != rawArt) {
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
            art
        } else {
            BitmapFactory.decodeResource(context.resources, R.drawable.headphone)
        }
    }

}