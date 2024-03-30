package com.kelme.interfaces

import android.view.View

/**
 * Created by Amit on 28,June,2021
 */
interface ItemClickListener {

    fun onClick(position: Int, view: View?=null)

}