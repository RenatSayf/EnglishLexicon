package com.myapp.lexicon.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.R
import com.myapp.lexicon.database.Word

class OneFiveTestAdapter constructor(private val list: ArrayList<Word>) : RecyclerView.Adapter<OneFiveTestAdapter.ViewHolder>()
{
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item)

    private lateinit var listener: ITestAdapterListener

    interface ITestAdapterListener
    {
        fun onItemClickListener(position: Int, word: Word, view: Button)
    }

    fun setOnItemClickListener(listener: ITestAdapterListener)
    {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val linearLayout = LayoutInflater.from(parent.context).inflate(R.layout.answer_btn_layout, parent, false) as Button
        return ViewHolder(linearLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val itemView = holder.itemView
        val answerView = itemView.findViewById<Button>(R.id.answerView)
        answerView.text = list[position].english
        answerView.setOnClickListener {
            try
            {
                val word = list[position]
                listener.onItemClickListener(position, word, answerView)
            }
            catch (e: IndexOutOfBoundsException)
            {
                listener.onItemClickListener(0, list[0], answerView)
            }
        }
    }

    override fun getItemCount(): Int
    {
        return list.size
    }

    fun getItems(): ArrayList<Word>
    {
        return list
    }

    fun removeItem(position: Int): Word
    {
        val word = list.removeAt(position)
        notifyItemRemoved(position)
        return word
    }

    fun removeItem(english: String, translate: String)
    {
        var position: Int = -1
        list.forEachIndexed { i, w ->
            if (w.english == english && w.translate == translate)
            {
                position = i
                return@forEachIndexed
            }
        }
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun addItem(position: Int, word: Word)
    {
        list.add(position, word)
        notifyItemInserted(position)
        notifyItemChanged(position)
    }

}