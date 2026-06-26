package cn.xybbz.platform

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * JVM 平台上下文路径测试。
 */
class ContextWrapperJvmTest {
    /**
     * 未配置覆盖目录时，JVM 数据根目录应位于运行目录所在盘符根目录。
     */
    @Test
    fun defaultDataDirectoryUsesRuntimeDriveRoot() {
        val root = createTempDirectory()
        val contextWrapper = ContextWrapper.createForTest(
            properties = mapOf(ContextWrapper.PACKAGE_NAME_PROPERTY to "XyMusic"),
            installationDirectory = File(root, "install/bin"),
        )

        try {
            val expectedRoot = File(root.toPath().root.toFile(), "XyMusicData").absoluteFile

            assertEquals("XyMusic", contextWrapper.appName)
            assertEquals("xymusic", contextWrapper.packageName)
            assertEquals(expectedRoot, contextWrapper.dataDirectory)
            assertEquals(File(expectedRoot, "database").absoluteFile, contextWrapper.databaseDirectory)
            assertEquals(File(expectedRoot, "cache").absoluteFile, contextWrapper.cacheDirectory)
            assertEquals(File(expectedRoot, "temp").absoluteFile, contextWrapper.downloadTempParentDirectory)
            assertEquals(File(expectedRoot, "Downloads/XyMusic").absoluteFile, contextWrapper.downloadDirectory)
            assertEquals(contextWrapper.dataDirectory, contextWrapper.applicationDirectory)
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 配置数据根覆盖属性时，JVM 所有目录都应从覆盖目录派生。
     */
    @Test
    fun dataRootPropertyOverridesRuntimeDriveRoot() {
        val root = createTempDirectory()
        val dataRoot = File(root, "CustomDataRoot")
        val contextWrapper = ContextWrapper.createForTest(
            properties = mapOf(
                ContextWrapper.PACKAGE_NAME_PROPERTY to "XyMusic",
                ContextWrapper.DATA_ROOT_PROPERTY to dataRoot.absolutePath,
            ),
            installationDirectory = File(root, "install/bin"),
        )

        try {
            assertEquals(dataRoot.absoluteFile, contextWrapper.applicationDirectory)
            assertEquals(File(dataRoot, "database").absoluteFile, contextWrapper.databaseDirectory)
            assertEquals(File(dataRoot, "cache").absoluteFile, contextWrapper.cacheDirectory)
            assertEquals(File(dataRoot, "temp").absoluteFile, contextWrapper.downloadTempParentDirectory)
            assertEquals(File(dataRoot, "Downloads/XyMusic").absoluteFile, contextWrapper.downloadDirectory)
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 空白包名配置应回退默认应用名称，并生成小写 packageName。
     */
    @Test
    fun blankPackageNameFallsBackToDefaultAppName() {
        val root = createTempDirectory()
        val contextWrapper = ContextWrapper.createForTest(
            properties = mapOf(ContextWrapper.PACKAGE_NAME_PROPERTY to " "),
            installationDirectory = root,
        )

        try {
            assertEquals(ContextWrapper.DEFAULT_APP_NAME, contextWrapper.appName)
            assertEquals("xymusic", contextWrapper.packageName)
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 创建 JVM 路径测试临时目录。
     */
    private fun createTempDirectory(): File {
        return Files.createTempDirectory("xy-platform-context-test").toFile()
    }
}
