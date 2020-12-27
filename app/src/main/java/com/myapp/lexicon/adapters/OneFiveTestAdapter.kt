package com.myapp.lexicon.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.R
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.helpers.Event
import io.reactivex.subjects.BehaviorSubject
import java.lang.IndexOutOfBoundsException

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
        //holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.from_right_to_left_anim)

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

    fun removeItem(position: Int)
    {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

}