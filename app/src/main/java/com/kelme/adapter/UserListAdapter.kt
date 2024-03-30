package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemGroupUserListBinding
import com.kelme.databinding.ItemUserListBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.interfaces.QuantityListner
import com.kelme.model.ContactModel
import com.kelme.utils.Utils

/**
 * Created by Gaurav Kumar on 21/07/21.
 */

class UserListAdapter(
    var context: Context,
    private var list: ArrayList<ContactModel?>,
    var quantityListner: QuantityListner
) : RecyclerView.Adapter<UserListAdapter.MyHolder>() {

    var checkedItems = ArrayList<ContactModel>()

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    //VIEWHOLDER IS INITIALIZED
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {

        val rootView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_group_user_list, null, false)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rootView.layoutParams = lp
        return MyHolder(rootView)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        holder.setIsRecyclable(false)
        list[position]?.let { holder.bind(context, it, checkedItems) }

        holder.binding?.checkBox?.setOnCheckedChangeListener { _, isChecked ->
            if (holder.binding.checkBox.isChecked) {
                list[position]?.let { checkedItems.add(it) }
                list[position]!!.isSelected = true
            } else {
                list[position]!!.isSelected = false
                list[position]?.let { checkedItems.remove(it) }
            }
            quantityListner.onQuantitychanged(checkedItems)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItemCountCheckedItemList(): Int {
        return checkedItems.size
    }

    fun updateItems(newList: ArrayList<ContactModel?>) {
        // list.clear()
        list = newList
        notifyDataSetChanged()
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding: ItemGroupUserListBinding? = DataBindingUtil.bind(itemView)

        fun bind(context: Context, modal: ContactModel, checkedItems: ArrayList<ContactModel>) {

            binding?.checkBox?.isChecked = modal.isSelected

            try {
                binding?.tvName?.text = modal.name
            } catch (e: Exception) {
                binding?.tvName?.text = ""
            }
            try {
                Utils.loadImage(context, binding?.ivProfile, modal.profilePicture!!)
            } catch (e: Exception) {
                Utils.loadImage(context, binding?.ivProfile, "")
            }
        }
    }
}