package com.example.icemusic.fragment

import android.app.DownloadManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.icemusic.R
import com.example.icemusic.databinding.FragmentPlayingBinding
import com.example.icemusic.helper.KeyWork
import com.example.icemusic.model.IceMusic
import com.example.icemusic.model.MusicFavorite
import com.example.icemusic.repositoty.MusicFavoriteRoomRepository
import com.example.icemusic.service.MusicService
import com.example.icemusic.viewmodel.FavoriteMusicViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*


class Playing : Fragment() {

    private val binding: FragmentPlayingBinding by lazy {
        FragmentPlayingBinding.inflate(
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
    private val REQUEST_PERMISSION_CODE = 100
    private lateinit var musicService: MusicService
    private lateinit var listMusicFavorite: MutableList<MusicFavorite>
    private var iceMusic: IceMusic? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Intent(requireContext(), MusicService::class.java).also {
            requireContext().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        listMusicFavorite = mutableListOf()
        binding.btnNext.setOnClickListener { musicService.onNext() }
        binding.btnPrevious.setOnClickListener { musicService.onPrevious() }
        binding.btnPlay.setOnClickListener { musicService.onPauseOrRePlay() }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvCurrent.text = when {
                    (progress / 1000) >= 60 && (progress / 1000) % 60 < 10 -> "${(progress / 1000) / 60}:0${(progress / 1000) % 60}"
                    (progress / 1000) >= 60 -> "${(progress / 1000) / 60}:${(progress / 1000) % 60}"
                    (progress / 1000) < 10 -> "0:0${(progress / 1000)}"
                    else -> "0:${(progress / 1000)}"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                musicService.cancelCoroutineRepeat()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    musicService.callCoroutineRepeat(it.progress)
                }
            }
        })
        binding.btnFavorite.setOnClickListener {
            iceMusic?.let {
                if (iceMusic?.uri != null) {
                    Toast.makeText(requireContext(), "This song was been download", Toast.LENGTH_SHORT).show()
                }
                else {
                    val favoriteMusic =  MusicFavorite(
                        0,
                        it.id,
                        it.name,
                        it.artists_names,
                        it.thumbnail,
                        it.duration,
                        null
                    )
                    when (listMusicFavorite.size) {
                        0 -> {
                            favoriteMusic.let {
                                musicFavoriteMusicViewModel.insertMusicFavorite(it)
                            }
                        }
                        else -> {
                            var isFavorite = false
                            for (music in listMusicFavorite) {
                                if (music.name == iceMusic?.name) {
                                    isFavorite = true
                                    musicFavoriteMusicViewModel.deleteMusicFavorite(music)
                                    break
                                }
                            }
                            if (!isFavorite) {
                                favoriteMusic.let { musicFavoriteMusicViewModel.insertMusicFavorite(it) }
                            }
                        }
                    }
                    musicFavoriteMusicViewModel.getFavoriteMusic()
                }
            }
        }
        binding.btnDownload.setOnClickListener {
            checkPermission()
        }

        binding.btnShuffle.setOnClickListener {
            if (musicService.getPlayMode() == KeyWork.PLAY_MODE_SHUFFLE) musicService.setPlayMode(
                KeyWork.PLAY_MODE_NORMAL
            )
            else musicService.setPlayMode(KeyWork.PLAY_MODE_SHUFFLE)
        }
        binding.btnRepeat.setOnClickListener {
            when (musicService.getPlayMode()) {
                KeyWork.PLAY_MODE_NORMAL -> musicService.setPlayMode(KeyWork.PLAY_MODE_REPEAT_ONE)
                KeyWork.PLAY_MODE_REPEAT_ONE -> musicService.setPlayMode(KeyWork.PLAY_MODE_REPEAT)
                KeyWork.PLAY_MODE_REPEAT -> musicService.setPlayMode(KeyWork.PLAY_MODE_NORMAL)
                KeyWork.PLAY_MODE_SHUFFLE -> musicService.setPlayMode(KeyWork.PLAY_MODE_REPEAT_ONE)
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iBinder = service as MusicService.MyBinder
            musicService = iBinder.getMusicService()
            musicService.getMusicPlaying().observe(this@Playing, {
                iceMusic = it
                if (it.uri != null) binding.imgThumbnails.setImageBitmap(
                    getAlbumBitmap(
                        requireContext(),
                        it.uri!!
                    )
                )
                else Picasso.get().load(it.thumbnail.replace("w94", "w320"))
                    .into(binding.imgThumbnails)
                binding.tvNameMusic.text = it.name
                binding.tvArtistsMusic.text = it.artists_names
                musicFavoriteMusicViewModel.getFavoriteMusic()
            })
            musicService.getIsPlayingMusic().observe(this@Playing, {
                when (it) {
                    true -> binding.btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
                    false -> binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            })
            musicService.getDurationMusic().observe(this@Playing, {
                binding.seekBar.max = it
                binding.tvDuration.text = when {
                    (it / 1000) >= 60 && (it / 1000) % 60 < 10 -> "${(it / 1000) / 60}:0${(it / 1000) % 60}"
                    (it / 1000) >= 60 -> "${(it / 1000) / 60}:${(it / 1000) % 60}"
                    (it / 1000) < 10 -> "0:0${(it / 1000)}"
                    else -> "0:${(it / 1000)}"
                }
            })
            musicService.getCurrentMusic().observe(this@Playing, {
                binding.seekBar.progress = it
            })
            musicFavoriteMusicViewModel.getFavoriteMusic()
            musicFavoriteMusicViewModel.listFavorite.observe(this@Playing, { musics ->
                listMusicFavorite.clear()
                listMusicFavorite.addAll(musics)
                var isFavorite = false
                for (favorite in musics) {
                    if (favorite.name == iceMusic?.name) {
                        binding.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
                        isFavorite = true
                        break
                    }
                }
                if (!isFavorite) {
                    binding.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                }
            })
            musicService.getPlayModeLiveData().observe(this@Playing, {
                when (it) {
                    KeyWork.PLAY_MODE_NORMAL -> {
                        binding.btnShuffle.setImageResource(R.drawable.ic_baseline_shuffle_24)
                        binding.btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_24)
                    }
                    KeyWork.PLAY_MODE_REPEAT_ONE -> {
                        binding.btnShuffle.setImageResource(R.drawable.ic_baseline_shuffle_24)
                        binding.btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                    }
                    KeyWork.PLAY_MODE_REPEAT -> {
                        binding.btnShuffle.setImageResource(R.drawable.ic_baseline_shuffle_24)
                        binding.btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_24_all)
                    }
                    KeyWork.PLAY_MODE_SHUFFLE -> {
                        binding.btnShuffle.setImageResource(R.drawable.ic_baseline_shuffle_24_selected)
                        binding.btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_24)
                    }
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissionArr = arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requireActivity().requestPermissions(permissionArr, REQUEST_PERMISSION_CODE)
            } else {
                startDownloadMusic()
            }
        } else {
            startDownloadMusic()
        }
    }

    private fun startDownloadMusic() {
        binding.btnDownload.isEnabled = false
        val url = "http://api.mp3.zing.vn/api/streaming/audio/${iceMusic?.id}/128"
        val request = DownloadManager.Request(Uri.parse(url))
//        val title = URLUtil.guessFileName(url, null, null)
//        val cookie = CookieManager.getInstance().getCookie(title)
//        request.addRequestHeader("cookie", cookie)
        request.setAllowedOverRoaming(true)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "${iceMusic?.name}.mp3"
        )
        val downloadManager =
            requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val idDownload = downloadManager.enqueue(request)
        CoroutineScope(Dispatchers.IO).launch {
            var isDownloading = true
            while (isDownloading) {
                val downloadQuery = DownloadManager.Query().setFilterById(idDownload)
                val cursor: Cursor = downloadManager.query(downloadQuery)
                cursor.moveToFirst()
                val progressDownload =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val totalDownload =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                withContext(Dispatchers.Main) {
                    binding.progressDownload.visibility = View.VISIBLE
                    binding.progressDownload.progress = (progressDownload / totalDownload) * 100
                }
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    isDownloading = false
                    withContext(Dispatchers.Main) {
                        binding.progressDownload.visibility = View.INVISIBLE
                        binding.btnDownload.isEnabled = true
                    }
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownloadMusic()
            } else {
                Toast.makeText(requireContext(), "Download file failed !!", Toast.LENGTH_SHORT)
                    .show()
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