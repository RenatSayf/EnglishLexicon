package com.myapp.lexicon.video.web.bookmarks

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.ItemSearchQueryBinding
import com.myapp.lexicon.video.models.Bookmark
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class BookmarkAdapter: ListAdapter<Bookmark, BookmarkAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Bookmark>() {
    override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
        return oldItem.url == newItem.url && oldItem.title == newItem.title
    }

}) {

    private var onClick: ((Bookmark) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchQueryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    fun setItemClickListener(onClick: (bookmark: Bookmark) -> Unit = {}) {
        this.onClick = onClick
    }

    inner class ViewHolder(private val binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Bookmark) {
            with(binding as ItemSearchQueryBinding) {

                layoutQuery.setOnClickListener {
                    onClick?.invoke(item)?: run { NullPointerException("OnClick listener not resolved") }
                }

                ivFirstIcon.setImageResource(R.drawable.ic_play_arrow_32)
                tvSuggestion.text = item.title
                val thumbnailUri = Uri.parse(item.thumbnailUrl)
                Picasso.get().load(thumbnailUri)
                    .placeholder(R.drawable.ic_image_place_holder)
                    .into(ivThumbnail, object : Callback {
                        override fun onSuccess() {

                        }

                        override fun onError(e: Exception?) {

                        }
                    })
            }
        }
    }
}