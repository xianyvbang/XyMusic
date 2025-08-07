package cn.xybbz.common.utils

import cn.xybbz.entity.data.EncryptAesData
import cn.xybbz.entity.data.Md5EncryptData
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


object PasswordUtils {


    /**
     * 假定设置密码时，密码规则为：  字母、数字、特殊符号，至少匹配2种
     * 则密码可能出现的情况有：
     * 1、数字+特殊符号
     * 2、字母+特殊符号
     * 3、字母+数字
     * 4、字母+数字+特殊符号
     * (组合与顺序无关)
     * 解决思路：
     * 1、遍历字符串的字符数组，查看是否包含目标特殊字符，若包含，则标记字符串
     * 包含特殊字符，并替换当前特殊字符为''。
     * 2、判断剩下的字符组成的字符串，是否匹配以下情况
     * - 纯字母
     * - 纯数字
     * - 字母+数字
     * 3、字符串匹配规则
     * 纯字母+包含特殊字符  ---- 匹配通过
     * 纯数字+包含特殊字符 ---- 匹配通过
     * 字母+数字+包含个数字符 ---- 匹配通过
     */
    //特殊字符
    private const val specCharacters = " !\"#$%&'()*+,-./:;<=>?@\\]\\[^_`{|}~"

    // 纯字母
    private const val character = "[a-zA-Z]+$"

    // 纯数字
    private const val number = "[0-9]+$"

    // 字母和数字
    private const val numberAndCharacter = "((^[a-zA-Z]+[0-9]+[a-zA-Z0-9]*)+)" +
            "|((^[0-9]+[a-zA-Z]+[a-zA-Z0-9]*)+)$"

    fun checkPassword(targetString: String): Boolean {
        var opStr = targetString
        val isLegal: Boolean
        var hasSpecChar = false
        val charArray = opStr.toCharArray()
        for (c in charArray) {
            if (specCharacters.contains(c.toString())) {
                hasSpecChar = true
                // 替换此字符串
                opStr = opStr.replace(c, ' ')
            }
        }
        val excSpecCharStr = opStr.replace(" ", "")
        val isPureNum: Boolean = Pattern.compile(number).matcher(excSpecCharStr).matches()
        val isPureChar: Boolean = Pattern.compile(character).matcher(excSpecCharStr).matches()
        val isNumAndChar: Boolean =
            Pattern.compile(numberAndCharacter).matcher(excSpecCharStr).matches()
        isLegal =
            isPureNum && hasSpecChar || isPureChar && hasSpecChar || isNumAndChar && hasSpecChar || isNumAndChar
        println("字符串：$targetString,是否符合规则：$isLegal")
        println("---------------")
        return isLegal
    }

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