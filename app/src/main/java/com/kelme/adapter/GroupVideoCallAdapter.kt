package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.databinding.ItemGroupVideoCallLayBinding
import com.kelme.interfaces.ItemClickListener
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas

class GroupVideoCallAdapter (
    var context: Context,
    private var list: ArrayList<Int>,
    private var mRtcEngine: RtcEngine,
    private var listner:OnItemClick
    ) : RecyclerView.Adapter<GroupVideoCallAdapter.MyHolder>() {

    var ustate:Int=0
    var uid:Int=0
    var checkedItems = ArrayList<Int>()

    var listener: ItemClickListener? = null

    fun onItemClick(listener: ItemClickListener) {
        this.listener = listener
    }

    //VIEWHOLDER IS INITIALIZED
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {

        val view: View =
            LayoutInflater.from(context).inflate(R.layout.item_group_video_call_lay, parent, false)
        return MyHolder(view,listener)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.setIsRecyclable(false)
        //list[position].let { holder.bind(context, it) }
        holder.bind(context, list, mRtcEngine,listner,uid,ustate)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateGroupCallItems(newList: java.util.ArrayList<Int>,mNewRtcEngine: RtcEngine) {
        // list.clear()
        mRtcEngine = mNewRtcEngine
        list = newList
        notifyDataSetChanged()
    }

    fun getItemCountCheckedItemList(): Int {
        return checkedItems.size
    }

    fun updateItems(newList: ArrayList<Int>) {
        // list.clear()
        list = newList
        notifyDataSetChanged()
    }

     fun updateBackground(id:Int,state:Int) {
        // list.clear()
         uid=id
         ustate = state
         notifyDataSetChanged()
    }

    class MyHolder(itemView: View,listener: ItemClickListener?) : RecyclerView.ViewHolder(itemView) {

        val binding: ItemGroupVideoCallLayBinding? = DataBindingUtil.bind(itemView)

        init {
            binding?.root?.setOnClickListener { listener?.onClick(absoluteAdapterPosition,binding.root) }
        }

        fun bind(context: Context, newList: ArrayList<Int>, mRtcEngine: RtcEngine,listner:OnItemClick,uid:Int,ustate:Int) {

            val view = RtcEngine.CreateRendererView(context)
            view.setZOrderMediaOverlay(true)
            binding?.groupVideoViewContainer?.addView(view)
            val mRemoteVideo = VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, newList[adapterPosition])
            // Initializes the video view of a remote user.
            mRtcEngine.setupRemoteVideo(mRemoteVideo)

            itemView.setOnClickListener {
               // binding?.groupVideoViewContainer?.setBackgroundColor(Color.CYAN)
                //binding.groupVideoViewContainer.setBackgroundResource(R.drawable.background_white_rounded)
                listner.onClick(mRemoteVideo, binding?.groupVideoViewContainer!!)
            }

//            if(ustate==1 && (newList[adapterPosition]==uid)) {
//                binding?.noImage?.visibility=View.GONE
//            }else if(ustate==2 && (newList[adapterPosition]==uid)){
//                binding?.noImage?.visibility=View.VISIBLE
//            }
        }
    }

    interface OnItemClick{
        fun onClick(mRemoteVideo: VideoCanvas,frameLay: FrameLayout)
    }
}