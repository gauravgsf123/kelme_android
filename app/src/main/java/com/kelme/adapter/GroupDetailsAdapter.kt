package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemGroupMemberListBinding
import com.kelme.databinding.ItemGroupNormalMemberListBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.response.ChatListModelWithName
import com.kelme.utils.PrefManager
import com.kelme.utils.Utils

class GroupDetailsAdapter(
    var context: Context,
    var list: ArrayList<ChatListModelWithName>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ADMIN = 0
        private const val VIEW_TYPE_NORMAL_USER = 1
    }

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    class MyViewHolderNormalUser(
        val view: View,
        val listener: ItemClickListener?,
        val context: Context
    ) :
        RecyclerView.ViewHolder(view) {
        var binding: ItemGroupNormalMemberListBinding = ItemGroupNormalMemberListBinding.bind(itemView)

        init {
            view.setOnClickListener {
                listener?.onClick(adapterPosition, view)
            }
        }

        fun bind(modal: ChatListModelWithName) {
            //   binding.tvMessage.text = modal.message
            binding.tvName.text = modal.name
            binding.btnRemove.text = modal.isAdmin
            Utils.loadImage(context, binding.ivProfile, modal.chatPic!!)
        }
    }

    class MyViewHolderAdmin(
        val view: View,
        val listener: ItemClickListener?,
        val context: Context
    ) :
        RecyclerView.ViewHolder(view) {

        var binding: ItemGroupMemberListBinding = ItemGroupMemberListBinding.bind(itemView)

        init {
            binding.btnRemove.setOnClickListener {
                listener?.onClick(absoluteAdapterPosition, view)
            }
        }

        fun bind(modal: ChatListModelWithName) {
            val createrID = PrefManager.read(PrefManager.CREATER_ID, "")
            val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")

            if (createrID == uid) {
                binding.btnRemove.visibility = View.VISIBLE
            } else {
                binding.btnRemove.visibility = View.INVISIBLE
            }
            // binding?.btnRemove?.isChecked = modal.isSelected
            binding.tvName.text = modal.name
            binding.btnRemove.text = modal.isAdmin

            Utils.loadImage(context, binding.ivProfile, modal.chatPic!!)

        }
    }

    //VIEWHOLDER IS INITIALIZED
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return when (viewType) {
            VIEW_TYPE_ADMIN -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_group_member_list, parent, false)
                MyViewHolderAdmin(view, listener = listener, context = context)
            }
            VIEW_TYPE_NORMAL_USER -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_group_normal_member_list, parent, false)
                MyViewHolderNormalUser(view, listener = listener, context = context)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val chatId = list[position].chatId
        val createrID = PrefManager.read(PrefManager.CREATER_ID, "")
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")

        return if(chatId == createrID)
            VIEW_TYPE_NORMAL_USER
        else
            VIEW_TYPE_ADMIN
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val element = list[position]

        when (holder) {
            is MyViewHolderNormalUser -> holder.bind(element)
            is MyViewHolderAdmin -> holder.bind(element)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateItems(newList: ArrayList<ChatListModelWithName>) {
        // list.clear()
        list = newList
        notifyDataSetChanged()
    }
}