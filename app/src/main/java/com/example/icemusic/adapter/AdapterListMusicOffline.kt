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
import com.example.icemusic.databinding.ItemListMusicNomalBinding
import com.example.icemusic.model.IceMusic

class AdapterListMusicOffline(
    val context: Context,
    private val listMusicOffline: MutableList<IceMusic>,
    val itemOnClick: (Int) -> Unit
) :
    RecyclerView.Adapter<AdapterListMusicOffline.ViewHolder>() {

    class ViewHolder(val binding: ItemListMusicNomalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val img = binding.imgMusic
        val tvName = binding.tvNameMusic
        val tvArtists = binding.tvArtistsMusic
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListMusicNomalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvArtists.text = listMusicOffline[position].artists_names
        holder.tvName.text = listMusicOffline[position].name
        listMusicOffline[position].uri?.let {
            holder.img.setImageBitmap(getAlbumBitmap(context, it))
        }
        holder.itemView.setOnClickListener {
            itemOnClick(position)
        }

    }

    override fun getItemCount(): Int {
        return listMusicOffline.size
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