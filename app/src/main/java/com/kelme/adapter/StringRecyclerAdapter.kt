package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kelme.R
import com.kelme.databinding.ItemChatListBinding
import com.kelme.databinding.ItemContactListBinding
import com.kelme.databinding.ItemCountryListBinding
import com.kelme.databinding.ItemSpinnerBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.ChatListModel
import com.kelme.model.ContactModel
import com.kelme.model.CountryModel
import com.kelme.utils.Utils

/**
 * Created by Amit Gupta on 08-07-2021.
 */
class StringRecyclerAdapter(
    private var context: Context,
    private var list: ArrayList<String>
) : RecyclerView.Adapter<StringRecyclerAdapter.ViewHolder>() {

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spinner, parent, false) as View
        return ViewHolder(
            view = view,
            listener = listener,
            context = context
        )
    }

    override fun onBindViewHolder(holder: StringRecyclerAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])

    }

    override fun getItemCount() = list.size


    class ViewHolder(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        private val binding: ItemSpinnerBinding? = DataBindingUtil.bind(itemView)

        init {
            view.setOnClickListener { listener?.onClick(adapterPosition, view) }
        }

        fun bind(modal: String) {
            binding?.tvName?.text = modal
        }
    }

    fun updateItems(newList: ArrayList<String>) {
        list.clear()
        list = newList
        notifyDataSetChanged()
    }
}