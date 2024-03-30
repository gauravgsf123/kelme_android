package com.kelme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.kelme.R
import com.kelme.custom.RegularTextView

class NavMenuAdapter(private var c: Context, private var menuList: Array<String>) : BaseAdapter() {

    override fun getCount(): Int   {  return menuList.size  }
    override fun getItem(i: Int): Any {  return menuList[i] }
    override fun getItemId(i: Int): Long { return i.toLong()}

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        if (view == null) {
            //inflate layout resource if its null
            view = LayoutInflater.from(c).inflate(R.layout.menu_list_item, viewGroup, false)
        }
        val menuName = menuList.get(i)
        val name = view?.findViewById<RegularTextView>(R.id.tvName)
        name?.text = menuName
        return view!!
    }
}