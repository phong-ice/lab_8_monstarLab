package com.example.icemusic.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.icemusic.databinding.ItemListFolderBinding
import com.example.icemusic.model.Folder

class AdapterListFolder(private val listFolder: MutableList<Folder>) :
    RecyclerView.Adapter<AdapterListFolder.ViewHolder>() {

    class ViewHolder(private val binding: ItemListFolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvTitle = binding.tvTitleFolder
        val imgFolder = binding.imgListFolder
        val background = binding.background
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListFolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTitle.text = listFolder[position].title
        holder.imgFolder.setImageResource(listFolder[position].drawable)
        holder.background.setBackgroundColor(Color.parseColor(listFolder[position].backgroundColor))
    }

    override fun getItemCount(): Int {
        return listFolder.size
    }
}