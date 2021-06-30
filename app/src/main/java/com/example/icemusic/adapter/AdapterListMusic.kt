package com.example.icemusic.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.icemusic.R
import com.example.icemusic.databinding.ItemListMusicBinding
import com.example.icemusic.databinding.ItemListMusicNomalBinding
import com.example.icemusic.model.IceMusic
import com.squareup.picasso.Picasso

class AdapterListMusic(
    val context: Context,
    private val listMusic: MutableList<IceMusic>,
    val itemOnClick: (Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var idAudioPlaying: String = "unKnow"

    class ViewHolderPlaying(binding: ItemListMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val img = binding.imgMusic
        val tvName = binding.tvNameMusic
        val tvArtist = binding.tvArtistsMusic
    }

    class ViewHolderNormal(binding: ItemListMusicNomalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val img = binding.imgMusic
        val tvName = binding.tvNameMusic
        val tvArtist = binding.tvArtistsMusic
    }


    fun setIdMusicPlaying(idMusic: String) {
        idAudioPlaying = idMusic
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> ViewHolderPlaying(
                ItemListMusicBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> ViewHolderNormal(
                ItemListMusicNomalBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            1 -> {
                val holderPlaying = holder as ViewHolderPlaying
                if (listMusic[position].uri != null) {
                    holderPlaying.img.setImageBitmap(
                        getAlbumBitmap(
                            context,
                            listMusic[position].uri!!
                        )
                    )
                } else {
                    Picasso.get().load(listMusic[position].thumbnail).into(holderPlaying.img)
                }
                holderPlaying.tvName.text = listMusic[position].name
                holderPlaying.tvArtist.text = listMusic[position].artists_names
            }
            else -> {
                val holderNormal = holder as ViewHolderNormal
                if (listMusic[position].uri != null) {
                    holderNormal.img.setImageBitmap(
                        getAlbumBitmap(
                            context,
                            listMusic[position].uri!!
                        )
                    )
                } else {
                    Picasso.get().load(listMusic[position].thumbnail).into(holderNormal.img)
                }
                holderNormal.tvName.text = listMusic[position].name
                holderNormal.tvArtist.text = listMusic[position].artists_names
                holderNormal.itemView.setOnClickListener {
                    itemOnClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listMusic.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (listMusic[position].id) {
            idAudioPlaying -> 1
            else -> 2
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