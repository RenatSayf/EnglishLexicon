package com.myapp.lexicon.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.R
import com.myapp.lexicon.database.Word
import io.reactivex.Single

class MainViewPagerAdapter constructor(private val list: MutableList<Word>) : RecyclerView.Adapter<PagerViewHolder>()
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
            it.text = list[position].english
        }
        itemView.findViewById<TextView>(R.id.ruTextView)?.let {
            it.text = list[position].translate
        }

        //isEnd.doOnSuccess { true }
    }

    override fun getItemCount(): Int
    {
        return list.size
    }

    fun getItem(position: Int) : Word
    {
        return list[position]
    }

    fun getItems() : MutableList<Word>
    {
        return list
    }

    fun getItems(start: Int, end: Int) : MutableList<Word>
    {
        return list.filterIndexed { index, _ -> index in start..end } as MutableList<Word>
    }

}

class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)