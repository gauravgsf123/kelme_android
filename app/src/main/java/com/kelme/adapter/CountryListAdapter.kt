package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemCountryListBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.CountryModel
import com.kelme.utils.Utils

/**
 * Created by Amit Gupta on 07-07-2021.
 */
class CountryListAdapter(
    private var context: Context,
    private var list: ArrayList<CountryModel>
) : RecyclerView.Adapter<CountryListAdapter.ViewHolder>() {

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country_list, parent, false) as View
        return ViewHolder(
            view = view,
            listener = listener,
            context = context
        )
    }

    override fun onBindViewHolder(holder: CountryListAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])

    }

    override fun getItemCount() = list.size


    class ViewHolder(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        private val binding: ItemCountryListBinding? = DataBindingUtil.bind(itemView)

        init {
            view.setOnClickListener { listener?.onClick(adapterPosition, view) }
        }

        fun bind(modal: CountryModel) {
            binding?.tvName?.text = modal.country_name
            Utils.loadImage(context,binding?.ivCountry,modal.country_flag)
        }
    }

    fun updateItems(newList: ArrayList<CountryModel>) {
        list.clear()
        list = newList
        notifyDataSetChanged()
    }
}