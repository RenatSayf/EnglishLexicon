package com.myapp.lexicon.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.R
import com.myapp.lexicon.database.DataBaseEntry
import kotlinx.android.synthetic.main.a_content_main.view.*

class MainViewPagerAdapter constructor(private val entries: MutableList<DataBaseEntry>) : RecyclerView.Adapter<PagerViewHolder>()
{
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

}

class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)