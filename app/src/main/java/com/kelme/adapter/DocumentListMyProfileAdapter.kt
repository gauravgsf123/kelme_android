package com.kelme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemDocumentListBinding
import com.kelme.databinding.ItemDocumentListInMyprofileBinding
import com.kelme.model.AlertModel
import com.kelme.model.response.DocumentData


/**
 * Created by Gaurav Kumar on 14/07/21.
 */
class DocumentListMyProfileAdapter : RecyclerView.Adapter<DocumentListMyProfileAdapter.MyViewHolder>() {
    var imageViewClick: ((DocumentData) -> Unit)? = null
    var downloadClick: ((DocumentData) -> Unit)? = null
    private var itemList = listOf<DocumentData>()
    fun setItems(itemList: ArrayList<DocumentData>) {
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_document_list_in_myprofile, parent, false)
        ).apply {
            imageViewClick = { i ->
                this@DocumentListMyProfileAdapter.imageViewClick?.invoke(i)
            }
            downloadClick={i->
                this@DocumentListMyProfileAdapter.downloadClick?.invoke(i)
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindView(itemList[position], position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageViewClick: ((DocumentData) -> Unit)? = null
        var downloadClick: ((DocumentData) -> Unit)? = null
        var binding: ItemDocumentListInMyprofileBinding = ItemDocumentListInMyprofileBinding.bind(itemView)
        fun bindView(item: DocumentData, i:Int) {


            binding.tvName.text = item.title

            binding.ivView.setOnClickListener {
                imageViewClick?.invoke(item)
            }
            binding.ivDownload.setOnClickListener {
                downloadClick?.invoke(item)
            }






        }
    }
}