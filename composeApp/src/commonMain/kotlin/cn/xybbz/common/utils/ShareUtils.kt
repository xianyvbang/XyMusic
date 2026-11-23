package cn.xybbz.common.utils

import cn.xybbz.platform.ContextWrapper

/**
 * 分享音乐资源。
 *
 * 对于网络链接，平台实现通常会按文本/链接分享；
 * 对于本地路径，平台实现会尽量按本地文件分享或采用对应平台的降级方案。
 */
expect fun shareMusicResource(
    contextWrapper: ContextWrapper,
    resource: String?
)

/**
 * 当前平台是否展示「分享歌曲」入口。
 * 桌面端暂无合适的系统分享能力，默认隐藏该入口。
 */
internal expect val platformSupportsSongShare: Boolean
