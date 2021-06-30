package com.example.icemusic.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.icemusic.databinding.ItemListTop100Binding
import com.example.icemusic.activity.PlayingMusic
import com.example.icemusic.helper.CommunicationAdapterMusic
import com.example.icemusic.model.IceMusic
import com.squareup.picasso.Picasso

class AdapterListTop100Music(val context:Context,private val listMusic: MutableList<IceMusic>,private val listener:CommunicationAdapterMusic) :
    RecyclerView.Adapter<AdapterListTop100Music.ViewHolder>() {

    class ViewHolder(private val binding: ItemListTop100Binding) :
        RecyclerView.ViewHolder(binding.root) {
        val img = binding.imgMusic
        val tvName = binding.tvNameMusic
        val tvArtist = binding.tvArtistsMusic
        val tvRank = binding.tvRank
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemListTop100Binding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get().load(listMusic[position].thumbnail).into(holder.img)
        holder.tvName.text = listMusic[position].name
        holder.tvArtist.text = listMusic[position].artists_names
        holder.tvRank.text = "#${position + 1}"
        holder.itemView.setOnClickListener {
            listener.itemOnClick(position)
        }
    }

    override fun getItemCount(): Int {
        return listMusic.size
    }
}