package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemNotificationBinding
import com.kelme.model.NotificationModel

/**
 * Created by Gaurav Kumar on 07/07/21.
 */
class NotificationListAdapter(context:Context, cartList:ArrayList<NotificationModel>):
    RecyclerView.Adapter<NotificationListAdapter.MyViewHolder>() {
    private val context: Context = context
    private val cartList: ArrayList<NotificationModel> = cartList
    //private lateinit var binding: ItemNotificationBinding
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding = ItemNotificationBinding.bind(view)

        init {
            /*name = view.findViewById(R.id.name)
            description = view.findViewById(R.id.description)
            price = view.findViewById(R.id.price)
            thumbnail = view.findViewById(R.id.thumbnail)
            viewBackground = view.findViewById(R.id.view_background)
            viewForeground = view.findViewById(R.id.view_foreground)*/
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: NotificationListAdapter.MyViewHolder,
        position: Int
    ) {
        val item: NotificationModel = cartList[position]
        /*holder.name.setText(item.getName())
        holder.description.setText(item.getDescription())
        holder.price.setText("₹" + item.getPrice())
        Glide.with(context)
            .load(item.getThumbnail())
            .into(holder.thumbnail)*/
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    fun removeItem(position: Int) {
        cartList.removeAt(position)
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