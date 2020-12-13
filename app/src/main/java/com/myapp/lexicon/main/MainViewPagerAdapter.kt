package com.myapp.lexicon.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.R
import com.myapp.lexicon.database.DataBaseEntry
import kotlinx.android.synthetic.main.a_content_main.view.*

class MainViewPagerAdapter constructor() : RecyclerView.Adapter<PagerViewHolder>()
{
    var counters: MutableMap<String, Int> = mutableMapOf()
    var entries : MutableList<DataBaseEntry> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder
    {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.a_page_layout, parent, false)
        return PagerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int)
    {
        val itemView = holder.itemView
        itemView.findViewById<TextView>(R.id.enTextView)?.let {
            it.text = entries[position].english
        }
        itemView.findViewById<TextView>(R.id.ruTextView)?.let {
            it.text = entries[position].translate
        }
    }

    override fun getItemCount(): Int
    {
        return entries.size
    }

    fun getItem(position: Int) : DataBaseEntry
    {
        return entries[position]
    }

    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver)
    {
        super.registerAdapterDataObserver(observer)
    }
}

class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)