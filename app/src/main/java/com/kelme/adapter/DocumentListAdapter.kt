package com.kelme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemDocumentListBinding
import com.kelme.model.response.DocumentData

/**
 * Created by Gaurav Kumar on 13/07/21.
 */
class DocumentListAdapter(
    private val mListener: onClickListener
) : RecyclerView.Adapter<DocumentListAdapter.MyViewHolder>() {
    private var itemList = listOf<DocumentData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_document_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindView(itemList[position], position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemDocumentListBinding = ItemDocumentListBinding.bind(itemView)

        init {
            binding.ivDelete.setOnClickListener {
                mListener.onDeleteItem(adapterPosition)
            }
        }

        fun bindView(item: DocumentData, i: Int) {
            binding.tvFileName.text = item.title
        }
    }

    fun setItems(itemList: ArrayList<DocumentData>) {
        this.itemList = itemList
        notifyDataSetChanged()
    }

    interface onClickListener {
        fun onDeleteItem(position: Int)
    }
}