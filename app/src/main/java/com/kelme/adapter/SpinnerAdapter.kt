package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kelme.R
import com.kelme.model.response.RiskLevelListData

/**
 * Created by Amit Gupta on 13-07-2021.
 */
class SpinnerAdapter(val context: Context, val list: ArrayList<RiskLevelListData>) : BaseAdapter() {

//    fun spinnerAdapter() {
//        this.list = list
//        this.context = context
//
//    }

//    private var context: Context? = null
//    private var list: ArrayList<String>? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View = LayoutInflater.from(context).inflate(R.layout.item_spinner, parent, false)

        if (position == 0) {
            val tvName: TextView = view as TextView
            ContextCompat.getColor(context,R.color.gray).let { tvName.setTextColor(it)
                tvName.text= list[position].name}
        } else {
            val tvName: TextView = view as TextView
            context.resources.getColor(R.color.black).let { tvName.setTextColor(it)
                tvName.text= list[position].name}
        }
        return view
    }


    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return 123456789
    }

    override fun getCount(): Int {
        return list.size
    }
}