/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.common.music

import cn.xybbz.localdata.enums.PlayerTypeEnum

sealed class PlayerEvent {
    //设置删除播放历史进度
    data class RemovePlaybackProgress(val musicId: String) : PlayerEvent()
    //暂停
    data class Pause(val musicId: String, val playSessionId: String, val musicUrl: String) : PlayerEvent()
    //播放
    data class Play(val musicId: String, val playSessionId: String) : PlayerEvent()
    //进度跳转
    data class PositionSeekTo(val positionMs: Long, val musicId: String, val playSessionId: String) : PlayerEvent()
    //手动切换音频之前
    object BeforeChangeMusic : PlayerEvent()
    //音频切换
    data class ChangeMusic(val musicId: String) : PlayerEvent()
    //收藏/取消收藏
    data class Favorite(val musicId: String) : PlayerEvent()
    //加载下一页数据,参数是页码
    data class NextList(val page: Int) : PlayerEvent()
    //播放列表增加数据,传值是艺术家id
    data class AddMusicList(val artistId: String?,/*是否为还原播放列表*/val ifInitPlayerList: Boolean = false) : PlayerEvent()
    //设置播放模式变化监听方法
    data class PlayerTypeChange(val playerType: PlayerTypeEnum) : PlayerEvent()
}
