package cn.xybbz.entity.data


/**
 * 加密 AES 数据
 * @author Administrator
 * @date 2024/08/21
 * @constructor 创建[EncryptAesData]
 * @param [encryptedSalt] md5加密盐
 * @param [passwordMd5] 加密后的密码
 */
data class Md5EncryptData(val encryptedSalt:String,val passwordMd5:String)
