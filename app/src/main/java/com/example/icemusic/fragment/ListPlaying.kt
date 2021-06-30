package com.example.icemusic.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icemusic.adapter.AdapterListMusic
import com.example.icemusic.databinding.FragmentListPlayingBinding
import com.example.icemusic.model.IceMusic
import com.example.icemusic.service.MusicService
import kotlinx.coroutines.*


class ListPlaying : Fragment() {

    private val binding: FragmentListPlayingBinding by lazy {
        FragmentListPlayingBinding.inflate(
            layoutInflater
        )
    }
    private lateinit var musicService: MusicService
    private lateinit var listMusic: MutableList<IceMusic>
    private lateinit var adapterListMusic: AdapterListMusic

    override fun onStart() {
        super.onStart()
        Intent(requireContext(), MusicService::class.java).also {
            requireContext().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listMusic = mutableListOf()
    }

    private val itemOnClick: (Int) -> Unit = {
        musicService.setListMusicService(listMusic, it)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iBinder = service as MusicService.MyBinder
            musicService = iBinder.getMusicService()
            musicService.getListMusic().observe(this@ListPlaying, {
                listMusic.clear()
                listMusic.addAll(it)
                adapterListMusic = AdapterListMusic(requireContext(), listMusic, itemOnClick)
                binding.lvMusic.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = adapterListMusic
                }
            })
            musicService.getMusicPlaying().observe(this@ListPlaying, {
                adapterListMusic.setIdMusicPlaying(it.id)
                CoroutineScope(Dispatchers.IO).launch {
                    for ((index, music) in listMusic.withIndex()) {
                        if (music.id == it.id) {
                            withContext(Dispatchers.Main) {
                                adapterListMusic.notifyItemChanged(index)
                            }
                            break
                        }
                    }
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }
}