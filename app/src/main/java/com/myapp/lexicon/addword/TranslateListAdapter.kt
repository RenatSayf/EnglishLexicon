package com.myapp.lexicon.addword

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.R
import com.myapp.lexicon.addword.TranslateListAdapter.ViewHolder
import kotlinx.android.synthetic.main.translate_item_layout.view.*

class TranslateListAdapter(private val translateList: ArrayList<String>) : RecyclerView.Adapter<TranslateListAdapter.ViewHolder>()
{
    //private val translateList: List<TranslateItemModel> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.translate_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int
    {
        return translateList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val translateItemModel = translateList[position]
        holder.itemView.checkItem.isChecked = true
        holder.itemView.textItem.text = translateList[position]
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        init
        {
            itemView.checkItem.setOnClickListener {

            }
        }

    }

}