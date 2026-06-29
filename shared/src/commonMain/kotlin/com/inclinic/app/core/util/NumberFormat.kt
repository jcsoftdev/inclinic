package com.inclinic.app.core.util

import kotlin.math.roundToLong

/**
 * Multiplatform-safe decimal formatter.
 * Replaces JVM-only "%.2f".format(value) calls in commonMain.
 */
fun Double.formatDecimal(decimals: Int = 2): String {
    val factor = when (decimals) {
        1 -> 10L
        2 -> 100L
        3 -> 1000L
        else -> {
            var f = 1L
            repeat(decimals) { f *= 10 }
            f
        }
    }
    val scaled = (this * factor).roundToLong()
    val intPart = scaled / factor
    val decPart = (scaled % factor).let {
        if (it < 0) -it else it
    }.toString().padStart(decimals, '0')
    return "$intPart.$decPart"
}

/**
 * Formats an integer amount with thousands separators (e.g. 4820 -> "4,820").
 * Used for compact money display where no decimals are shown.
 */
fun Long.formatThousands(): String {
    val negative = this < 0
    val digits = (if (negative) -this else this).toString()
    val sb = StringBuilder()
    val len = digits.length
    for (i in 0 until len) {
        if (i > 0 && (len - i) % 3 == 0) sb.append(',')
        sb.append(digits[i])
    }
    return if (negative) "-$sb" else sb.toString()
}
