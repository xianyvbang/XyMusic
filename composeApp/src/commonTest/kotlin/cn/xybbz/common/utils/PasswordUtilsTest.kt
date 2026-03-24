package cn.xybbz.common.utils

import cn.xybbz.entity.data.EncryptAesData
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertMatches
import kotlin.test.assertNotEquals

@OptIn(ExperimentalEncodingApi::class)
class PasswordUtilsTest {

    @Test
    fun encryptAndDecryptRoundTrip() {
        val plaintext = "XyMusic-Test-Password"

        val encrypted = PasswordUtils.encryptAES(plaintext)

        assertEquals(plaintext, PasswordUtils.decryptAES(encrypted))
    }

    @Test
    fun decryptsLegacyAesCbcSample() {
        val encrypted = EncryptAesData(
            aesKey = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=",
            aesIv = "AAECAwQFBgcICQoLDA0ODw==",
            aesData = "DfvgGGK7JSFadGZUaFYuErlO7+EbkurxRTmRnnTrx2w="
        )

        assertEquals("XyMusic-Test-Password", PasswordUtils.decryptAES(encrypted))
    }

    @Test
    fun encryptAndDecryptEmptyString() {
        val encrypted = PasswordUtils.encryptAES("")

        assertEquals("", PasswordUtils.decryptAES(encrypted))
    }

    @Test
    fun generatedSaltMatchesRequestedByteLength() {
        val salt = PasswordUtils.generateSalt(16)

        assertEquals(16, Base64.decode(salt).size)
    }

    @Test
    fun encryptMd5KeepsHexDigestAndBase64SaltFormat() {
        val encrypted = PasswordUtils.encryptMd5("XyMusic-Test-Password")

        assertMatches(Regex("[0-9a-f]{32}"), encrypted.passwordMd5)
        assertNotEquals("", encrypted.encryptedSalt)
        assertEquals(16, Base64.decode(encrypted.encryptedSalt).size)
    }
}
