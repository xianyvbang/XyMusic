package cn.xybbz.localdata.enums

/**
 * 播放器类型
 * @author 刘梦龙
 * @date 2024/01/22
 * @constructor 创建[PlayerTypeEnum]
 * @param [code] 密码
 * @param [message] 消息
 */
enum class PlayerTypeEnum(val code:Int, val message:String) {

    /**
     * 单曲循环
     */
    SINGLE_LOOP(0,"单曲循环"),
    /**
     * 顺序播放
     */
    SEQUENTIAL_PLAYBACK(1,"顺序播放"),
    /**
     * 随机播放
     */
    RANDOM_PLAY(2,"随机播放")
}