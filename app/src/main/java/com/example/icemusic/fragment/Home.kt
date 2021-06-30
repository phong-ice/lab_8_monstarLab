package com.example.icemusic.fragment

import android.content.*
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icemusic.activity.PlayingMusic
import com.example.icemusic.adapter.AdapterListTop100Music
import com.example.icemusic.databinding.FragmentHomeBinding
import com.example.icemusic.helper.CheckNetworkConnected
import com.example.icemusic.helper.CommunicationAdapterMusic
import com.example.icemusic.model.IceMusic
import com.example.icemusic.repositoty.ApiRepository
import com.example.icemusic.service.MusicService
import com.example.icemusic.viewmodel.ApiViewModel

class Home : Fragment(), CommunicationAdapterMusic {

    private val binding: FragmentHomeBinding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private val repo: ApiRepository by lazy { ApiRepository() }
    private val viewModel: ApiViewModel by lazy {
        ViewModelProvider(
            this,
            ApiViewModel.ApiViewModelProvide(repo)
        )[ApiViewModel::class.java]
    }
    private lateinit var listMusic: MutableList<IceMusic>
    private lateinit var musicService: MusicService

    override fun onStart() {
        super.onStart()
        Intent(requireContext(), MusicService::class.java).also {
            requireActivity().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        requireContext().registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
       if (CheckNetworkConnected.isConnectedInternet(requireContext())){
           viewModel.getTop100Music()
       }
        listMusic = mutableListOf()
        viewModel.listTop100Music.observe(this, {
            binding.lvTop100Music.apply {
                listMusic.addAll(it)
                adapter = AdapterListTop100Music(requireContext(), it, this@Home)
                layoutManager = LinearLayoutManager(requireContext())
            }
            binding.progress.visibility = View.GONE
        })
    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iBinder = service as MusicService.MyBinder
            musicService = iBinder.getMusicService()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    override fun itemOnClick(position: Int) {
       if (CheckNetworkConnected.isConnectedInternet(requireContext())){
           musicService.setListMusicService(listMusic, position)
           startActivity(Intent(requireContext(), PlayingMusic::class.java))
       }else{
           Toast.makeText(requireContext(), "No Internet", Toast.LENGTH_SHORT).show()
       }
    }
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (CheckNetworkConnected.isConnectedInternet(requireContext())) {
                false -> {
                    binding.lvTop100Music.visibility = View.GONE
                    binding.layoutNoInternet.visibility = View.VISIBLE
                }
                true -> {
                    viewModel.getTop100Music()
                    binding.lvTop100Music.visibility = View.VISIBLE
                    binding.layoutNoInternet.visibility = View.GONE
                }
            }
        }
    }
}