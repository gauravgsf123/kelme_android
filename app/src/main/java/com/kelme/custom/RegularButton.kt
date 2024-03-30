package com.kelme.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

@SuppressLint("ResourceAsColor")
class RegularButton(context: Context, attrs: AttributeSet?) : AppCompatButton(context, attrs) {
    init {
        val typeface = Typeface.createFromAsset(getContext().assets, "font/Poppins-Regular.ttf")
        setTypeface(typeface)
        isAllCaps = false
    }
}