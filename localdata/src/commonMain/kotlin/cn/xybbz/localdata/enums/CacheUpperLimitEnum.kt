package cn.xybbz.localdata.enums

/**
 * 缓存上限枚举
 * @author 刘梦龙
 * @date 2023/12/13
 * @constructor 创建[CacheUpperLimitEnum]
 * @param [code] 编码
 */
enum class CacheUpperLimitEnum(val code: Int) {

    Auto(0),
    No(1),
    OneHundred(2),
    FiveHundred(3),
    EightHundred(4),
    OneG(5),
    ThreeG(6),
    FourG(7),
    EightG(8),
    SixteenG(9),
    ThirtyTwoG(10),
    SixtyFourG(11),
    OneHundredTwentyEightG(12);

}
