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
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icemusic.adapter.AdapterListMusicRelate
import com.example.icemusic.databinding.FragmentRelateBinding
import com.example.icemusic.helper.CheckNetworkConnected
import com.example.icemusic.helper.CommunicationAdapterMusic
import com.example.icemusic.model.IceMusic
import com.example.icemusic.repositoty.ApiRepository
import com.example.icemusic.service.MusicService
import com.example.icemusic.viewmodel.ApiViewModel

class Relate : Fragment(), CommunicationAdapterMusic {

    private val binding: FragmentRelateBinding by lazy {
        FragmentRelateBinding.inflate(
            layoutInflater
        )
    }
    private val repo: ApiRepository by lazy { ApiRepository() }
    private val viewModelApi: ApiViewModel by lazy {
        ViewModelProvider(
            this,
            ApiViewModel.ApiViewModelProvide(repo)
        )[ApiViewModel::class.java]
    }
    private lateinit var musicService: MusicService
    private lateinit var listMusic: MutableList<IceMusic>

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

        viewModelApi.listRelateMusic.observe(this, {
            binding.lvRelateMusic.apply {
                listMusic.clear()
                listMusic.addAll(it)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = AdapterListMusicRelate(listMusic, this@Relate, requireContext())
            }
            binding.progress.visibility = View.GONE
        })
    }


    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iBinder = service as MusicService.MyBinder
            musicService = iBinder.getMusicService()
            musicService.getMusicPlaying().observe(this@Relate, {
                binding.progress.visibility = View.VISIBLE
                if (CheckNetworkConnected.isConnectedInternet(requireContext())) {
                    viewModelApi.getRelateMusic("audio", it.id)
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unbindService(serviceConnection)
    }

    override fun itemOnClick(position: Int) {
        if (CheckNetworkConnected.isConnectedInternet(requireContext())) {
            musicService.setListMusicService(listMusic, position)
        } else {
            Toast.makeText(requireContext(), "No internet", Toast.LENGTH_SHORT).show()
        }
    }
}