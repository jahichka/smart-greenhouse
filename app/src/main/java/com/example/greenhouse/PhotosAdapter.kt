package com.example.greenhouse

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView


import com.bumptech.glide.Glide
class PhotosAdapter(private val photosList: MutableList<Photo>, private val onDeleteClick: (Photo) -> Unit) :
    RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photosList[position])
    }

    override fun getItemCount(): Int = photosList.size

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image_photo)
        private val deleteButton: ImageView = itemView.findViewById(R.id.button_delete_photo)

        fun bind(photo: Photo) {
            Glide.with(itemView.context).load(Uri.parse(photo.uri)).into(imageView)
            deleteButton.setOnClickListener { onDeleteClick(photo) }
        }
    }
}
