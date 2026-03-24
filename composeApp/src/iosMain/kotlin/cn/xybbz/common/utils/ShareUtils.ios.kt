package cn.xybbz.common.utils

import cn.xybbz.di.ContextWrapper
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

actual fun shareMusicResource(
    contextWrapper: ContextWrapper,
    resource: String?
) {
    val value = resource?.trim()?.takeIf { it.isNotEmpty() } ?: return
    val shareItem = if (value.startsWith("http://") || value.startsWith("https://")) {
        NSURL.URLWithString(value) ?: value
    } else {
        NSURL.fileURLWithPath(value)
    }

    val controller = UIActivityViewController(
        activityItems = listOf(shareItem),
        applicationActivities = null
    )

    topViewController()?.presentViewController(
        viewControllerToPresent = controller,
        animated = true,
        completion = null
    )
}

private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
