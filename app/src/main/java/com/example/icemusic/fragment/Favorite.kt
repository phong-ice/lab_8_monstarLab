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
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icemusic.adapter.AdapterListMusicRelate
import com.example.icemusic.databinding.FragmentFavoriteBinding
import com.example.icemusic.helper.CheckNetworkConnected
import com.example.icemusic.helper.CommunicationAdapterMusic
import com.example.icemusic.model.IceMusic
import com.example.icemusic.model.MusicFavorite
import com.example.icemusic.repositoty.MusicFavoriteRoomRepository
import com.example.icemusic.service.MusicService
import com.example.icemusic.viewmodel.FavoriteMusicViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Favorite : Fragment(),CommunicationAdapterMusic {

    private val binding: FragmentFavoriteBinding by lazy {
        FragmentFavoriteBinding.inflate(
            layoutInflater
        )
    }
    private val repo: MusicFavoriteRoomRepository by lazy {
        MusicFavoriteRoomRepository(requireActivity().application)
    }
    private val musicFavoriteMusicViewModel: FavoriteMusicViewModel by lazy {
        ViewModelProvider(
            this,
            FavoriteMusicViewModel.ViewModelProvide(repo)
        )[FavoriteMusicViewModel::class.java]
    }
    private lateinit var listMusic: MutableList<IceMusic>
    private lateinit var adapterListMusicRelate: AdapterListMusicRelate
    private lateinit var musicService:MusicService

    override fun onStart() {
        super.onStart()
        Intent(requireContext(),MusicService::class.java).also {
            requireContext().bindService(it,serviceConnection,Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        musicFavoriteMusicViewModel.getFavoriteMusic()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listMusic = mutableListOf()
        musicFavoriteMusicViewModel.getFavoriteMusic()
        musicFavoriteMusicViewModel.listFavorite.observe(this, {
            CoroutineScope(Dispatchers.IO).launch {
                listMusic.clear()
                for (favorite in it) {
                    val iceMusic = when {
                        favorite.uri != null -> IceMusic(
                            favorite.id,
                            favorite.name,
                            favorite.artists_names,
                            favorite.thumbnail,
                            favorite.duration,
                            favorite.uri!!.toUri()
                        )
                        else -> IceMusic(
                            favorite.id,
                            favorite.name,
                            favorite.artists_names,
                            favorite.thumbnail,
                            favorite.duration,
                            null
                        )
                    }
                    listMusic.add(iceMusic)
                }
                withContext(Dispatchers.Main){adapterListMusicRelate.notifyDataSetChanged()}
            }
            adapterListMusicRelate = AdapterListMusicRelate(listMusic, this,requireContext())
            binding.lvFavoriteMusic.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = adapterListMusicRelate
            }
        })
    }

    private val serviceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iBinder = service as MusicService.MyBinder
            musicService = iBinder.getMusicService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

    }

    override fun itemOnClick(position: Int) {
       if(CheckNetworkConnected.isConnectedInternet(requireContext())){
           musicService.setListMusicService(listMusic,position)
       }else{
           Toast.makeText(requireContext(), "No internet", Toast.LENGTH_SHORT).show()
       }
    }

}