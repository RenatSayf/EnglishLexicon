package com.myapp.lexicon.video.search

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.ItemSearchQueryBinding
import com.myapp.lexicon.video.models.query.ErrorItem
import com.myapp.lexicon.video.models.query.HistoryQuery
import com.myapp.lexicon.video.models.query.ISearchItem
import com.myapp.lexicon.video.models.query.SearchQuery
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class SearchQueryAdapter : ListAdapter<ISearchItem, SearchQueryAdapter.ViewHolder>(object : DiffUtil.ItemCallback<ISearchItem>() {
    override fun areItemsTheSame(oldItem: ISearchItem, newItem: ISearchItem): Boolean {
        return oldItem.text == newItem.text
    }

    override fun areContentsTheSame(oldItem: ISearchItem, newItem: ISearchItem): Boolean {
        return oldItem.text == newItem.text
    }

}) {

    companion object {
        private var instance: SearchQueryAdapter? = null

        fun getInstance(): SearchQueryAdapter {
            return if (instance == null) {
                instance = SearchQueryAdapter()
                instance!!
            }
            else instance!!
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchQueryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ISearchItem) {

            with(binding as ItemSearchQueryBinding) {

                when (item) {
                    is ErrorItem -> {
                        ivThumbnail.visibility = View.GONE
                        tvSuggestion.setCompoundDrawables(null, null, null, null)
                    }
                    is SearchQuery -> {
                        ivThumbnail.visibility = View.GONE
                        tvSuggestion.setCompoundDrawables(ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_search_black, null), null, null, null)
                    }

                    is HistoryQuery -> {
                        ivThumbnail.visibility = View.VISIBLE
                        tvSuggestion.setCompoundDrawables(ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_refresh, null), null, null, null)
                        val thumbnailUri = Uri.parse(item.thumbnailUrl)
                        Picasso.get().load(thumbnailUri)
                            .placeholder(R.drawable.ic_smart_display)
                            .into(ivThumbnail, object : Callback {
                                override fun onSuccess() {

                                }

                                override fun onError(e: Exception?) {

                                }
                            })
                    }
                }
                tvSuggestion.text = item.text
            }
        }
    }
}