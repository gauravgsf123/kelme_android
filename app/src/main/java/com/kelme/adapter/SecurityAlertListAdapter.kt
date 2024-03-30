package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemSecurityAlertListBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.SecurityAlertModel
import com.kelme.utils.Constants
import com.kelme.utils.Utils

class SecurityAlertListAdapter (
    private var context: Context,
    private var list: ArrayList<SecurityAlertModel>
) : RecyclerView.Adapter<SecurityAlertListAdapter.ViewHolder>() {

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_security_alert_list, parent, false) as View
        return ViewHolder(
            view = view,
            listener = listener,
            context = context
        )
    }

    override fun onBindViewHolder(holder: SecurityAlertListAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])

    }

    override fun getItemCount() = list.size


    class ViewHolder(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        private val binding: ItemSecurityAlertListBinding? = DataBindingUtil.bind(itemView)

        init {
            view.setOnClickListener { listener?.onClick(adapterPosition, view) }
        }

        fun bind(modal: SecurityAlertModel) {
            binding?.tvCountry?.text = modal.country_name
             if (modal.title == ""){
                 binding?.tvTitle?.text = "Not Available"
            }else{
                 binding?.tvTitle?.text = modal.title
            }
            binding?.tvPlace?.text = modal.location
            binding?.tvCategory?.text = modal.category_name
            binding?.tvPlace?.isSelected = true
            binding?.tvCategory?.isSelected = true
            binding?.tvStopwatch?.text = Utils.convertTimeStampToDate(modal.created_at.toLong(),"dd MMM yyyy")
            Utils.loadImage(context,binding?.ivCountry,Constants.SERVER_IMAGE_URL+modal.country_flag)
        }
    }

    fun updateItems(newList: ArrayList<SecurityAlertModel>) {
        list.clear()
        list = newList
        notifyDataSetChanged()
    }

    fun updateItemsSearch(newList: ArrayList<SecurityAlertModel>) {
       // list.clear()
        list = newList
        notifyDataSetChanged()
    }
}