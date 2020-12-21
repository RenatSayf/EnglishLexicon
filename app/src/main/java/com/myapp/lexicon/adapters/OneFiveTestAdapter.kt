package com.myapp.lexicon.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.R
import com.myapp.lexicon.database.Word

class OneFiveTestAdapter constructor(private val list: MutableList<Word>) : RecyclerView.Adapter<OneFiveTestAdapter.ViewHolder>()
{
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val linearLayout = LayoutInflater.from(parent.context).inflate(R.layout.answer_btn_layout, parent, false) as LinearLayout
        return ViewHolder(linearLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val itemView = holder.itemView
        val answerView = itemView.findViewById<TextView>(R.id.answerView)
        answerView.text = list[position].english
    }

    override fun getItemCount(): Int
    {
        return list.size
    }
}