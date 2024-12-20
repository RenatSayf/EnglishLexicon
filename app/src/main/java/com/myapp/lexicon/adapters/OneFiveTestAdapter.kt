package com.myapp.lexicon.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.R
import com.myapp.lexicon.helpers.onDebouncedListener
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.models.Word

class OneFiveTestAdapter : RecyclerView.Adapter<OneFiveTestAdapter.ViewHolder>()
{
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item)

    private val list: MutableList<Word> = mutableListOf()
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
        answerView.onDebouncedListener {
            try
            {
                val word = list[position]
                listener.onItemClickListener(position, word, answerView)
            }
            catch (e: IndexOutOfBoundsException)
            {
                val word = list.firstOrNull()
                if (word != null) {
                    listener.onItemClickListener(0, word, answerView)
                }
            }
        }
    }

    override fun getItemCount(): Int
    {
        return list.size
    }

    fun getItems(): MutableList<Word>
    {
        return list
    }

    fun removeItem(english: String, translate: String) {

        try {
            if (list.size == 1) {
                list.clear()
            }
            else {
                val position = list.indexOfFirst { item ->
                    item.english == english && item.translate == translate
                }
                list.removeAt(position)
                notifyItemRemoved(position)
            }
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
        }
    }

    fun addItem(position: Int, word: Word)
    {
        list.add(position, word)
        notifyItemInserted(position)
        notifyItemChanged(position)
    }

    fun addItems(list: List<Word>) {
        this.list.addAll(list)
    }

}