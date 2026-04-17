@file:Suppress("UNCHECKED_CAST", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package cn.xybbz.config.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.node.HitTestResult
import androidx.compose.ui.node.LayoutNode
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.RootNodeOwner
import androidx.compose.ui.scene.ComposeSceneContext
import androidx.compose.ui.scene.CopiedList
import androidx.compose.ui.scene.LocalComposeSceneContext
import androidx.compose.ui.util.fastForEachReversed
import androidx.compose.ui.util.packFloats

/**
 * 标题栏命中检测能力。
 * 用于区分标题栏空白区与 Compose 交互控件，避免误触发窗口拖拽。
 */
interface DesktopWindowTitleBarHitTestOwner {
    fun hitTest(x: Float, y: Float): Boolean

    companion object {
        val None = object : DesktopWindowTitleBarHitTestOwner {
            override fun hitTest(x: Float, y: Float): Boolean = false
        }
    }
}

@OptIn(InternalComposeUiApi::class)
@Composable
fun rememberDesktopWindowTitleBarHitTestOwner(): DesktopWindowTitleBarHitTestOwner {
    val sceneContext = LocalComposeSceneContext.current
    return remember(sceneContext) {
        runCatching {
            when (sceneContext?.javaClass?.name) {
                null -> DesktopWindowTitleBarHitTestOwner.None
                "androidx.compose.ui.scene.CanvasLayersComposeSceneImpl" ->
                    CanvasLayersDesktopWindowTitleBarHitTestOwner(sceneContext)

                "androidx.compose.ui.scene.PlatformLayersComposeSceneImpl" ->
                    PlatformLayersDesktopWindowTitleBarHitTestOwner(sceneContext)

                else -> DesktopWindowTitleBarHitTestOwner.None
            }
        }.getOrElse {
            DesktopWindowTitleBarHitTestOwner.None
        }
    }
}

@OptIn(InternalComposeUiApi::class)
private abstract class ReflectDesktopWindowTitleBarHitTestOwner : DesktopWindowTitleBarHitTestOwner {
    protected val classLoader = ComposeSceneContext::class.java.classLoader!!

    protected fun LayoutNode.layoutNodeHitTest(x: Float, y: Float): Boolean {
        return try {
            val result = HitTestResult()
            hitTest(pointerPosition = Offset(x, y), hitTestResult = result)
            result.lastOrNull() is PointerInputModifierNode
        } catch (_: Exception) {
            false
        }
    }
}

@OptIn(InternalComposeUiApi::class)
private class PlatformLayersDesktopWindowTitleBarHitTestOwner(
    sceneContext: ComposeSceneContext,
) : ReflectDesktopWindowTitleBarHitTestOwner() {
    private val mainOwner = runCatching {
        val sceneClass = classLoader.loadClass("androidx.compose.ui.scene.PlatformLayersComposeSceneImpl")
        sceneClass.getDeclaredMethod("getMainOwner").let {
            it.trySetAccessible()
            it.invoke(sceneContext) as RootNodeOwner
        }
    }.getOrNull()

    override fun hitTest(x: Float, y: Float): Boolean = mainOwner?.owner?.root?.layoutNodeHitTest(x, y) ?: false
}

@OptIn(InternalComposeUiApi::class)
private class CanvasLayersDesktopWindowTitleBarHitTestOwner(
    private val sceneContext: ComposeSceneContext,
) : ReflectDesktopWindowTitleBarHitTestOwner() {
    private val sceneClass = runCatching {
        classLoader.loadClass("androidx.compose.ui.scene.CanvasLayersComposeSceneImpl")
    }.getOrNull()

    private val layerClass = sceneClass
        ?.declaredClasses
        ?.firstOrNull { it.name.endsWith("\$AttachedComposeSceneLayer") }

    private val mainOwner = runCatching {
        sceneClass
            ?.getDeclaredField("mainOwner")
            ?.apply { trySetAccessible() }
            ?.get(sceneContext) as RootNodeOwner
    }.getOrNull()

    private val layers = runCatching {
        sceneClass
            ?.getDeclaredField("_layersCopyCache")
            ?.apply { trySetAccessible() }
            ?.get(sceneContext) as CopiedList<*>
    }.getOrNull()

    private val focusedLayerField = runCatching {
        sceneClass
            ?.getDeclaredField("focusedLayer")
            ?.apply { trySetAccessible() }
    }.getOrNull()

    private val layerOwnerField = runCatching {
        layerClass
            ?.getDeclaredField("owner")
            ?.apply { trySetAccessible() }
    }.getOrNull()

    private val layerIsInBoundsMethod = runCatching {
        layerClass
            ?.declaredMethods
            ?.firstOrNull { it.name.startsWith("isInBounds") }
            ?.apply { trySetAccessible() }
    }.getOrNull()

    override fun hitTest(x: Float, y: Float): Boolean {
        val mainRoot = mainOwner?.owner?.root ?: return false
        val currentLayers = layers
        val currentFocusedLayerField = focusedLayerField
        val currentLayerOwnerField = layerOwnerField
        val currentLayerIsInBoundsMethod = layerIsInBoundsMethod

        if (currentLayers == null || currentFocusedLayerField == null || currentLayerOwnerField == null || currentLayerIsInBoundsMethod == null) {
            return mainRoot.layoutNodeHitTest(x, y)
        }

        layers.withCopy { layerList ->
            layerList.fastForEachReversed { layer ->
                if (currentLayerIsInBoundsMethod.invoke(layer, packFloats(x, y)) == true) {
                    return ((currentLayerOwnerField.get(layer) as? RootNodeOwner)?.owner?.root ?: mainRoot)
                        .layoutNodeHitTest(x, y)
                } else if (layer == currentFocusedLayerField.get(sceneContext)) {
                    return false
                }
            }
        }
        return mainRoot.layoutNodeHitTest(x, y)
    }
}
