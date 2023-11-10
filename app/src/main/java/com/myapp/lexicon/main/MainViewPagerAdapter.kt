package com.myapp.lexicon.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.R
import com.myapp.lexicon.models.Word

class MainViewPagerAdapter: RecyclerView.Adapter<PagerViewHolder>()
{
    private val list: MutableList<Word> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(list: List<Word>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder
    {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.a_page_layout, parent, false)
        return PagerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int)
    {
        val itemView = holder.itemView
        itemView.findViewById<TextView>(R.id.enTextView)?.let {
            it.text = list[position].english
        }
        itemView.findViewById<TextView>(R.id.ruTextView)?.let {
            it.text = list[position].translate
        }
    }


    override fun getItemCount(): Int
    {
        return list.size
    }

    fun getItem(position: Int) : Word
    {
        return list[position]
    }

    fun getItems() : List<Word>
    {
        return list
    }

    fun getItems(start: Int, end: Int) : List<Word>
    {
        return list.filterIndexed { index, _ -> index in start..end }
    }

}

class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
