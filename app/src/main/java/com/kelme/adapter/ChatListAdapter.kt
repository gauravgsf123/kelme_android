package com.kelme.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kelme.R
import com.kelme.databinding.ItemChatListBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.response.ChatListModelWithName
import com.kelme.utils.Constants
import com.kelme.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Amit Gupta on 08-07-2021.
 */
class ChatListAdapter(
    private var context: Context,
    private var list: ArrayList<ChatListModelWithName?>
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    lateinit var gson: Gson
    val sharedPreferences = context.getSharedPreferences("USER", AppCompatActivity.MODE_PRIVATE)

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_list, parent, false) as View
        return ViewHolder(
            view = view,
            listener = listener,
            context = context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list[position]?.let { holder.bind(it) }

    }

    override fun getItemCount() = list.size

    class ViewHolder(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {
        var counter = 0
        private val binding: ItemChatListBinding? = DataBindingUtil.bind(itemView)

        init {
            view.setOnClickListener {
                listener?.onClick(adapterPosition, view)
                //Constants.userName= binding?.tvName?.getText().toString();
                counter = 0
            }
        }

        fun bind(modal: ChatListModelWithName) {

            if (modal.chatType == "single") {
                binding?.tvName?.text = modal.name
            } else {
                binding?.tvName?.text = modal.chatTitle
            }
            binding?.tvMessageSnippet?.text = modal.chatLastMessage
            modal.chatPic?.let { Utils.loadImage(context, binding?.civProfileImage, it) }

            if(modal.unSeenMsg!!>0)
            {
                Constants.globalChatCount += 1
                binding?.tvUnseenMsgCounter?.visibility=View.VISIBLE
                binding?.tvUnseenMsgCounter?.text=modal.unSeenMsg.toString()
            }else{
                binding?.tvUnseenMsgCounter?.visibility=View.INVISIBLE
            }



            val date = modal.lastUpdate?.let { Date(it) }
            val formatter = if (DateUtils.isToday(modal.lastUpdate!!)) {
                SimpleDateFormat("hh:mm aa")
            } else {
                SimpleDateFormat("dd/MM/yyyy")
            }
            val finalDate = formatter.format(date).toString()
                if(finalDate != "01-01-1970")
                    binding?.tvDateTime?.text = formatter.format(date).toString()
                else
                    binding?.tvDateTime?.text = ""

            //binding?.tvUnseenMsgCounter?.text = modal.unSeenMsg.toString()
        }
    }

    fun updateItems(newList: ArrayList<ChatListModelWithName?>) {
        // list.clear()
        list = newList
        notifyDataSetChanged()
    }
}