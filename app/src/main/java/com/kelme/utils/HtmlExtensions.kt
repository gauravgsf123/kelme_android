package com.kelme.utils

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan

fun String.html2AttributedString(context: Context): SpannableStringBuilder? {
    return try {
        val html = this.updateHTML

        val attributedString = HtmlRenderer.render(
            context = context,
            html = html
        )

        // Same behavior as iOS:
        // If foreground color is not specified in HTML,
        // use white.
        applyDefaultTextColor(
            attributedString,
            Color.WHITE
        )

        attributedString

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun applyDefaultTextColor(
    text: SpannableStringBuilder,
    defaultColor: Int
) {
    val fullRange = 0 until text.length

    if (text.isEmpty()) return

    // Find all existing foreground color spans
    val existingColorSpans = text.getSpans(
        0,
        text.length,
        ForegroundColorSpan::class.java
    )

    val ranges = mutableListOf<IntRange>()

    existingColorSpans.forEach { span ->
        val start = text.getSpanStart(span)
        val end = text.getSpanEnd(span)

        if (start >= 0 && end > start) {
            ranges.add(start until end)
        }
    }

    // Add white only where there is no existing color span
    var start = 0

    while (start < text.length) {

        val hasColor = ranges.any { range ->
            start in range
        }

        if (hasColor) {
            start++
            continue
        }

        var end = start + 1

        while (
            end < text.length &&
            ranges.none { range -> end in range }
        ) {
            end++
        }

        text.setSpan(
            ForegroundColorSpan(defaultColor),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        start = end
    }
}

val String.updateHTML: String
    get() {
        var updatedHtml = this

        updatedHtml = updatedHtml
            .replace("&nbsp;", "")

        updatedHtml = updatedHtml
            .replace("<br></p>", "</p>")

        updatedHtml = updatedHtml
            .replace("<br></li>", "</li>")

        updatedHtml = updatedHtml
            .replace("</ul>", "</ul><br>")

        return updatedHtml
    }