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
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icemusic.adapter.AdapterListMusicRelate
import com.example.icemusic.databinding.FragmentSearchBinding
import com.example.icemusic.helper.CheckNetworkConnected
import com.example.icemusic.helper.CommunicationAdapterMusic
import com.example.icemusic.model.IceMusic
import com.example.icemusic.repositoty.ApiRepository
import com.example.icemusic.service.MusicService
import com.example.icemusic.viewmodel.ApiViewModel
import kotlinx.coroutines.*

class Search : Fragment(), CommunicationAdapterMusic {
    val tagName = "SEARCH"

    private val binding: FragmentSearchBinding by lazy {
        FragmentSearchBinding.inflate(layoutInflater)
    }

    private val repo: ApiRepository by lazy { ApiRepository() }
    private val viewModelApi: ApiViewModel by lazy {
        ViewModelProvider(
            this,
            ApiViewModel.ApiViewModelProvide(repo)
        )[ApiViewModel::class.java]
    }
    private lateinit var listMusic: MutableList<IceMusic>
    private lateinit var adapterListMusic: AdapterListMusicRelate
    private var coroutineSearch: Job? = null
    private lateinit var musicService: MusicService
    private var isServiceConnection = false

    override fun onStart() {
        super.onStart()
        if (!isServiceConnection) {
            Intent(requireContext(), MusicService::class.java).also {
                requireContext().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isServiceConnection) {
            Intent(requireContext(), MusicService::class.java).also {
                requireContext().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
            }
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
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        listMusic = mutableListOf()
        adapterListMusic = AdapterListMusicRelate(listMusic, this,requireContext())
        binding.lvSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterListMusic
        }
        viewModelApi.listMusicSearched.observe(this, {
            if (it.size > 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    listMusic.clear()
                    for (musicSearch in it) {
                        val iceMusic = IceMusic(
                            musicSearch.id,
                            musicSearch.name,
                            musicSearch.artist,
                            "https://photo-resize-zmp3.zadn.vn/w320_r1x1_png/${musicSearch.thumb}",
                            musicSearch.duration.toInt(),
                            null
                        )
                        listMusic.add(iceMusic)
                    }
                    withContext(Dispatchers.Main) {
                        adapterListMusic.notifyDataSetChanged()
                    }
                }
            }
            binding.progress.visibility = View.GONE
        })

        binding.edtSearch.addTextChangedListener {
            binding.progress.visibility = View.VISIBLE
            coroutineSearch?.cancel(null)
            coroutineSearch = null
            coroutineSearch = CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                val searchPattern: String = it.toString()
                if (CheckNetworkConnected.isConnectedInternet(requireContext())) {
                    if (searchPattern != "") {
                        viewModelApi.searchMusic(searchPattern)
                    } else {
                        binding.progress.visibility = View.GONE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No internet", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    override fun itemOnClick(position: Int) {
        val itemOnClick = listMusic[position]
        var iceMusic: IceMusic? = null
        viewModelApi.getInfoMusic(itemOnClick.id)
        viewModelApi.dataInfoLiveData.observe(this, {
            if (it.artists.size > 0) {
                iceMusic = IceMusic(
                    itemOnClick.id,
                    itemOnClick.name,
                    itemOnClick.artists_names,
                    it.artists[0].thumbnail,
                    itemOnClick.duration,
                    null
                )
            } else {
                iceMusic = IceMusic(
                    itemOnClick.id,
                    itemOnClick.name,
                    itemOnClick.artists_names,
                    it.info.thumbnail,
                    itemOnClick.duration,
                    null
                )
            }

        })
        viewModelApi.getRelateMusic("audio", itemOnClick.id)
        viewModelApi.listRelateMusic.observe(this, { _listMusic ->
            val listMusic: MutableList<IceMusic> = mutableListOf()
            listMusic.addAll(_listMusic)
            iceMusic?.let { listMusic.add(0, it) }
            musicService.setListMusicService(listMusic, 0)
        })
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iBinder = service as MusicService.MyBinder
            musicService = iBinder.getMusicService()
            isServiceConnection = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceConnection = false
        }

    }

    override fun onStop() {
        super.onStop()
        requireContext().unbindService(serviceConnection)
        isServiceConnection = false
    }

}