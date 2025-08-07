package cn.xybbz.localdata.enums

/**
 * 缓存上限枚举
 * @author 刘梦龙
 * @date 2023/12/13
 * @constructor 创建[CacheUpperLimitEnum]
 * @param [code] 编码
 * @param [message] 消息
 * @param [value] 数据
 */
enum class CacheUpperLimitEnum(val code: Int, val message: String, val value: Int) {

    Auto(0, "自动", -1), //可用空间>100G是16g 100G>可用空间>=50G 8g 50g>可用空间>=10g 4g 可用空间<10G 2g
    No(1, "不缓存", 0),
    OneHundred(2, "100MB", 100),
    FiveHundred(3, "500MB", 500),
    EightHundred(4, "800MB", 800),
    OneG(5, "1GB", 1 * 1024),
    ThreeG(6, "2GB", 2 * 1024),
    FourG(7, "4GB", 4 * 1024),
    EightG(8, "8GB", 8 * 1024);

}