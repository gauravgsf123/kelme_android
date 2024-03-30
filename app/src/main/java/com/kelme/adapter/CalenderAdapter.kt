package com.kelme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemCalenderListBinding
import com.kelme.model.response.CalendarData
import com.kelme.model.response.MonthData


/**
 * Created by Gaurav Kumar on 03/07/21.
 */
class CalenderAdapter : RecyclerView.Adapter<CalenderAdapter.MyViewHolder>() {
    private var securityList = listOf<CalendarData>()
    fun setItems(securityList: ArrayList<CalendarData>) {
        this.securityList = securityList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_calender_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val viewHolder = holder
        viewHolder.bindView(securityList.get(position), position)
    }

    override fun getItemCount(): Int {
        return securityList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemCalenderListBinding = ItemCalenderListBinding.bind(itemView)
        fun bindView(item: CalendarData, i: Int) {
            binding.cvSummary.setOnClickListener {
                if (binding.ivSummaryArrow.rotation.equals(0f)) {
                    binding.ivSummaryArrow.rotation = 180f
                    binding.clDescription.visibility = View.VISIBLE
                } else {
                    binding.ivSummaryArrow.rotation = 0f
                    binding.clDescription.visibility = View.GONE
                }
            }

            binding.tvSummaryTitle.text = item.month

            binding.rvMonthData.adapter = CalenderMonthAdapter()
            (binding.rvMonthData.adapter as CalenderMonthAdapter).setItems(item.month_data as ArrayList<MonthData>)

            /*binding.tvDate.text = item.month_data?.get(position)?.eventMonth
            binding.tvTitle.text = item.month_data?.get(position)?.eventMonth
            binding.tvSummary.text = item.month_data?.get(position)?.eventMonth*/
            /*binding.nameTxt.text = item.name
            binding.stopwatchTxt.text = item.stopwatch
            binding.placeTxt.text = item.place
            binding.priceTxt.text = item.price
            binding.icon.setImageResource(item.icon)*/

        }
    }
}