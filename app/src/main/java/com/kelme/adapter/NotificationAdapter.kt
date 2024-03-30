package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemNotificationBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.NotificationModel
import com.kelme.utils.Utils

class NotificationAdapter(
    private val mContext: Context,
    private var notificationModelList: MutableList<NotificationModel>
) :
    RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {
    //private var notificationModelList: ArrayList<Notifications>? = null

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding: ItemNotificationBinding? = DataBindingUtil.bind(itemView)

        init {
            binding?.tvAttachment?.setOnClickListener { listener?.onClick(absoluteAdapterPosition) }
            binding?.tvTitle?.setOnClickListener { listener?.onClick(absoluteAdapterPosition,view) }
        }

        fun bind(modal: NotificationModel) {
            binding?.tvTitle?.text = modal.message

//            val timeAgo2 = TimeAgo2()
//            val myFinalValue = timeAgo2.covertTimeToText(modal.created_at)
            binding?.tvTime?.text = Utils.convertTimeStampToDateNotification(modal.created_at.toLong())
            if(modal.attachment==""){
                binding?.tvAttachment?.visibility=View.GONE
            }else{
                binding?.tvAttachment?.visibility=View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(mContext).inflate(R.layout.item_notification, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return if (notificationModelList != null)
            notificationModelList.size
        else {
            0
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(notificationModelList[position])
//        val notificationModel = notificationModelList?.get(position)
//
//        notificationModel?.let {
//          // holder.itemView.tvT.text = it.message
//           // holder.itemView.tvDate.text = it.title
//        }
    }

    fun setNotificationList(notificationModelList: ArrayList<NotificationModel>) {
        this.notificationModelList = notificationModelList
        notifyDataSetChanged()
    }

    interface onClickListener {
        fun onDeleteClick(position: Int)
    }

    fun removeAt(position: Int) {
        notificationModelList?.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeItem(position: Int) {
        notificationModelList?.removeAt(position)
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position)
    }

    fun restoreItem(item: NotificationModel?, position: Int) {
        //cartList.
        // notify item added by position
        //notifyItemInserted(position)
    }
}