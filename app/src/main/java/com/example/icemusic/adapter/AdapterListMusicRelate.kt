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
import com.example.icemusic.helper.CommunicationAdapterMusic
import com.example.icemusic.model.IceMusic
import com.squareup.picasso.Picasso

class AdapterListMusicRelate(
    private val listMusic: MutableList<IceMusic>,
    private val listener:CommunicationAdapterMusic,
    val context: Context
) :
    RecyclerView.Adapter<AdapterListMusicRelate.ViewHolder>() {

    class ViewHolder(private val binding: ItemListMusicNomalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val img = binding.imgMusic
        val tvName = binding.tvNameMusic
        val tvArtist = binding.tvArtistsMusic
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
        if (listMusic[position].uri != null) {
            holder.img.setImageBitmap(
                getAlbumBitmap(
                    context,
                    listMusic[position].uri!!
                )
            )
        } else {
            Picasso.get().load(listMusic[position].thumbnail).into(holder.img)
        }
        holder.tvName.text = listMusic[position].name
        holder.tvArtist.text = listMusic[position].artists_names
        holder.itemView.setOnClickListener {
            listener.itemOnClick(position)
        }
    }

    override fun getItemCount(): Int {
        return listMusic.size
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