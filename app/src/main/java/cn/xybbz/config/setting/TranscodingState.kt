package cn.xybbz.config.setting

sealed class TranscodingState {

    data object Transcoding : TranscodingState()

    data object NetWorkChange : TranscodingState()
}