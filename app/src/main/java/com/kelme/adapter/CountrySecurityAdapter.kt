package com.kelme.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemCountrySecurityListBinding
import com.kelme.model.CalenderModel
import com.kelme.model.response.Security


/**
 * Created by Gaurav Kumar on 09/07/21.
 */
class CountrySecurityAdapter : RecyclerView.Adapter<CountrySecurityAdapter.MyViewHolder>() {
    var itemClick: ((Security) -> Unit)? = null
    private var securityList = listOf<Security>()
    fun setItems(securityList: ArrayList<Security>) {
        this.securityList = securityList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_country_security_list, parent, false)
        ).apply {
            itemClick = { i ->
                this@CountrySecurityAdapter.itemClick?.invoke(i)
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val viewHolder = holder
        viewHolder.bindView(securityList.get(position), position)
    }

    override fun getItemCount(): Int {
        return securityList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemClick: ((Security) -> Unit)? = null
        var binding: ItemCountrySecurityListBinding = ItemCountrySecurityListBinding.bind(itemView)
        fun bindView(item: Security, i: Int) {

            /*binding.nameTxt.text = item.name
            binding.stopwatchTxt.text = item.stopwatch
            binding.placeTxt.text = item.place
            binding.priceTxt.text = item.price
            binding.icon.setImageResource(item.icon)*/
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

            itemView.setOnClickListener {
                itemClick?.invoke(item)
            }
        }
    }
}