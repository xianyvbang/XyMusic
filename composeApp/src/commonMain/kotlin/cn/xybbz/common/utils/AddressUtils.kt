package cn.xybbz.common.utils

fun String.extractPortOrNull(): Int? {
    val normalizedAddress = trim()
    if (normalizedAddress.isBlank()) {
        return null
    }

    val addressWithoutScheme = normalizedAddress.substringAfter("://", normalizedAddress)
    val authority = addressWithoutScheme
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
        .substringAfterLast('@')

    if (authority.isBlank()) {
        return null
    }

    val portText = when {
        authority.startsWith("[") -> {
            val closingBracketIndex = authority.indexOf(']')
            if (closingBracketIndex == -1) {
                return null
            }
            authority.substring(closingBracketIndex + 1).takeIf { it.startsWith(":") }?.drop(1)
        }

        authority.count { it == ':' } == 1 -> authority.substringAfterLast(':')
        else -> null
    } ?: return null

    if (portText.isEmpty() || !portText.all(Char::isDigit)) {
        return null
    }

    val port = portText.toIntOrNull() ?: return null
    return port.takeIf { it in 0..65535 }
}
