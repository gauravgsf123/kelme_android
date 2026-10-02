package com.kelme.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemSecurityListBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.EventDetails
import com.kelme.utils.HtmlRenderer

class SecurityListAdapter(
    private var context: Context,
    private var list: List<EventDetails>
) : RecyclerView.Adapter<SecurityListAdapter.ViewHolder>() {


    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_security_list, parent, false) as View
        return ViewHolder(
            view = view,
            listener = listener,
            context = context
        )
    }

    override fun onBindViewHolder(holder: SecurityListAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])

    }

    override fun getItemCount() = list.size


    class ViewHolder(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        private val binding: ItemSecurityListBinding? = DataBindingUtil.bind(itemView)

        init {
            view.setOnClickListener { listener?.onClick(adapterPosition, view) }
        }

        fun bind(eventDetails: EventDetails) {
            binding?.tvTitle?.text = eventDetails.title
            binding?.tvSecurityDetails?.text =
                HtmlRenderer.render(context, eventDetails.html_page_desc)
        }
    }

    /*@SuppressLint("NotifyDataSetChanged")
    fun updateItems(newList: ArrayList<EventDetails>) {
        //list.clear()
        list = newList
        notifyDataSetChanged()
    }*/
}