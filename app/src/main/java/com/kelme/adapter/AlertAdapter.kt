package com.kelme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemAlertListBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.response.SecrityAlertListData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Gaurav Kumar on 03/07/21.
 */
class AlertAdapter : RecyclerView.Adapter<AlertAdapter.ViewHolder>() {

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    private var itemList = listOf<SecrityAlertListData>()

    fun setItems(itemList: ArrayList<SecrityAlertListData>) {
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert_list, parent, false) as View
        return ViewHolder(
            view = view,
            listener = listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(itemList[position], position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(view: View,val listener: ItemClickListener?) : RecyclerView.ViewHolder(view)
    {
        var binding: ItemAlertListBinding = ItemAlertListBinding.bind(itemView)
        init
        {
            view.setOnClickListener { listener?.onClick(adapterPosition, view) }
        }

        fun bindView(item: SecrityAlertListData, i: Int) {

            if (item.title == "") {
                binding.nameTxt.text = "Not Available"
            } else {
                binding.nameTxt.text = item.title
            }

            binding.tvTime.text = getDateTime(item.createdAt)
            try {
                val string = item.location
                val parts = string.split(",".toRegex()).toTypedArray()
                binding.tvLocation.text = parts[0]
            } catch (e: Exception) {
                binding.tvLocation.text = item.location
                e.printStackTrace()
            }

            binding.tvRiskType.text = item.categoryName

            binding.tvLocation.isSelected=true
            binding.tvRiskType.isSelected=true
        }

        private fun getDateTime(s: String): String? {
            return try {
                val sdf = SimpleDateFormat("dd MMM yyyy")
                val netDate = Date(s.toLong() * 1000)
                sdf.format(netDate)
            } catch (e: Exception) {
                e.toString()
            }
        }
    }


}