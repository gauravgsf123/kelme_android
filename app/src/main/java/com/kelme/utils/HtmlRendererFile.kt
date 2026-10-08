package com.kelme.utils

import android.content.Context
import android.graphics.Color
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.graphics.Typeface
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import kotlin.math.roundToInt

object HtmlRendererFile {

    /**
     * Represents the formatting inherited from parent HTML elements.
     *
     * Example:
     *
     * <div style="color:white;font-size:16px">
     *      <span style="font-size:12px">
     *          Small text
     *      </span>
     * </div>
     *
     * The span will inherit white color but override font size to 12.
     */
    private data class RenderStyle(
        val fontSize: Int? = null,
        val color: Int? = null,
        val fontFamily: String? = null,
        val bold: Boolean = false,
        val italic: Boolean = false,
        val underline: Boolean = false,
        val strikeThrough: Boolean = false,
        val alignment: Layout.Alignment? = null,
        val superscript: Boolean = false,
        val subscript: Boolean = false,
        val code: Boolean = false
    )

    // -------------------------------------------------------------------------
    // PUBLIC
    // -------------------------------------------------------------------------

    /**
     * Main HTML rendering function.
     *
     * Returns SpannableStringBuilder which can directly be assigned to:
     *
     * textView.text = HtmlRenderer.render(context, html)
     */
    fun render(
        context: Context,
        html: String
    ): SpannableStringBuilder {

        if (html.isBlank()) {
            return SpannableStringBuilder()
        }

        val cleanedHtml = prepareHtml(html)

        val document = Jsoup.parseBodyFragment(cleanedHtml)

        val output = SpannableStringBuilder()

        /*
         * iOS implementation uses white as the fallback color
         * when HTML doesn't specify a foreground color.
         */
        val rootStyle = RenderStyle(
            color = Color.WHITE
        )

        renderChildren(
            context = context,
            parent = document.body(),
            output = output,
            inheritedStyle = rootStyle
        )

        removeExtraNewLines(output)

        return output
    }

    // -------------------------------------------------------------------------
    // HTML PREPARATION
    // -------------------------------------------------------------------------

    /**
     * Similar cleanup to the iOS updateHTML implementation.
     */
    private fun prepareHtml(html: String): String {

        return html
            .replace("&nbsp;", " ")
            .replace("\u00A0", " ")
            .replace("<br></p>", "</p>")
            .replace("<br></p>", "</p>")
            .replace("<br></li>", "</li>")
    }

    // -------------------------------------------------------------------------
    // CHILDREN
    // -------------------------------------------------------------------------

    private fun renderChildren(
        context: Context,
        parent: Element,
        output: SpannableStringBuilder,
        inheritedStyle: RenderStyle
    ) {

        for (node in parent.childNodes()) {

            when (node) {

                is TextNode -> {

                    renderText(
                        node = node,
                        output = output,
                        style = inheritedStyle
                    )
                }

                is Element -> {

                    renderElement(
                        context = context,
                        element = node,
                        output = output,
                        inheritedStyle = inheritedStyle
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // ELEMENT
    // -------------------------------------------------------------------------

    private fun renderElement(
        context: Context,
        element: Element,
        output: SpannableStringBuilder,
        inheritedStyle: RenderStyle
    ) {

        val tag = element.tagName().lowercase()

        /*
         * IMPORTANT:
         *
         * Resolve current element style BEFORE rendering children.
         *
         * This is what prevents:
         *
         * parent 16px -> child 12px
         *
         * from becoming 16px again.
         */
        val currentStyle = resolveStyle(
            element = element,
            inherited = inheritedStyle
        )

        when (tag) {

            // -------------------------------------------------------------
            // BR
            // -------------------------------------------------------------

            "br" -> {
                output.append("\n")
                return
            }

            // -------------------------------------------------------------
            // META
            // -------------------------------------------------------------

            "meta" -> {
                return
            }

            // -------------------------------------------------------------
            // HEAD
            // -------------------------------------------------------------

            "head" -> {
                return
            }

            // -------------------------------------------------------------
            // STYLE / SCRIPT
            // -------------------------------------------------------------

            "style",
            "script" -> {
                return
            }

            // -------------------------------------------------------------
            // LIST
            // -------------------------------------------------------------

            "ul" -> {

                renderList(
                    context = context,
                    element = element,
                    output = output,
                    inheritedStyle = currentStyle,
                    ordered = false
                )

                return
            }

            "ol" -> {

                renderList(
                    context = context,
                    element = element,
                    output = output,
                    inheritedStyle = currentStyle,
                    ordered = true
                )

                return
            }

            // -------------------------------------------------------------
            // LIST ITEM
            // -------------------------------------------------------------

            "li" -> {

                renderListItem(
                    context = context,
                    element = element,
                    output = output,
                    inheritedStyle = currentStyle,
                    ordered = false,
                    index = 1
                )

                return
            }

            // -------------------------------------------------------------
            // ANCHOR
            // -------------------------------------------------------------

            "a" -> {

                val start = output.length

                renderChildren(
                    context = context,
                    parent = element,
                    output = output,
                    inheritedStyle = currentStyle
                )

                val end = output.length

                val href = element.attr("href")

                if (
                    href.isNotBlank() &&
                    end > start
                ) {

                    output.setSpan(
                        URLSpan(href),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                return
            }

            // -------------------------------------------------------------
            // TABLE
            // -------------------------------------------------------------

            "table" -> {

                renderTable(
                    context = context,
                    table = element,
                    output = output,
                    inheritedStyle = currentStyle
                )

                return
            }

            // -------------------------------------------------------------
            // TABLE ROW
            // -------------------------------------------------------------

            "tbody",
            "thead",
            "tfoot",
            "tr",
            "td",
            "th" -> {

                renderChildren(
                    context = context,
                    parent = element,
                    output = output,
                    inheritedStyle = currentStyle
                )

                if (
                    tag == "tr" &&
                    !output.endsWith("\n")
                ) {
                    output.append("\n")
                }

                return
            }

            // -------------------------------------------------------------
            // SUPERSCRIPT
            // -------------------------------------------------------------

            "sup" -> {

                val start = output.length

                renderChildren(
                    context = context,
                    parent = element,
                    output = output,
                    inheritedStyle = currentStyle.copy(
                        superscript = true
                    )
                )

                val end = output.length

                if (end > start) {

                    output.setSpan(
                        SuperscriptSpan(),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    output.setSpan(
                        RelativeSizeSpan(0.75f),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                return
            }

            // -------------------------------------------------------------
            // SUBSCRIPT
            // -------------------------------------------------------------

            "sub" -> {

                val start = output.length

                renderChildren(
                    context = context,
                    parent = element,
                    output = output,
                    inheritedStyle = currentStyle.copy(
                        subscript = true
                    )
                )

                val end = output.length

                if (end > start) {

                    output.setSpan(
                        SubscriptSpan(),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    output.setSpan(
                        RelativeSizeSpan(0.75f),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                return
            }

            // -------------------------------------------------------------
            // BLOCK / INLINE ELEMENTS
            // -------------------------------------------------------------

            "html",
            "body",
            "div",
            "p",
            "span",
            "strong",
            "b",
            "i",
            "em",
            "u",
            "s",
            "strike",
            "del",
            "code",
            "h1",
            "h2",
            "h3",
            "h4",
            "h5",
            "h6" -> {

                val start = output.length

                renderChildren(
                    context = context,
                    parent = element,
                    output = output,
                    inheritedStyle = currentStyle
                )

                val end = output.length

                /*
                 * Alignment is applied after children are rendered.
                 *
                 * This is safe because alignment doesn't replace
                 * child font/color/size spans.
                 */
                if (
                    currentStyle.alignment != null &&
                    end > start
                ) {

                    output.setSpan(
                        AlignmentSpan.Standard(
                            currentStyle.alignment
                        ),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                /*
                 * Add newline only for block elements.
                 */
                if (
                    isBlockElement(tag) &&
                    end > start &&
                    !output.endsWith("\n")
                ) {
                    output.append("\n")
                }

                return
            }

            // -------------------------------------------------------------
            // UNKNOWN ELEMENT
            // -------------------------------------------------------------

            else -> {

                renderChildren(
                    context = context,
                    parent = element,
                    output = output,
                    inheritedStyle = currentStyle
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // TEXT
    // -------------------------------------------------------------------------

    private fun renderText(
        node: TextNode,
        output: SpannableStringBuilder,
        style: RenderStyle
    ) {

        var text = node.text()

        if (text.isEmpty()) {
            return
        }

        /*
         * Convert HTML NBSP to normal spaces.
         */
        text = text
            .replace('\u00A0', ' ')
            .replace("\r\n", "\n")
            .replace("\r", "\n")

        /*
         * Jsoup can leave formatting whitespace around block elements.
         *
         * Don't completely trim text because spaces between inline
         * elements are important.
         */
        if (text.isBlank()) {
            return
        }

        val start = output.length

        output.append(text)

        val end = output.length

        applyStyle(
            output = output,
            start = start,
            end = end,
            style = style
        )
    }

    // -------------------------------------------------------------------------
    // STYLE RESOLUTION
    // -------------------------------------------------------------------------

    /**
     * Combines parent style + current HTML element style.
     *
     * Child properties override parent properties.
     */
    private fun resolveStyle(
        element: Element,
        inherited: RenderStyle
    ): RenderStyle {

        var style = inherited

        val css = parseStyleMap(
            element.attr("style")
        )

        // -------------------------------------------------------------
        // COLOR
        // -------------------------------------------------------------

        css["color"]?.let { value ->

            parseCssColor(value)?.let { color ->

                style = style.copy(
                    color = color
                )
            }
        }

        // -------------------------------------------------------------
        // FONT SIZE
        // -------------------------------------------------------------

        css["font-size"]?.let { value ->

            parseCssFontSize(value)?.let { size ->

                style = style.copy(
                    fontSize = size
                )
            }
        }

        // -------------------------------------------------------------
        // FONT FAMILY
        // -------------------------------------------------------------

        css["font-family"]?.let { value ->

            val family = extractFontFamily(value)

            if (family.isNotBlank()) {

                style = style.copy(
                    fontFamily = family
                )
            }
        }

        // -------------------------------------------------------------
        // FONT WEIGHT
        // -------------------------------------------------------------

        css["font-weight"]?.let { value ->

            if (
                value.equals("bold", ignoreCase = true) ||
                value == "600" ||
                value == "700" ||
                value == "800" ||
                value == "900"
            ) {

                style = style.copy(
                    bold = true
                )
            }

            if (
                value.equals("normal", ignoreCase = true) ||
                value == "400"
            ) {

                style = style.copy(
                    bold = false
                )
            }
        }

        // -------------------------------------------------------------
        // FONT STYLE
        // -------------------------------------------------------------

        css["font-style"]?.let { value ->

            when {

                value.equals("italic", ignoreCase = true) -> {

                    style = style.copy(
                        italic = true
                    )
                }

                value.equals("normal", ignoreCase = true) -> {

                    style = style.copy(
                        italic = false
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // TEXT DECORATION
        // -------------------------------------------------------------

        css["text-decoration"]?.let { value ->

            if (
                value.contains(
                    "underline",
                    ignoreCase = true
                )
            ) {

                style = style.copy(
                    underline = true
                )
            }

            if (
                value.contains(
                    "line-through",
                    ignoreCase = true
                )
            ) {

                style = style.copy(
                    strikeThrough = true
                )
            }
        }

        // -------------------------------------------------------------
        // TEXT ALIGN
        // -------------------------------------------------------------

        css["text-align"]?.let { value ->

            parseTextAlignment(value)?.let { alignment ->

                style = style.copy(
                    alignment = alignment
                )
            }
        }

        // -------------------------------------------------------------
        // HTML TAG FORMATTING
        // -------------------------------------------------------------

        when (element.tagName().lowercase()) {

            "strong",
            "b" -> {

                style = style.copy(
                    bold = true
                )
            }

            "i",
            "em" -> {

                style = style.copy(
                    italic = true
                )
            }

            "u" -> {

                style = style.copy(
                    underline = true
                )
            }

            "s",
            "strike",
            "del" -> {

                style = style.copy(
                    strikeThrough = true
                )
            }

            "small" -> {

                /*
                 * Only use 12px if the HTML didn't already specify
                 * another font size.
                 */
                if (css["font-size"] == null) {

                    style = style.copy(
                        fontSize = 12
                    )
                }
            }

            "code" -> {

                style = style.copy(
                    code = true
                )
            }

            // ---------------------------------------------------------
            // HEADINGS
            // ---------------------------------------------------------

            "h1" -> {

                if (css["font-size"] == null) {

                    style = style.copy(
                        fontSize = 24
                    )
                }

                style = style.copy(
                    bold = true,
                    alignment = style.alignment
                        ?: Layout.Alignment.ALIGN_CENTER
                )
            }

            "h2" -> {

                if (css["font-size"] == null) {

                    style = style.copy(
                        fontSize = 22
                    )
                }

                style = style.copy(
                    bold = true,
                    alignment = style.alignment
                        ?: Layout.Alignment.ALIGN_CENTER
                )
            }

            "h3" -> {

                if (css["font-size"] == null) {

                    style = style.copy(
                        fontSize = 20
                    )
                }

                style = style.copy(
                    bold = true,
                    alignment = style.alignment
                        ?: Layout.Alignment.ALIGN_CENTER
                )
            }

            "h4" -> {

                if (css["font-size"] == null) {

                    style = style.copy(
                        fontSize = 18
                    )
                }

                style = style.copy(
                    bold = true
                )
            }

            "h5" -> {

                if (css["font-size"] == null) {

                    style = style.copy(
                        fontSize = 17
                    )
                }

                style = style.copy(
                    bold = true
                )
            }

            "h6" -> {

                if (css["font-size"] == null) {

                    style = style.copy(
                        fontSize = 16
                    )
                }

                style = style.copy(
                    bold = true
                )
            }
        }

        return style
    }

    // -------------------------------------------------------------------------
    // APPLY STYLE
    // -------------------------------------------------------------------------

    private fun applyStyle(
        output: SpannableStringBuilder,
        start: Int,
        end: Int,
        style: RenderStyle
    ) {

        if (start >= end) {
            return
        }

        // -------------------------------------------------------------
        // COLOR
        // -------------------------------------------------------------

        style.color?.let { color ->

            output.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // -------------------------------------------------------------
        // FONT SIZE
        // -------------------------------------------------------------

        style.fontSize?.let { size ->

            output.setSpan(
                AbsoluteSizeSpan(
                    size,
                    true
                ),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // -------------------------------------------------------------
        // FONT FAMILY
        // -------------------------------------------------------------

        var fontFamily = style.fontFamily

        if (
            fontFamily.isNullOrBlank() &&
            style.code
        ) {
            fontFamily = "monospace"
        }

        fontFamily?.let { family ->

            output.setSpan(
                TypefaceSpan(family),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // -------------------------------------------------------------
        // BOLD / ITALIC
        // -------------------------------------------------------------

        when {

            style.bold && style.italic -> {

                output.setSpan(
                    StyleSpan(Typeface.BOLD_ITALIC),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            style.bold -> {

                output.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            style.italic -> {

                output.setSpan(
                    StyleSpan(Typeface.ITALIC),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // -------------------------------------------------------------
        // UNDERLINE
        // -------------------------------------------------------------

        if (style.underline) {

            output.setSpan(
                UnderlineSpan(),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // -------------------------------------------------------------
        // STRIKE THROUGH
        // -------------------------------------------------------------

        if (style.strikeThrough) {

            output.setSpan(
                StrikethroughSpan(),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // -------------------------------------------------------------
        // SUPERSCRIPT
        // -------------------------------------------------------------

        if (style.superscript) {

            output.setSpan(
                SuperscriptSpan(),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            output.setSpan(
                RelativeSizeSpan(0.75f),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // -------------------------------------------------------------
        // SUBSCRIPT
        // -------------------------------------------------------------

        if (style.subscript) {

            output.setSpan(
                SubscriptSpan(),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            output.setSpan(
                RelativeSizeSpan(0.75f),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    // -------------------------------------------------------------------------
    // LIST
    // -------------------------------------------------------------------------

    private fun renderList(
        context: Context,
        element: Element,
        output: SpannableStringBuilder,
        inheritedStyle: RenderStyle,
        ordered: Boolean
    ) {

        var index = 1

        for (child in element.children()) {

            if (
                child.tagName().equals(
                    "li",
                    ignoreCase = true
                )
            ) {

                renderListItem(
                    context = context,
                    element = child,
                    output = output,
                    inheritedStyle = inheritedStyle,
                    ordered = ordered,
                    index = index
                )

                index++
            }
        }

        if (!output.endsWith("\n")) {
            output.append("\n")
        }
    }

    private fun renderListItem(
        context: Context,
        element: Element,
        output: SpannableStringBuilder,
        inheritedStyle: RenderStyle,
        ordered: Boolean,
        index: Int
    ) {

        val itemStyle = resolveStyle(
            element = element,
            inherited = inheritedStyle
        )

        val start = output.length

        /*
         * Keep the bullet/number outside the styled text range.
         */
        if (ordered) {

            output.append(
                "$index. "
            )

        } else {

            output.append(
                "• "
            )
        }

        val textStart = output.length

        renderChildren(
            context = context,
            parent = element,
            output = output,
            inheritedStyle = itemStyle
        )

        val textEnd = output.length

        /*
         * Leading margin controls indentation.
         */
        if (textEnd > start) {

            output.setSpan(
                LeadingMarginSpan.Standard(
                    dpToPx(
                        context = context,
                        dp = if (ordered) 24 else 20
                    )
                ),
                start,
                textEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        /*
         * Make bullet/number inherit the first text color.
         */
        itemStyle.color?.let { color ->

            output.setSpan(
                ForegroundColorSpan(color),
                start,
                textStart,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        if (!output.endsWith("\n")) {
            output.append("\n")
        }
    }

    // -------------------------------------------------------------------------
    // TABLE
    // -------------------------------------------------------------------------

    /**
     * Basic HTML table support.
     *
     * SpannableStringBuilder cannot create a real HTML table layout.
     *
     * This renders table rows/cells as text while preserving the content.
     *
     * If you need exact table borders/widths like Safari, the table should
     * be rendered as a separate Android View rather than Spannable.
     */
    private fun renderTable(
        context: Context,
        table: Element,
        output: SpannableStringBuilder,
        inheritedStyle: RenderStyle
    ) {

        val rows = table.select("tr")

        if (rows.isEmpty()) {
            return
        }

        for (row in rows) {

            val cells = row.select("> th, > td")

            if (cells.isEmpty()) {
                continue
            }

            for ((index, cell) in cells.withIndex()) {

                if (index > 0) {
                    output.append("    ")
                }

                renderChildren(
                    context = context,
                    parent = cell,
                    output = output,
                    inheritedStyle = inheritedStyle
                )
            }

            output.append("\n")
        }

        output.append("\n")
    }

    // -------------------------------------------------------------------------
    // CSS PARSING
    // -------------------------------------------------------------------------

    private fun parseStyleMap(
        style: String
    ): Map<String, String> {

        if (style.isBlank()) {
            return emptyMap()
        }

        return style
            .split(";")
            .mapNotNull { declaration ->

                val parts = declaration.split(
                    ":",
                    limit = 2
                )

                if (parts.size != 2) {
                    return@mapNotNull null
                }

                val property = parts[0]
                    .trim()
                    .lowercase()

                val value = parts[1]
                    .trim()

                if (property.isBlank() || value.isBlank()) {
                    null
                } else {
                    property to value
                }
            }
            .toMap()
    }

    // -------------------------------------------------------------------------
    // CSS COLOR
    // -------------------------------------------------------------------------

    private fun parseCssColor(
        value: String
    ): Int? {

        val color = value
            .trim()
            .lowercase()

        return try {

            when {

                color.startsWith("#") -> {

                    Color.parseColor(color)
                }

                color.startsWith("rgb(") -> {

                    parseRgbColor(color)
                }

                color.startsWith("rgba(") -> {

                    parseRgbaColor(color)
                }

                else -> {

                    Color.parseColor(color)
                }
            }

        } catch (_: Exception) {

            null
        }
    }

    private fun parseRgbColor(
        value: String
    ): Int? {

        return try {

            val content = value
                .substringAfter("(")
                .substringBefore(")")

            val parts = content
                .split(",")
                .map { it.trim() }

            if (parts.size != 3) {
                return null
            }

            Color.rgb(
                parts[0].toInt(),
                parts[1].toInt(),
                parts[2].toInt()
            )

        } catch (_: Exception) {

            null
        }
    }

    private fun parseRgbaColor(
        value: String
    ): Int? {

        return try {

            val content = value
                .substringAfter("(")
                .substringBefore(")")

            val parts = content
                .split(",")
                .map { it.trim() }

            if (parts.size != 4) {
                return null
            }

            val alpha = if (
                parts[3].contains(".")
            ) {

                (
                        parts[3]
                            .toFloat()
                            .coerceIn(0f, 1f) * 255f
                        ).roundToInt()

            } else {

                parts[3]
                    .toInt()
                    .coerceIn(0, 255)
            }

            Color.argb(
                alpha,
                parts[0].toInt(),
                parts[1].toInt(),
                parts[2].toInt()
            )

        } catch (_: Exception) {

            null
        }
    }

    // -------------------------------------------------------------------------
    // CSS FONT SIZE
    // -------------------------------------------------------------------------

    private fun parseCssFontSize(
        value: String
    ): Int? {

        val clean = value
            .trim()
            .lowercase()

        /*
         * Supports:
         *
         * 12px
         * 12
         * 12dp
         * 12sp
         */
        val match = Regex(
            """(-?\d+(?:\.\d+)?)\s*(px|dp|sp)?"""
        ).find(clean)

        return match
            ?.groupValues
            ?.getOrNull(1)
            ?.toFloatOrNull()
            ?.roundToInt()
    }

    // -------------------------------------------------------------------------
    // FONT FAMILY
    // -------------------------------------------------------------------------

    private fun extractFontFamily(
        value: String
    ): String {

        /*
         * Example:
         *
         * Arial,sans-serif
         *
         * becomes:
         *
         * Arial
         *
         * Example:
         *
         * Times New Roman,Times,serif
         *
         * becomes:
         *
         * Times New Roman
         */

        return value
            .split(",")
            .firstOrNull()
            ?.trim()
            ?.removeSurrounding("\"")
            ?.removeSurrounding("'")
            ?: ""
    }

    // -------------------------------------------------------------------------
    // TEXT ALIGNMENT
    // -------------------------------------------------------------------------

    private fun parseTextAlignment(
        value: String
    ): Layout.Alignment? {

        return when (
            value
                .trim()
                .lowercase()
        ) {

            "left",
            "start" -> {
                Layout.Alignment.ALIGN_NORMAL
            }

            "right",
            "end" -> {
                Layout.Alignment.ALIGN_OPPOSITE
            }

            "center" -> {
                Layout.Alignment.ALIGN_CENTER
            }

            "justify" -> {
                Layout.Alignment.ALIGN_NORMAL
            }

            else -> {
                null
            }
        }
    }

    // -------------------------------------------------------------------------
    // BLOCK ELEMENT
    // -------------------------------------------------------------------------

    private fun isBlockElement(
        tag: String
    ): Boolean {

        return tag in setOf(
            "html",
            "body",
            "div",
            "p",
            "ul",
            "ol",
            "li",
            "table",
            "tr",
            "h1",
            "h2",
            "h3",
            "h4",
            "h5",
            "h6"
        )
    }

    // -------------------------------------------------------------------------
    // DP
    // -------------------------------------------------------------------------

    private fun dpToPx(
        context: Context,
        dp: Int
    ): Int {

        return (
                dp *
                        context.resources.displayMetrics.density
                ).roundToInt()
    }

    // -------------------------------------------------------------------------
    // CLEAN EXTRA NEWLINES
    // -------------------------------------------------------------------------

    private fun removeExtraNewLines(
        output: SpannableStringBuilder
    ) {

        /*
         * Remove excessive new lines at the end.
         */
        while (
            output.isNotEmpty() &&
            output.last() == '\n'
        ) {

            output.delete(
                output.length - 1,
                output.length
            )
        }
    }
}