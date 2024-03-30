package com.kelme.utils

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.adapter.NotificationAdapter


/**
 * Created by Gaurav Kumar on 07/07/21.
 */
class RecyclerItemTouchHelper(
    val dragDirs: Int,
    val swipeDirs: Int,
    val listener: RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
): ItemTouchHelper.SimpleCallback(dragDirs,swipeDirs) {
    //private var listener: RecyclerItemTouchHelperListener? = null
    /*fun RecyclerItemTouchHelper(
        dragDirs: Int,
        swipeDirs: Int,
        listener: RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
    ) {
        super(dragDirs, swipeDirs)
        listener = listener
    }*/



    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            val foregroundView: View = (viewHolder as NotificationAdapter.MyViewHolder).itemView.findViewById(
                R.id.cv_forground)
            getDefaultUIUtil().onSelected(foregroundView)
        }
    }

    /*fun onChildDrawOver(
        c: Canvas?, recyclerView: RecyclerView?,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val foregroundView: View = (viewHolder as NotificationAdapter.MyViewHolder).itemView.findViewById(
            R.id.cv_forground)
        getDefaultUIUtil().onDrawOver(
            c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive
        )
    }

    override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder) {
        val foregroundView: View = (viewHolder as NotificationAdapter.MyViewHolder).itemView.findViewById(
            R.id.cv_forground)
        getDefaultUIUtil().clearView(foregroundView)
    }

    fun onChildDraw(
        c: Canvas?, recyclerView: RecyclerView?,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val foregroundView: View = (viewHolder as NotificationAdapter.MyViewHolder).itemView.findViewById(
            R.id.cv_forground)
        getDefaultUIUtil().onDraw(
            c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive
        )
    }*/

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    interface RecyclerItemTouchHelperListener {
        fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int)
    }
}