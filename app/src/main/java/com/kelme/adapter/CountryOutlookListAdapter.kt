package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemCountryOutlookBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.CountryOutlookModel
import com.kelme.utils.Utils

/**
 * Created by Amit Gupta on 07-07-2021.
 */
class CountryOutlookListAdapter(
    private var context: Context,
    private var list: ArrayList<CountryOutlookModel>
) : RecyclerView.Adapter<CountryOutlookListAdapter.ViewHolder>() {

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country_outlook, parent, false) as View
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

        private val binding: ItemCountryOutlookBinding? = DataBindingUtil.bind(itemView)

        init {
            view.setOnClickListener { listener?.onClick(adapterPosition, view) }
        }

        fun bind(modal: CountryOutlookModel) {
            binding?.tvName?.text = modal.country_outlook_name
            when (modal.country_outlook_id) {
                "1" -> {
                    Utils.loadImageWithDrawable(context,binding?.imgMap,R.drawable.img_africa)
                }
                "2" -> {
                    Utils.loadImageWithDrawable(context,binding?.imgMap,R.drawable.img_asia)
                }
                "3" -> {
                    //change to america map
                    Utils.loadImageWithDrawable(context,binding?.imgMap,R.drawable.img_america)
                }
                "4" -> {
                    Utils.loadImageWithDrawable(context,binding?.imgMap,R.drawable.img_middle_east)
                }
                "5" -> {
                    Utils.loadImageWithDrawable(context,binding?.imgMap,R.drawable.img_europe)
                }
            }
        }
    }

    fun updateItems(newList: ArrayList<CountryOutlookModel>) {
        list.clear()
        list = newList
        notifyDataSetChanged()
    }
}