package com.kelme.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.kelme.R
import com.kelme.activity.chat.FullScreenImageActivity
import com.kelme.databinding.*
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.response.ChatListModelWithName
import com.kelme.model.response.ChatModel
import com.kelme.utils.Constants
import com.kelme.utils.PrefManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Shradha on 05/07/21.
 */

class ChatAdapter(
    private var context: Context,
    private var list: ArrayList<ChatModel?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SEND = 0
        private const val VIEW_TYPE_RECEIVE = 1
        private const val VIEW_TYPE_IMG_SEND = 2
        private const val VIEW_TYPE_IMG_RECEIVE = 3
        private const val VIEW_TYPE_DOC_SEND = 4
        private const val VIEW_TYPE_DOC_RECEIVE = 5
        private const val VIEW_TYPE_DATE = 6
    }

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    class MyViewHolderDateMsg(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {
        var name:String=""
        var name2:String=""
        var binding: ItemChatDateHeaderBinding = ItemChatDateHeaderBinding.bind(itemView)

        init {
            view.setOnClickListener {
                listener?.onClick(adapterPosition, view)
            }
        }

        fun bind(modal: ChatModel) {
            val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
            when (modal.type) {
                "join" -> {
                    if(modal.actionBy==uid) {
                        name=getMemberNameSender(modal)
                        modal.message = "You added $name"
                        binding.tvMessage.text = modal.message
                    }else{
                        name=getMemberNameSender(modal)
                        val actionBy=getMemberNameAction(modal)
                      //  modal.message = "$name added"
                        modal.message = "$actionBy added $name"
                        binding.tvMessage.text = modal.message
                    }
                }
                "left" -> {
                    if(modal.actionBy==uid) {
                        name=getMemberNameSender(modal)
                        modal.message = "You removed $name"
                        binding.tvMessage.text = modal.message
                    }else{
                        name=getMemberNameSender(modal)
                        modal.message = "$name left"
                        binding.tvMessage.text = modal.message
                    }
                }
                "create" -> {
                    if(modal.actionBy==uid) {
                        modal.message = PrefManager.read(PrefManager.NAME, "") + " Created"
                        binding.tvMessage.text = modal.message
                    }else{
                        name=getMemberNameSender(modal)
                        modal.message = "$name Created"
                        binding.tvMessage.text = modal.message
                    }
                }
                else -> {
                    binding.tvMessage.text = modal.message
                }
            }
        }

        private fun getMemberNameSender(modal: ChatModel): String {
            Constants.userList.forEach { user ->
                if (user?.userId == modal.sender) {
                    name = user?.name.toString()
                }
            }
            return name
        }

        private fun getMemberNameAction(modal: ChatModel): String {
            Constants.userList.forEach { user ->
                if (user?.userId == modal.actionBy) {
                    name2 = user?.name.toString()
                }
            }
            return name2
        }
    }

    class MyViewHolderOwnTextMsg(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        var binding: ItemChatTextOwnBinding = ItemChatTextOwnBinding.bind(itemView)

        init {
            view.setOnClickListener {
                listener?.onClick(adapterPosition, view)
            }
        }

        fun bind(modal: ChatModel) {
            binding.tvMessage.text = modal.message

            val date = modal.timestamp?.let { Date(it) }
            val formatter = SimpleDateFormat("hh:mm aa")
            binding.tvTime.text = formatter.format(date).toString()
        }
    }

    class MyViewHolderOtherTextMsg(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        var binding: ItemChatTextOtherBinding = ItemChatTextOtherBinding.bind(itemView)

        init {
            view.setOnClickListener {
                listener?.onClick(adapterPosition, view)
            }
        }

        fun bind(modal: ChatModel) {
            binding.tvMessage.text = modal.message

            val date = modal.timestamp?.let { Date(it) }
            val formatter = SimpleDateFormat("hh:mm aa")
            binding.tvTime.text = formatter.format(date).toString()
        }

    }

    class MyViewHolderOwnImageMsg(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        var binding: ItemChatImageOwnBinding = ItemChatImageOwnBinding.bind(itemView)

        init {
            view.setOnClickListener {
                listener?.onClick(adapterPosition, view)
            }
        }

        fun bind(modal: ChatModel) {

            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()
           // modal.message?.let { Utils.loadImage(context,binding.userMessage, it) }
            Glide.with(context)
                .load(modal.message)
                .centerCrop()
                .placeholder(circularProgressDrawable)
                .into(binding.userMessage)

            val date = modal.timestamp?.let { Date(it) }
            val formatter = SimpleDateFormat("hh:mm aa")

            binding.userTimeStamp.text = formatter.format(date).toString()
            binding.userMessage.setOnClickListener {
                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("image",modal.message)
                context.startActivity(intent)
            }
        }
    }

    class MyViewHolderOtherImageMsg(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        var binding: ItemChatImageOtherBinding = ItemChatImageOtherBinding.bind(itemView)

        init {
            view.setOnClickListener {
                listener?.onClick(adapterPosition, view)
            }
        }

        fun bind(modal: ChatModel) {
           // modal.message?.let { Utils.loadImage(context,binding.ivImage, it) }
            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()
            // modal.message?.let { Utils.loadImage(context,binding.userMessage, it) }
            Glide.with(context)
                .load(modal.message)
                .centerCrop()
                .placeholder(circularProgressDrawable)
                .into(binding.ivImage)

            val date = modal.timestamp?.let { Date(it) }
            val formatter = SimpleDateFormat("hh:mm aa")
            binding.tvTime.text = formatter.format(date).toString()
            binding.ivImage.setOnClickListener {
                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("image",modal.message)
                context.startActivity(intent)
            }

        }
    }

    class MyViewHolderOwnDocMsg(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        var binding: ItemChatDocOwnBinding = ItemChatDocOwnBinding.bind(itemView)

        init {
            view.setOnClickListener {
                listener?.onClick(adapterPosition, view)
            }
        }

        fun bind(modal: ChatModel) {
            val date = modal.timestamp?.let { Date(it) }
            val formatter = SimpleDateFormat("hh:mm aa")
            binding.tvTime.text = formatter.format(date).toString()

            val file = File(modal.message)
            val strFileName = file.name
           // binding.tvMessage.text = strFileName
            binding.tvMessage.text = modal.message?.substring( modal.message?.lastIndexOf('/')!! +1, modal.message!!.length)
            binding.tvMessage.setOnClickListener {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                openURL.data = Uri.parse(modal.message)
                context.startActivity(openURL)
            }
        }
    }

    class MyViewHolderOtherDocMsg(val view: View, val listener: ItemClickListener?, val context: Context) :
        RecyclerView.ViewHolder(view) {

        var binding: ItemChatDocOtherBinding = ItemChatDocOtherBinding.bind(itemView)

        init {
            view.setOnClickListener {
                listener?.onClick(adapterPosition, view)
            }
        }

        fun bind(modal: ChatModel) {
            val date = modal.timestamp?.let { Date(it) }
            val formatter = SimpleDateFormat("hh:mm aa")
            binding.tvTime.text = formatter.format(date).toString()
            val file = File(modal.message)
            val strFileName = file.name
 //           binding.tvMessage.text = strFileName
            binding.tvMessage.text  = modal.message?.substring( modal.message?.lastIndexOf('/')!! +1, modal.message!!.length)
            binding.tvMessage.setOnClickListener {
//                val intent = Intent(context, PdfViewActivity::class.java)
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                intent.putExtra("url",modal.message)
//                context.startActivity(intent)
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                openURL.data = Uri.parse(modal.message)
                context.startActivity(openURL)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SEND -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_text_own, parent, false)
                MyViewHolderOwnTextMsg(view, listener = listener, context = context)
            }
            VIEW_TYPE_RECEIVE -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_text_other, parent, false)
                MyViewHolderOtherTextMsg(view, listener = listener, context = context)
            }
            VIEW_TYPE_IMG_SEND -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_image_own, parent, false)
                MyViewHolderOwnImageMsg(view, listener = listener, context = context)
            }
            VIEW_TYPE_IMG_RECEIVE -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_image_other, parent, false)
                MyViewHolderOtherImageMsg(view, listener = listener, context = context)
            }
            VIEW_TYPE_DOC_SEND -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_doc_own, parent, false)
                MyViewHolderOwnDocMsg(view, listener = listener, context = context)
            }
            VIEW_TYPE_DOC_RECEIVE -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_doc_other, parent, false)
                MyViewHolderOtherDocMsg(view, listener = listener, context = context)
            }
            VIEW_TYPE_DATE -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_date_header, parent, false)
                MyViewHolderDateMsg(view, listener = listener, context = context)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateItems(msgList: ArrayList<ChatModel?>) {
        // list.clear()
        list = msgList
        notifyDataSetChanged()
    }


    override fun getItemViewType(position: Int): Int {
        val sender = list[position]?.sender
        val msgType = list[position]?.type
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")

        if(sender == uid && msgType=="text")
             return VIEW_TYPE_SEND
        else if(sender != uid && msgType=="text")
            return VIEW_TYPE_RECEIVE
        else if(sender == uid && msgType=="image")
            return VIEW_TYPE_IMG_SEND
        else if(sender != uid && msgType=="image")
            return VIEW_TYPE_IMG_RECEIVE
        else if(sender == uid && msgType=="document")
            return VIEW_TYPE_DOC_SEND
        else if(sender != uid && msgType=="document")
            return VIEW_TYPE_DOC_RECEIVE
        else if(msgType=="date"||msgType=="create"||msgType=="join"||msgType=="left")
            return VIEW_TYPE_DATE
        return 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val element = list[position]

        when (holder) {
            is MyViewHolderOwnTextMsg -> holder.bind(element as ChatModel)
            is MyViewHolderOtherTextMsg -> holder.bind(element as ChatModel)
            is MyViewHolderOwnImageMsg -> holder.bind(element as ChatModel)
            is MyViewHolderOtherImageMsg -> holder.bind(element as ChatModel)
            is MyViewHolderOwnDocMsg -> holder.bind(element as ChatModel)
            is MyViewHolderOtherDocMsg -> holder.bind(element as ChatModel)
            is MyViewHolderDateMsg -> holder.bind(element as ChatModel)
            else -> throw IllegalArgumentException()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun filterChatList(userList: ArrayList<ChatListModelWithName?>):ArrayList<ChatListModelWithName?>{
        val filterChatList = ArrayList<ChatListModelWithName?>()
        userList.forEach {model->
            val allChatDeleteTimestamp = model?.chatMemberDetails?.get("")?.allChatDelete
            if(allChatDeleteTimestamp!=null && allChatDeleteTimestamp>0){
                allChatDeleteTimestamp?.let {
                    if(allChatDeleteTimestamp!! >=model.lastUpdate!!){
                        Log.d("filterChatList","01 $allChatDeleteTimestamp ${model.lastUpdate}")
                    }else{
                        filterChatList.add(model)
                    }
                }
            }else{
                filterChatList.add(model)
            }
        }
        return filterChatList
    }
}

