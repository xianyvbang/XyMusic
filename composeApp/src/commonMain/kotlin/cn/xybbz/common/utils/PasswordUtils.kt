package cn.xybbz.common.utils

import cn.xybbz.entity.data.EncryptAesData
import cn.xybbz.entity.data.Md5EncryptData
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


object PasswordUtils {

    /**
     * 加密
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun encryptAES(data: String): EncryptAesData {
        val plaintext: ByteArray = data.toByteArray()
        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        val key = keygen.generateKey()

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext: ByteArray = cipher.doFinal(plaintext)
        val iv: ByteArray = cipher.iv

        return EncryptAesData(
            aesKey = Base64.encode(key.encoded),
            aesIv = Base64.encode(iv),
            aesData = Base64.encode(ciphertext)
        )
    }

    /**
     * 解密
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun decryptAES(data: EncryptAesData): String {
        val decode = Base64.decode(data.aesData)
        val plaintext: ByteArray = decode
        val iv = Base64.decode(data.aesIv)
        val keyEncoded = Base64.decode(data.aesKey)
        val key = SecretKeySpec(keyEncoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec)
        val ciphertext: ByteArray = cipher.doFinal(plaintext)
        return String(ciphertext)
    }


    /**
     * 将password进行MD5加盐加密
     */
    fun encryptMd5(password: String): Md5EncryptData {

        val salt = generateSalt()
        val tmpPassword = password + salt
        val algorithm = MessageDigest.getInstance("MD5")
        algorithm.reset()
        algorithm.update(tmpPassword.toByteArray(Charsets.UTF_8))
        val messageDigest = algorithm.digest()
        val passwordMd5 = toHex(messageDigest)
        return Md5EncryptData(salt, passwordMd5)
    }

    private fun toHex(hash: ByteArray): String {

        val buf = StringBuffer(hash.size * 2)

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
    @OptIn(ExperimentalEncodingApi::class)
    fun generateSalt(length: Int = 16): String {
        val random = SecureRandom()
        val salt = ByteArray(length)
        random.nextBytes(salt)
        return Base64.encode(salt) // Base64编码便于存储
    }
}