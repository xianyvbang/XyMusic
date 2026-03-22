package cn.xybbz.entity.data

/**
 * 加密 AES 数据
 * @author Administrator
 * @date 2024/08/21
 * @constructor 创建[EncryptAesData]
 * @param [aesKey] AES 密钥
 * @param [aesIv] AES IV系列
 * @param [aesData] AES数据
 */
data class EncryptAesData(val aesKey: String, val aesIv: String, val aesData: String)
