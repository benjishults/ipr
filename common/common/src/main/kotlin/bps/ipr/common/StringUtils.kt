package bps.ipr.common

interface StringUtils {
    fun String.abbreviate(maxChars: Int = 25): String =
        require(maxChars > 3) { "maxChars must be greater than 3" }
            .run {
                if (length <= maxChars)
                    this@abbreviate
                else
                    "${substring(0, maxChars - 3)}..."
            }

    companion object : StringUtils
}
