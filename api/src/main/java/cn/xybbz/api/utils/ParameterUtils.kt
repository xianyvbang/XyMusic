package cn.xybbz.api.utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ParameterUtils {


    fun buildParameter(key: String, value: String): String {
        // Check for bad strings to prevent endless hours debugging why the server throws http 500 errors
        require(!key.contains('=')) {
            "Key $key can not contain the = character in the authorization header"
        }
        require(!key.contains(',')) {
            "Key $key can not contain the , character in the authorization header"
        }
        require(!key.startsWith('"') && !key.endsWith('"')) {
            "Key $key can not start or end with the \" character in the authorization header"
        }

        // key="value"
        return """${key}="${encodeParameterValue(value)}""""
    }

    private fun encodeParameterValue(raw: String): String = raw
        .trim()
        .replace(Regex("\\n"), " ")
        .encodeUrlParameter()

    fun String.encodeUrlParameter(): String {
        return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
    }
}