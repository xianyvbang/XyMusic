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

# ------------------------ Optional Logging APIs ------------------------ #
# Logback/Netty 会探测这些可选日志后端和服务端扩展，桌面客户端未打包时可忽略。
-dontwarn jakarta.mail.**
-dontwarn jakarta.servlet.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.codehaus.commons.compiler.**
-dontwarn org.codehaus.janino.**
-keep class io.netty.util.internal.logging.** { *; }

# ---------------------- Optional Network Backends ---------------------- #
# Ktor/OkHttp/Apache/Netty 的压缩、TLS 和原生镜像集成为可选依赖，未启用时不参与桌面包。
-dontwarn com.aayushatharva.brotli4j.**
-dontwarn com.github.luben.zstd.**
-dontwarn com.jcraft.jzlib.**
-dontwarn com.ning.compress.**
-dontwarn io.netty.**
-dontwarn io.netty.internal.tcnative.**
-dontwarn io.netty.pkitesting.**
-dontwarn lzma.sdk.**
-dontwarn net.jpountz.**
-dontwarn org.bouncycastle.**
-dontwarn org.brotli.dec.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.tukaani.xz.**

# ----------------------- Optional Tooling Integrations ----------------- #
# GraalVM、OSGi 和 BlockHound 只在特定运行环境下存在，普通 JVM 桌面包无需携带。
-dontwarn com.oracle.svm.core.annotate.**
-dontwarn org.graalvm.nativeimage.hosted.**
-dontwarn org.osgi.annotation.bundle.**
-dontwarn reactor.blockhound.**

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
