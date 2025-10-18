package cn.xybbz.entity.dto

/**
 * 通过网络获取歌词信息
 * @author 刘梦龙
 * @date 2024/04/18
 * @constructor 创建[NetworkLrcDto]
 * @param [ifOk] 是否返回数据
 * @param [lrcTxt] 歌词数据
 */
data class NetworkLrcDto(val ifOk: Boolean = false, val lrcTxt: String? = "")