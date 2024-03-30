package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemUserListBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.ContactModel
import com.kelme.model.response.ChatListModelWithName
import com.kelme.utils.Utils

class AddAdminAdapter (
    var context: Context,
    private var list: ArrayList<ChatListModelWithName>,
) : RecyclerView.Adapter<AddAdminAdapter.MyHolder>() {

    private var selectedPosition = -1

    var checkedItems = ArrayList<ChatListModelWithName>()

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    //VIEWHOLDER IS INITIALIZED
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {

        val rootView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_list, null, false)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rootView.layoutParams = lp
        return MyHolder(rootView,listener)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.setIsRecyclable(false)
        list[position].let { holder.bind(context, it) }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItemCountCheckedItemList(): Int {
        return checkedItems.size
    }

    fun updateItems(newList: ArrayList<ChatListModelWithName>) {
        // list.clear()
        list = newList
        notifyDataSetChanged()
    }

    class MyHolder(itemView: View,listener: ItemClickListener?) : RecyclerView.ViewHolder(itemView) {

        val binding: ItemUserListBinding? = DataBindingUtil.bind(itemView)

        init {
            binding?.root?.setOnClickListener { listener?.onClick(absoluteAdapterPosition,binding?.root) }
        }

        fun bind(context: Context, modal: ChatListModelWithName) {
            binding?.tvName?.text = modal.name
            binding?.checkBox?.isChecked  = modal.isSelected

//            binding?.checkBox?.setOnCheckedChangeListener { _, isChecked ->
//                if (is)
//            }


           // Utils.loadImage(context, binding?.ivProfile, modal.profilePicture)

        }
    }
}