package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@Composable
internal actual fun rememberBackgroundImagePicker(
    onImagePicked: (String?) -> Unit
): BackgroundImagePicker {
    val currentOnImagePicked by rememberUpdatedState(onImagePicked)

    return remember {
        object : BackgroundImagePicker {
            override fun pickImage() {
                val picker = UIDocumentPickerViewController(
                    documentTypes = listOf(
                        "public.image",
                        "public.png",
                        "public.jpeg"
                    ),
                    inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
                )
                val delegate = BackgroundImagePickerDelegate { path ->
                    currentOnImagePicked(path)
                }
                picker.delegate = delegate
                topViewController()?.presentViewController(
                    viewControllerToPresent = picker,
                    animated = true,
                    completion = null
                )
            }
        }
    }
}

private class BackgroundImagePickerDelegate(
    private val onPicked: (String?) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val fileUrl = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (fileUrl == null) {
            onPicked(null)
            return
        }

        val accessing = fileUrl.startAccessingSecurityScopedResource()
        try {
            onPicked(fileUrl.absoluteString)
        } finally {
            if (accessing) {
                fileUrl.stopAccessingSecurityScopedResource()
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
