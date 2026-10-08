package com.kelme.utils

import android.content.Context
import android.text.Layout
import android.util.TypedValue
import kotlin.math.roundToInt
import androidx.core.graphics.toColorInt

object HtmlUtils {

    fun parseCssColor(value: String?): Int? {
        if (value.isNullOrBlank()) return null
        return try {
            value.trim().toColorInt()
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun parseCssFontSize(value: String?): Int? {
        if (value.isNullOrBlank()) return null

        val normalized = value.trim().lowercase()
        val regex = Regex("""([0-9]+(?:\.[0-9]+)?)\s*(px|dp|sp)?""")
        val match = regex.find(normalized) ?: return null

        val number = match.groupValues[1].toFloatOrNull() ?: return null
        return number.roundToInt()
    }

    fun parseTextAlignment(value: String?): Layout.Alignment? {
        if (value.isNullOrBlank()) return null

        return when (value.trim().lowercase()) {
            "center" -> Layout.Alignment.ALIGN_CENTER
            "right", "end" -> Layout.Alignment.ALIGN_OPPOSITE
            "left", "start" -> Layout.Alignment.ALIGN_NORMAL
            "justify" -> Layout.Alignment.ALIGN_NORMAL
            else -> null
        }
    }

    fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).roundToInt()
    }

}