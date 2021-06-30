package com.example.icemusic.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icemusic.activity.PlayingMusic
import com.example.icemusic.adapter.AdapterListMusicOffline
import com.example.icemusic.databinding.FragmentMusicOfflineBinding
import com.example.icemusic.model.IceMusic
import com.example.icemusic.repositoty.MusicFavoriteRoomRepository
import com.example.icemusic.service.MusicService
import com.example.icemusic.viewmodel.FavoriteMusicViewModel


class MusicOffline : Fragment() {

    private val binding: FragmentMusicOfflineBinding by lazy {
        FragmentMusicOfflineBinding.inflate(
            layoutInflater
        )
    }
    private val repo: MusicFavoriteRoomRepository by lazy {
        MusicFavoriteRoomRepository(
            requireActivity().application
        )
    }
    private val viewModelMusicFavorite: FavoriteMusicViewModel by lazy {
        ViewModelProvider(
            this,
            FavoriteMusicViewModel.ViewModelProvide(repo)
        )[FavoriteMusicViewModel::class.java]
    }
    private lateinit var musicService: MusicService
    private lateinit var adapterListMusicOffline: AdapterListMusicOffline
    private lateinit var listMusic: MutableList<IceMusic>
    private var isServiceConnection = false
    private val REQUEST_CODE_PERMISSTION = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        Intent(requireContext(), MusicService::class.java).also {
            requireContext().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        checkPermission()
        listMusic = mutableListOf()
        viewModelMusicFavorite.listMusicOffline.observe(this, {
            listMusic.clear()
            listMusic.addAll(it)
            adapterListMusicOffline =
                AdapterListMusicOffline(requireContext(), listMusic, itemOnClick)
            binding.lvMusicOffline.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = adapterListMusicOffline
            }
        })
    }

    private val itemOnClick: (Int) -> Unit = {
        musicService.setListMusicService(listMusic, it)
        Intent(requireContext(), PlayingMusic::class.java).also { intent ->
            requireContext().startActivity(intent)
        }
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

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissionArr = arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requireActivity().requestPermissions(permissionArr, REQUEST_CODE_PERMISSTION)
            } else {
                viewModelMusicFavorite.getAllMusicOffline(requireActivity().application)
            }
        } else {
            viewModelMusicFavorite.getAllMusicOffline(requireActivity().application)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSTION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModelMusicFavorite.getAllMusicOffline(requireActivity().application)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unbindService(serviceConnection)
        isServiceConnection = false
    }

}