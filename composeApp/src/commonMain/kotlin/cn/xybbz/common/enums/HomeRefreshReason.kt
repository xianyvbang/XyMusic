package cn.xybbz.common.enums

/**
 * home页面刷新数据的原因
 */
enum class HomeRefreshReason {
    /**
     * 登陆成功
     */
    Login,

    /**
     * 打开home页面
     */
    EnterHome,

    /**
     * 手动刷新
     */
    Manual
}