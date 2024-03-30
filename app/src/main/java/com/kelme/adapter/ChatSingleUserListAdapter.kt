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
import com.kelme.utils.Utils

class ChatSingleUserListAdapter (
    private var context: Context,
    private var list: ArrayList<ContactModel?>
) : RecyclerView.Adapter<ChatSingleUserListAdapter.ViewHolder>() {

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_list, parent, false) as View
        return ViewHolder(
            view = view,
            listener = listener,
            context = context
        )
    }

    class ViewHolder(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        private val binding: ItemUserListBinding? = DataBindingUtil.bind(itemView)

        init {
            view.setOnClickListener { listener?.onClick(adapterPosition, view)
            }
        }

        fun bind(modal: ContactModel) {

            try {
                binding?.tvName?.text = modal.name
            } catch (e: Exception) {
                binding?.tvName?.text = ""
            }
            // Log.d("TAG", "onDataChange: "+modal.name)
            // binding?.tvCategory?.text = modal.role
            try {
                Utils.loadImage(context,binding?.ivProfile, modal.profilePicture!!)
                binding?.checkBox?.visibility= View.GONE
            } catch (e: Exception) {
                Utils.loadImage(context,binding?.ivProfile, "")
                binding?.checkBox?.visibility= View.GONE
            }
        }
    }

    fun updateItems(newList: ArrayList<ContactModel?>) {
        // list.clear()
        list = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list[position]?.let { holder.bind( it) }
    }

    override fun getItemCount(): Int {
       return list.size
    }
}