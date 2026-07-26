package com.noteflow.app.ui.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

private val URL_REGEX = Regex("""(https?://[^\s]+)""")
private val EMAIL_REGEX = Regex("""[\w.+-]+@[\w-]+\.[\w.-]+""")
private val PHONE_REGEX = Regex("""\+?\d[\d\- ]{6,}\d""")

/**
 * Parses NoteFlow's inline markup:
 *   **bold**   *italic*   `mono`   ~~strike~~
 * and additionally underlines auto-detected links/emails/phone numbers so
 * they read as tappable, matching Notally's "clickable links" feature.
 */
fun parseMarkupToAnnotatedString(raw: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val markupRegex = Regex("""\*\*(.+?)\*\*|\*(.+?)\*|`(.+?)`|~~(.+?)~~""")
    var lastIndex = 0

    fun appendWithLinkDetection(text: String) {
        var cursor = 0
        val combined = Regex("${URL_REGEX.pattern}|${EMAIL_REGEX.pattern}|${PHONE_REGEX.pattern}")
        for (match in combined.findAll(text)) {
            builder.append(text.substring(cursor, match.range.first))
            builder.withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                append(match.value)
            }
            cursor = match.range.last + 1
        }
        builder.append(text.substring(cursor))
    }

    for (match in markupRegex.findAll(raw)) {
        if (match.range.first > lastIndex) {
            appendWithLinkDetection(raw.substring(lastIndex, match.range.first))
        }
        when {
            match.groupValues[1].isNotEmpty() -> builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }
            match.groupValues[2].isNotEmpty() -> builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append(match.groupValues[2])
            }
            match.groupValues[3].isNotEmpty() -> builder.withStyle(SpanStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)) {
                append(match.groupValues[3])
            }
            match.groupValues[4].isNotEmpty() -> builder.withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                append(match.groupValues[4])
            }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < raw.length) appendWithLinkDetection(raw.substring(lastIndex))

    return builder.toAnnotatedString()
}

/** Wraps [text] in the requested markup token, toggling it off if already wrapped. */
fun toggleMarkup(text: String, token: String): String {
    val wrapped = "$token$text$token"
    return if (text.startsWith(token) && text.endsWith(token) && text.length >= 2 * token.length) {
        text.removePrefix(token).removeSuffix(token)
    } else wrapped
}
