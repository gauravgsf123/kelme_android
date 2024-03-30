package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemCountryPickerListBinding
import com.kelme.databinding.ItemCountryPickerListGlobalBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.CountryModel
import com.kelme.model.GlobalCountryModel
import com.kelme.utils.Utils

class GlobalCountryPickerListAdapter(private var context: Context,
                                     private var list: ArrayList<GlobalCountryModel>
) : RecyclerView.Adapter<GlobalCountryPickerListAdapter.ViewHolder>() {

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country_picker_list_global, parent, false) as View
        return ViewHolder(
            view = view,
            listener = listener,
            context = context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])

    }

    override fun getItemCount() = list.size


    class ViewHolder(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        private val binding: ItemCountryPickerListGlobalBinding? = DataBindingUtil.bind(itemView)

        init {
            view.setOnClickListener { listener?.onClick(adapterPosition, view) }
        }

        fun bind(modal: GlobalCountryModel) {
            binding?.tvName?.text = modal.country_name+"("+modal.country_code+")"
        }
    }

    fun updateItems(newList: ArrayList<GlobalCountryModel>) {
        list.clear()
        list = newList
        notifyDataSetChanged()
    }
}