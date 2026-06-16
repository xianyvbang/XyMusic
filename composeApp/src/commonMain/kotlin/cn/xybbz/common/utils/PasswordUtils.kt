package cn.xybbz.common.utils

import cn.xybbz.entity.data.Md5EncryptData
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.MD5
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlin.io.encoding.Base64


object PasswordUtils {
    private val provider by lazy { CryptographyProvider.Default }

    @OptIn(DelicateCryptographyApi::class)
    private val md5Hasher by lazy { provider.get(MD5).hasher() }

    /**
     * 将password进行MD5加盐加密
     */
    fun encryptMd5(password: String): Md5EncryptData {

        val salt = generateSalt()
        val messageDigest = md5Hasher.hashBlocking((password + salt).encodeToByteArray())
        val passwordMd5 = toHex(messageDigest)
        return Md5EncryptData(salt, passwordMd5)
    }

    private fun toHex(hash: ByteArray): String {
        val buf = StringBuilder(hash.size * 2)

        var i = 0
        while (i < hash.size) {
            if ((hash[i].toInt() and 0xff) < 0x10) {
                buf.append("0")
            }
            buf.append((hash[i].toInt() and 0xff).toString(16))
            i++
        }
        return buf.toString()
    }

    // 生成指定字节数的随机盐
    fun generateSalt(length: Int = 16): String {
        val salt = CryptographyRandom.nextBytes(length)
        return Base64.encode(salt) // Base64编码便于存储
    }
}
