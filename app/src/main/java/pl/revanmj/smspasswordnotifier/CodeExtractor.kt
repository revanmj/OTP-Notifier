package pl.revanmj.smspasswordnotifier

import java.util.regex.Pattern

/**
 * Created by revanmj on 13.01.2017.
 */

object CodeExtractor {
    fun extractCode(message: String, customRegex: String?): String? {
        val regex = "([0-9]{3}( |-)[0-9]{3}( |-)[0-9]{3}|[0-9]{3,4}( |-)[0-9]{3,4}|[0-9]{4,9})"

        val pattern: Pattern = if (customRegex != null && customRegex.isNotEmpty())
            Pattern.compile(customRegex)
        else
            Pattern.compile(regex)

        val matcher = pattern.matcher(message)
        var result = ""

        while (matcher.find()) {
            val tmp = matcher.group()
            if (tmp.length > result.length)
                result = tmp
        }

        return if (result.isNotEmpty()) result else null

    }
}
