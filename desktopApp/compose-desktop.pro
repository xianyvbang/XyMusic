# -------------------------- Sketch Privider ---------------------------- #
-keep class * implements com.github.panpf.sketch.util.DecoderProvider { *; }
-keep class * implements com.github.panpf.sketch.util.FetcherProvider { *; }

# ------------------------------ JNA ------------------------------------ #
-dontwarn com.sun.jna.internal.Cleaner
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }

# ---------------------------- FileKit JVM ------------------------------ #
-keep class io.github.vinceglb.filekit.dialogs.platform.windows.jna.** { *; }
-keep class org.freedesktop.dbus.** { *; }
-keep class org.freedesktop.portal.** { *; }
-dontwarn org.freedesktop.dbus.**
-dontwarn org.freedesktop.portal.**

# ---------------------- Desktop Hit Test Reflection -------------------- #
-keep class androidx.compose.foundation.HoverableNode { *; }
-keep class androidx.compose.foundation.gestures.ScrollableNode { *; }
-keep class androidx.compose.ui.scene.PlatformLayersComposeSceneImpl { *; }
-keep class androidx.compose.ui.scene.CanvasLayersComposeSceneImpl { *; }
-keep class androidx.compose.ui.scene.CanvasLayersComposeSceneImpl$AttachedComposeSceneLayer { *; }

-keepclassmembers class androidx.compose.ui.scene.PlatformLayersComposeSceneImpl {
    private *** getMainOwner();
}

-keepclassmembers class androidx.compose.ui.scene.CanvasLayersComposeSceneImpl {
    private *** mainOwner;
    private *** _layersCopyCache;
    private *** focusedLayer;
}

-keepclassmembers class androidx.compose.ui.scene.CanvasLayersComposeSceneImpl$AttachedComposeSceneLayer {
    private *** owner;
    private *** isInBounds(...);
}
