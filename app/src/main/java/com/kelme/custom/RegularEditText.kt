package com.kelme.custom

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class RegularEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {
    init {
        val typeface = Typeface.createFromAsset(getContext().assets, "font/Poppins-Regular.ttf")
        setTypeface(typeface)
    }
}