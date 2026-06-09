package cn.xybbz.common.utils

import platform.UIKit.UIPasteboard

actual fun copyTextToClipboard(text: String) {
    UIPasteboard.generalPasteboard.string = text
}
