package com.kelme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemCountrySecurityListBinding
import com.kelme.model.response.Other
import com.kelme.model.response.Security


/**
 * Created by Gaurav Kumar on 09/07/21.
 */
class CountryOtherAdapter : RecyclerView.Adapter<CountryOtherAdapter.MyViewHolder>() {
    private var itemList = listOf<Other>()
    fun setItems(itemList: ArrayList<Other>) {
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_country_security_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val viewHolder = holder
        viewHolder.bindView(itemList.get(position), position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemCountrySecurityListBinding = ItemCountrySecurityListBinding.bind(itemView)
        fun bindView(item: Other, i: Int) {

            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description

            binding.ivArrow.setOnClickListener {
                if(binding.ivArrow.rotation.equals(0f)){
                    binding.ivArrow.rotation = 180f
                    binding.tvDescription.visibility = View.VISIBLE
                }else{
                    binding.ivArrow.rotation = 0f
                    binding.tvDescription.visibility = View.GONE
                }
            }
            binding.tvTitle.setOnClickListener {
                if(binding.ivArrow.rotation.equals(0f)){
                    binding.ivArrow.rotation = 180f
                    binding.tvDescription.visibility = View.VISIBLE
                }else{
                    binding.ivArrow.rotation = 0f
                    binding.tvDescription.visibility = View.GONE
                }
            }


        }
    }
}