package com.kelme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemCountrySecurityListBinding
import com.kelme.databinding.ItemRiskLevelListBinding
import com.kelme.model.SubCategory
import com.kelme.model.response.RiskLevelListData


/**
 * Created by Gaurav Kumar on 14/07/21.
 */
class RiskLevelListAdapter : RecyclerView.Adapter<RiskLevelListAdapter.MyViewHolder>() {
    private var itemList = listOf<SubCategory>()
    fun setItems(itemList: ArrayList<SubCategory>) {
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_risk_level_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindView(itemList[position], position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemRiskLevelListBinding = ItemRiskLevelListBinding.bind(itemView)
        fun bindView(item: SubCategory, i: Int) {

            binding.tvTitle.text = item.risk_category_name
            binding.tvDescription.text = item.risk_category_desc

            if (item.isSelected) {
                binding.ivArrow.rotation = 180f
                binding.tvDescription.visibility = View.VISIBLE
                item.isSelected = true
            } else {
                binding.ivArrow.rotation = 0f
                binding.tvDescription.visibility = View.GONE
                item.isSelected = false
            }

            binding.ivArrow.setOnClickListener {
                if (binding.ivArrow.rotation.equals(0f)) {
                    binding.ivArrow.rotation = 180f
                    binding.tvDescription.visibility = View.VISIBLE
                    item.isSelected = true
                } else {
                    binding.ivArrow.rotation = 0f
                    binding.tvDescription.visibility = View.GONE
                    item.isSelected = false
                }
            }
            binding.tvTitle.setOnClickListener {
                if (binding.ivArrow.rotation.equals(0f)) {
                    binding.ivArrow.rotation = 180f
                    binding.tvDescription.visibility = View.VISIBLE
                    item.isSelected = true
                } else {
                    binding.ivArrow.rotation = 0f
                    binding.tvDescription.visibility = View.GONE
                    item.isSelected = false
                }
            }

            when (item.risk_category_name) {
                "Geopolitical" -> {
                    binding.ivIcon.setImageResource(R.drawable.geography)
                }
                "Crime" -> {
                    binding.ivIcon.setImageResource(R.drawable.crime)
                }
                "Terrorism" -> {
                    binding.ivIcon.setImageResource(R.drawable.terrorism)
                }
                else -> {
                    binding.ivIcon.setImageResource(R.drawable.civil_unrest)
                }
            }
        }
    }
}