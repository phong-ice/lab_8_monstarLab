package com.example.icemusic.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import androidx.core.app.NotificationCompat
import com.example.icemusic.R
import com.example.icemusic.helper.CheckNetworkConnected
import com.example.icemusic.helper.KeyWork
import com.example.icemusic.helper.MyApplication.Companion.CHANNEL_ID
import com.example.icemusic.model.IceMusic
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.random.Random

class MusicService : Service() {

    inner class MyBinder : Binder() {
        fun getMusicService(): MusicService = this@MusicService
    }

    private val iBinder = MyBinder()

    private var _position: Int = -1
    private var drawablePlay = R.drawable.ic_baseline_pause_24
    private var mediaPlayer: MediaPlayer? = null
    private val _listMusic: MutableList<IceMusic> = mutableListOf()
    private var _musicCoroutine: Job? = null
    private val _listMusicLiveData = MutableLiveData<MutableList<IceMusic>>()
    private val iceMusicLiveData = MutableLiveData<IceMusic>()
    private val _isPlayingLiveData = MutableLiveData<Boolean>()
    private val _currentMediaPlayer = MutableLiveData<Int>()
    private val _durationMediaPlayer = MutableLiveData<Int>()
    private var _playMode: String = KeyWork.PLAY_MODE_NORMAL
    private val _playModeLiveData: MutableLiveData<String> = MutableLiveData()
    private val pendingIntentNext: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            this,
            1,
            Intent(KeyWork.ACTION_NEXT),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private val pendingIntentPlay: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            this,
            2,
            Intent(KeyWork.ACTION_PLAY),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private val pendingIntentPrevious: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            this,
            3,
            Intent(KeyWork.ACTION_PREVIOUS),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(broadcastReceiver, IntentFilter(KeyWork.ACTION_PREVIOUS))
        registerReceiver(broadcastReceiver, IntentFilter(KeyWork.ACTION_PLAY))
        registerReceiver(broadcastReceiver, IntentFilter(KeyWork.ACTION_NEXT))
    }

    override fun onBind(intent: Intent?): IBinder {
        _playModeLiveData.value = _playMode
        return iBinder
    }

    fun setListMusicService(listMusic: MutableList<IceMusic>, position: Int) {
        val lm: MutableList<IceMusic> = mutableListOf()
        lm.addAll(listMusic)
        _position = position
        iceMusicLiveData.value = listMusic[_position]
        _listMusic.clear()
        _listMusic.addAll(listMusic)
        _listMusicLiveData.value = lm
        onStartMusic()
    }

    private fun onStartMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        if (_listMusic[_position].uri != null) {
            mediaPlayer = MediaPlayer.create(
                this,
                _listMusic[_position].uri
            )
            mediaPlayer?.start()
            _isPlayingLiveData.value = mediaPlayer?.isPlaying
            _durationMediaPlayer.value = mediaPlayer?.duration
        } else {
            if (CheckNetworkConnected.isConnectedInternet(this)) {
                mediaPlayer = MediaPlayer.create(
                    this,
                    Uri.parse("http://api.mp3.zing.vn/api/streaming/audio/${_listMusic[_position].id}/128")
                )
                mediaPlayer?.start()
                _isPlayingLiveData.value = mediaPlayer?.isPlaying
                _durationMediaPlayer.value = mediaPlayer?.duration
            }
        }
        iceMusicLiveData.value = _listMusic[_position]
        drawablePlay = R.drawable.ic_baseline_pause_24
        createNotification()
        setProgressSeekBar()
    }

    fun onNext() {
        when {
            _position < _listMusic.size - 1 -> {
                _position++
                onStartMusic()
            }
        }
    }

    fun onPrevious() {
        when {
            _position > 0 -> {
                _position--
                onStartMusic()
            }
        }
    }

    fun onPauseOrRePlay() {
        when (mediaPlayer?.isPlaying) {
            true -> {
                mediaPlayer?.pause()
                drawablePlay = R.drawable.ic_baseline_play_arrow_24
            }
            false -> {
                mediaPlayer?.start()
                drawablePlay = R.drawable.ic_baseline_pause_24
            }
        }
        _isPlayingLiveData.value = mediaPlayer?.isPlaying
        createNotification()
    }

    fun getMusicPlaying(): MutableLiveData<IceMusic> = iceMusicLiveData
    fun getListMusic(): MutableLiveData<MutableList<IceMusic>> = _listMusicLiveData
    fun getIsPlayingMusic(): MutableLiveData<Boolean> = _isPlayingLiveData
    fun getCurrentMusic(): MutableLiveData<Int> = _currentMediaPlayer
    fun getDurationMusic(): MutableLiveData<Int> = _durationMediaPlayer
    fun getPlayModeLiveData(): MutableLiveData<String> = _playModeLiveData
    fun getPlayMode(): String = _playMode
    fun setPlayMode(playMode: String) {
        _playMode = playMode
        _playModeLiveData.value = _playMode
    }

    fun cancelCoroutineRepeat() = _musicCoroutine?.cancel(null)
    fun callCoroutineRepeat(progress: Int) {
        mediaPlayer?.seekTo(progress)
        setProgressSeekBar()
    }

    private fun setProgressSeekBar() {
        _musicCoroutine?.cancel(null)
        _musicCoroutine = null
        _musicCoroutine = CoroutineScope(Dispatchers.IO).launch {
            mediaPlayer?.let {
                repeat(it.duration - it.currentPosition + 1) {
                    withContext(Dispatchers.Main) {
                        _currentMediaPlayer.value = mediaPlayer?.currentPosition
                    }
                    if ((mediaPlayer!!.currentPosition + 700) > mediaPlayer!!.duration || (mediaPlayer!!.currentPosition + 700) == mediaPlayer!!.duration) {
                        withContext(Dispatchers.Main) {
                            when (_playMode) {
                                KeyWork.PLAY_MODE_NORMAL -> onNext()
                                KeyWork.PLAY_MODE_REPEAT_ONE -> onStartMusic()
                                KeyWork.PLAY_MODE_REPEAT -> {
                                    if (_position == _listMusic.size - 1) _position = 0
                                    else _position++
                                    onStartMusic()
                                }
                                KeyWork.PLAY_MODE_SHUFFLE -> {
                                    _position = Random.nextInt(0, _listMusic.size)
                                    onStartMusic()
                                }
                            }
                        }
                    }
                    delay(1000)
                }

            }
        }
    }

    private fun createNotification() {
        var mBitmap: Bitmap? = null
        if (_listMusic[_position].uri != null) {
            _listMusic[_position].uri?.let {
                mBitmap = getAlbumBitmap(this, it)
            }
        } else {
            Picasso.get().load(_listMusic[_position].thumbnail).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    mBitmap = bitmap
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.round_favorite_24)
            .setContentTitle(_listMusic[_position].name)
            .setContentText(_listMusic[_position].artists_names)
            .addAction(R.drawable.ic_baseline_fast_rewind_24, "Previous", pendingIntentPrevious)
            .addAction(drawablePlay, "Play or pause", pendingIntentPlay)
            .addAction(R.drawable.ic_baseline_fast_forward_24, "Next", pendingIntentNext)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            .setLargeIcon(mBitmap)
            .build()
        startForeground(1, notification)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                KeyWork.ACTION_NEXT -> onNext()
                KeyWork.ACTION_PLAY -> onPauseOrRePlay()
                KeyWork.ACTION_PREVIOUS -> onPrevious()
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