package com.myapp.lexicon.video.search

import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.ItemSearchQueryBinding
import com.myapp.lexicon.helpers.throwIfDebug
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

    private var onQueryItemClick: ((SearchQuery) -> Unit?)? = null
    private var onHistoryItemClick: ((item: HistoryQuery) -> Unit)? = null

    fun setOnQueryItemClick(onClick: (item: SearchQuery) -> Unit) {
        this.onQueryItemClick = onClick
    }

    fun setOnHistoryItemClick(onClick: (item: HistoryQuery) -> Unit) {
        this.onHistoryItemClick = onClick
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
                        ivFirstIcon.visibility = View.GONE
                        tvSuggestion.gravity = Gravity.CENTER
                    }
                    is SearchQuery -> {
                        ivThumbnail.visibility = View.GONE
                        tvSuggestion.gravity = Gravity.START and Gravity.CENTER_VERTICAL
                        ivFirstIcon.apply {
                            visibility = View.VISIBLE
                            setImageResource(R.drawable.ic_search_black)
                        }
                        layoutQuery.setOnClickListener {
                            this@SearchQueryAdapter.onQueryItemClick?.invoke(item)?: run { Exception("***** onQueryItemClick not registered *******").throwIfDebug()}
                        }
                    }

                    is HistoryQuery -> {
                        ivThumbnail.visibility = View.VISIBLE
                        ivFirstIcon.apply {
                            visibility = View.VISIBLE
                            setImageResource(R.drawable.ic_refresh)
                        }
                        tvSuggestion.gravity = Gravity.START and Gravity.CENTER_VERTICAL
                        val thumbnailUri = Uri.parse(item.thumbnailUrl)
                        Picasso.get().load(thumbnailUri)
                            .placeholder(R.drawable.ic_smart_display)
                            .into(ivThumbnail, object : Callback {
                                override fun onSuccess() {

                                }

                                override fun onError(e: Exception?) {

                                }
                            })
                        layoutQuery.setOnClickListener {
                            this@SearchQueryAdapter.onHistoryItemClick?.invoke(item)?: run { Exception("***** onQueryItemClick not registered *******").throwIfDebug()}
                        }
                    }
                }
                tvSuggestion.text = item.text
            }
        }
    }
}