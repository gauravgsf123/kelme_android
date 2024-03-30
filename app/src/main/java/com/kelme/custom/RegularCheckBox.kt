package com.kelme.custom

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox

class RegularCheckBox(context: Context, attrs: AttributeSet?) : AppCompatCheckBox(context, attrs) {
    init {
        val typeface = Typeface.createFromAsset(getContext().assets, "font/Poppins-Regular.ttf")
        setTypeface(typeface)
    }
}