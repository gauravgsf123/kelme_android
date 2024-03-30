package com.kelme.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.activity.chat.UserDetailsActivity
import com.kelme.databinding.ItemContactListBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.ContactUserDetailsModel
import com.kelme.utils.Constants
import com.kelme.utils.Utils
/**
 * Created by Amit Gupta on 08-07-2021.
 */
class ContactListAdapter(
    private var context: Context,
    private var list: ArrayList<ContactUserDetailsModel>
) : RecyclerView.Adapter<ContactListAdapter.ViewHolder>() {

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact_list, parent, false) as View
        return ViewHolder(
            view = view,
            listener = listener,
            context = context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    class ViewHolder(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        private val binding: ItemContactListBinding? = DataBindingUtil.bind(itemView)

        init {
            binding?.ivChat?.setOnClickListener { listener?.onClick(adapterPosition, view) }
        }

        fun bind(modal: ContactUserDetailsModel) {
            binding?.tvName?.text = modal.name
            if (modal.image?.isNotEmpty() == true ||modal.image!="" || modal.image!="assets\\/uploads\\/images\\/") {
                modal.image?.let { Utils.loadImage(context, binding?.ivCountry, Constants.BASE_URL+it) }
//                binding?.let {
//                    Glide.with(context)
//                        .load(Constants.SERVER_URL+modal.image)
//                        .into(it.ivCountry)
//                }
            }

            binding?.tvName?.setOnClickListener {
                val intent = Intent(context, UserDetailsActivity::class.java)
                intent.putExtra("userId", modal.fireBaseId)
                context.startActivity(intent)
            }

            binding?.ivCountry?.setOnClickListener {
                val intent = Intent(context, UserDetailsActivity::class.java)
                intent.putExtra("userId", modal.fireBaseId)
                context.startActivity(intent)
            }
        }
    }

    fun updateItems(newList: ArrayList<ContactUserDetailsModel>) {
        list.clear()
        list = newList
        notifyDataSetChanged()
    }

    fun updateItemsTemp(newList: ArrayList<ContactUserDetailsModel>) {
        // list.clear()
        list = newList
        notifyDataSetChanged()
    }
}