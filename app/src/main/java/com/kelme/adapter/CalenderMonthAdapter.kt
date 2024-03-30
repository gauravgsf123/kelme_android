package com.kelme.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemCalenderMonthListBinding
import com.kelme.model.response.MonthData
import com.kelme.utils.Utils

/**
 * Created by Gaurav Kumar on 03/07/21.
 */
class CalenderMonthAdapter : RecyclerView.Adapter<CalenderMonthAdapter.MyViewHolder>() {
    private var securityList = listOf<MonthData>()

    fun setItems(securityList: ArrayList<MonthData>) {
        this.securityList = securityList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calender_month_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val viewHolder = holder
        viewHolder.bindView(securityList[position], position)
    }

    override fun getItemCount(): Int {
        return securityList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemCalenderMonthListBinding = ItemCalenderMonthListBinding.bind(itemView)

        @SuppressLint("SetTextI18n")
        fun bindView(item: MonthData, i: Int) {
            binding.tvDate.text = Utils.convertTimeStampToDate(item.event_start!!.toLong(), "dd MMM yyyy")

            if (item.event_end!!.isBlank() || item.event_end == "" || item.event_end.isEmpty() ||
                item.event_start == item.event_end ||item.event_end.equals(0))
                binding.tvEnddate.text = ""
            else
                binding.tvEnddate.text = "-" + " " + Utils.convertTimeStampToDate(item.event_end.toLong(), "dd MMM yyyy")

            binding.tvTitle.text = item.event_title
            binding.tvSummary.text = item.event_description
        }
    }
}