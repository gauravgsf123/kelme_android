package com.kelme.utils

import android.content.Context
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.text.style.URLSpan
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.util.Locale

object HtmlRenderer {

    private val BLOCK_TAGS = setOf(
        "p",
        "div",
        "section",
        "article",
        "header",
        "footer",
        "blockquote"
    )

    private const val BASE_PRIORITY = 0

    fun render(context: Context, html: String?): SpannableStringBuilder {
        val output = SpannableStringBuilder()

        if (html.isNullOrBlank()) {
            return output
        }

        return try {
            val document = Jsoup.parseBodyFragment(html)
            renderChildren(context, document.body(), output)
            trimTrailingNewLines(output)
            output
        } catch (e: Exception) {
            output.append(html)
            output
        }
    }

    private fun renderChildren(
        context: Context,
        parent: Node,
        output: SpannableStringBuilder
    ) {
        for (child in parent.childNodes()) {
            renderNode(context, child, output)
        }
    }

    private fun renderNode(
        context: Context,
        node: Node,
        output: SpannableStringBuilder
    ) {
        when (node) {
            is TextNode -> renderTextNode(node, output)
            is Element -> renderElement(context, node, output)
            else -> {
                // Graceful fallback for unknown node types.
            }
        }
    }

    private fun renderTextNode(node: TextNode, output: SpannableStringBuilder) {
        val text = node.text()
        if (text.isNotBlank()) {
            output.append(text)
        }
    }

    private fun renderElement(
        context: Context,
        element: Element,
        output: SpannableStringBuilder
    ) {
        val tag = element.tagName().lowercase(Locale.US)

        when (tag) {
            "small" -> {
                val start = output.length

                renderChildren(context, element, output)

                if (output.length > start) {
                    output.setSpan(
                        AbsoluteSizeSpan(12, true),
                        start,
                        output.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                applyElementStyling(
                    context,
                    element,
                    output,
                    start,
                    output.length,
                    headingLevel = null
                )
            }
            "br" -> {
                output.append("\n")
            }

            "ul" -> {
                renderList(context, element, output, ordered = false)
            }

            "ol" -> {
                renderList(context, element, output, ordered = true)
            }

            "li" -> {
                // Fallback handling if li is encountered outside of list context.
                val start = output.length
                renderChildren(context, element, output)
                applyElementStyling(context, element, output, start, output.length,headingLevel = null)
            }

            "p" -> {
                renderBlockElement(context, element, output, tag = tag)
            }

            "div", "section", "article", "header", "footer", "blockquote" -> {
                renderBlockElement(context, element, output, tag = tag)
            }

            "h1" -> renderHeading(context, element, output, level = 1)
            "h2" -> renderHeading(context, element, output, level = 2)
            "h3" -> renderHeading(context, element, output, level = 3)
            "h4" -> renderHeading(context, element, output, level = 4)
            "h5" -> renderHeading(context, element, output, level = 5)
            "h6" -> renderHeading(context, element, output, level = 6)

            "strong", "b", "i", "u", "span", "a" -> {
                renderInlineElement(context, element, output)
            }

            else -> {
                // Unknown tags should gracefully fallback:
                // Traverse children without crashing, but still apply CSS styles if present.
                val start = output.length
                renderChildren(context, element, output)
                applyElementStyling(context, element, output, start, output.length, headingLevel = null)
            }
        }
    }

    private fun renderInlineElement(
        context: Context,
        element: Element,
        output: SpannableStringBuilder
    ) {
        val start = output.length
        renderChildren(context, element, output)
        applyElementStyling(context, element, output, start, output.length, headingLevel = null)
    }

    private fun renderBlockElement(
        context: Context,
        element: Element,
        output: SpannableStringBuilder,
        tag: String
    ) {
        ensureBlockStart(output)
        val start = output.length
        renderChildren(context, element, output)
        applyElementStyling(context, element, output, start, output.length, headingLevel = null)
        appendBlockSpacing(output)
    }

    private fun renderHeading(
        context: Context,
        element: Element,
        output: SpannableStringBuilder,
        level: Int
    ) {
        ensureBlockStart(output)
        val start = output.length
        renderChildren(context, element, output)

        if (output.length > start) {
            output.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                start,
                output.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            output.setSpan(
                TypefaceSpan("serif"),
                start,
                output.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val headingSize = when (level) {
                1 -> 24
                2 -> 22
                3 -> 20
                4 -> 18
                5 -> 17
                else -> 16
            }

            output.setSpan(
                AbsoluteSizeSpan(headingSize, true),
                start,
                output.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        applyElementStyling(context, element, output, start, output.length, headingLevel = level)
        appendBlockSpacing(output)
    }

    private fun renderList(
        context: Context,
        element: Element,
        output: SpannableStringBuilder,
        ordered: Boolean
    ) {
        ensureBlockStart(output)
        val listStart = output.length

        var itemIndex = 1
        for (child in element.childNodes()) {
            when (child) {
                is Element -> {
                    if (child.tagName().equals("li", ignoreCase = true)) {
                        renderListItem(context, child, output, ordered, itemIndex)
                        itemIndex++
                    } else {
                        renderNode(context, child, output)
                    }
                }

                is TextNode -> {
                    if (child.text().isNotBlank()) {
                        renderNode(context, child, output)
                    }
                }
            }
        }

        applyElementStyling(context, element, output, listStart, output.length, headingLevel = null)
        appendBlockSpacing(output)
    }

    private fun renderListItem(
        context: Context,
        element: Element,
        output: SpannableStringBuilder,
        ordered: Boolean,
        index: Int
    ) {
        ensureLineBreak(output)
        val start = output.length

        if (ordered) {
            output.append("$index. ")
        }

        renderChildren(context, element, output)

        if (output.isNotEmpty() && output.lastChar() != '\n') {
            output.append("\n")
        }

        val end = output.length
        val bulletMargin = HtmlUtils.dpToPx(context, 20f)

        if (ordered) {
            output.setSpan(
                LeadingMarginSpan.Standard(bulletMargin),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            output.setSpan(
                BulletSpan(HtmlUtils.dpToPx(context, 8f)),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            output.setSpan(
                LeadingMarginSpan.Standard(bulletMargin),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        applyElementStyling(context, element, output, start, end, headingLevel = null)
    }

    private fun applyElementStyling(
        context: Context,
        element: Element,
        output: SpannableStringBuilder,
        start: Int,
        end: Int,
        headingLevel: Int?
    ) {
        if (start >= end) return

        val tag = element.tagName().lowercase(Locale.US)
        val styleMap = parseStyleMap(element.attr("style"))

        styleMap["color"]?.let { colorValue ->
            HtmlUtils.parseCssColor(colorValue)?.let { colorInt ->
                output.setSpan(
                    ForegroundColorSpan(colorInt),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        styleMap["font-size"]?.let { fontSizeValue ->
            HtmlUtils.parseCssFontSize(fontSizeValue)?.let { size ->
                output.setSpan(
                    AbsoluteSizeSpan(size, true),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        styleMap["text-align"]?.let { textAlignValue ->
            HtmlUtils.parseTextAlignment(textAlignValue)?.let { alignment ->
                output.setSpan(
                    AlignmentSpan.Standard(alignment),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        } ?: run {
            if (headingLevel != null && headingLevel <= 3) {
                output.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        styleMap["font-family"]?.let { familyValue ->
            val cleaned = familyValue.trim().trim('"', '\'')
            if (cleaned.isNotBlank()) {
                output.setSpan(
                    TypefaceSpan(cleaned),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        when (tag) {
            "strong", "b" -> {
                output.setSpan(
                    StyleSpan(android.graphics.Typeface.BOLD),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            "i" -> {
                output.setSpan(
                    StyleSpan(android.graphics.Typeface.ITALIC),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            "u" -> {
                output.setSpan(
                    UnderlineSpan(),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            "a" -> {
                val href = element.absUrl("href").ifBlank { element.attr("href") }
                if (href.isNotBlank()) {
                    output.setSpan(
                        URLSpan(href),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            "h1", "h2", "h3", "h4", "h5", "h6" -> {
                output.setSpan(
                    StyleSpan(android.graphics.Typeface.BOLD),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                output.setSpan(
                    TypefaceSpan("serif"),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun parseStyleMap(style: String): Map<String, String> {
        if (style.isBlank()) return emptyMap()

        val result = mutableMapOf<String, String>()
        style.split(';').forEach { declaration ->
            val parts = declaration.split(':', limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim().lowercase(Locale.US)
                val value = parts[1].trim()
                if (key.isNotBlank() && value.isNotBlank()) {
                    result[key] = value
                }
            }
        }
        return result
    }

    private fun ensureBlockStart(output: SpannableStringBuilder) {
        if (output.isNotEmpty() && output.lastChar() != '\n') {
            output.append("\n")
        }
    }

    private fun ensureLineBreak(output: SpannableStringBuilder) {
        if (output.isNotEmpty() && output.lastChar() != '\n') {
            output.append("\n")
        }
    }

    private fun appendBlockSpacing(output: SpannableStringBuilder) {
        if (output.isEmpty()) return

        var trailingNewlines = 0
        var index = output.length - 1
        while (index >= 0 && output[index] == '\n') {
            trailingNewlines++
            index--
        }

        when {
            trailingNewlines >= 2 -> return
            trailingNewlines == 1 -> output.append("\n")
            else -> output.append("\n\n")
        }
    }

    private fun trimTrailingNewLines(output: SpannableStringBuilder) {
        while (output.isNotEmpty() && output.lastChar() == '\n') {
            output.delete(output.length - 1, output.length)
        }
    }

    private fun SpannableStringBuilder.lastChar(): Char {
        return this[this.length - 1]
    }
}