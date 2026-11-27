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
     * 未配置覆盖目录时，JVM 数据目录应使用平台用户目录。
     */
    @Test
    fun defaultDataDirectoryUsesPlatformUserDirectories() {
        val root = createTempDirectory()
        val appData = File(root, "AppData/Roaming")
        val localAppData = File(root, "AppData/Local")
        val userProfile = File(root, "UserProfile")
        val contextWrapper = ContextWrapper.createForTest(
            properties = mapOf(
                ContextWrapper.PACKAGE_NAME_PROPERTY to "XyMusic",
                "APPDATA" to appData.absolutePath,
                "LOCALAPPDATA" to localAppData.absolutePath,
                "USERPROFILE" to userProfile.absolutePath,
            ),
        )

        try {
            val expectedData = File(appData, "XyMusic").absoluteFile
            val expectedLocalData = File(localAppData, "XyMusic").absoluteFile
            val expectedCache = File(expectedLocalData, "cache").absoluteFile
            val expectedDownload = File(File(userProfile, "Music"), "XyMusic").absoluteFile

            assertEquals("XyMusic", contextWrapper.appName)
            assertEquals("xymusic", contextWrapper.packageName)
            assertEquals(expectedData, contextWrapper.dataDirectory)
            assertEquals(expectedLocalData, contextWrapper.localDataDirectory)
            assertEquals(File(expectedData, "database").absoluteFile, contextWrapper.databaseDirectory)
            assertEquals(expectedCache, contextWrapper.cacheDirectory)
            assertEquals(expectedDownload, contextWrapper.downloadDirectory)
            assertEquals(contextWrapper.dataDirectory, contextWrapper.applicationDirectory)
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 配置数据根覆盖属性时，JVM 所有目录都应从覆盖目录派生。
     */
    @Test
    fun dataRootPropertyOverridesPlatformUserDirectories() {
        val root = createTempDirectory()
        val dataRoot = File(root, "CustomDataRoot")
        val contextWrapper = ContextWrapper.createForTest(
            properties = mapOf(
                ContextWrapper.PACKAGE_NAME_PROPERTY to "XyMusic",
                ContextWrapper.DATA_ROOT_PROPERTY to dataRoot.absolutePath,
            ),
        )

        try {
            assertEquals(dataRoot.absoluteFile, contextWrapper.applicationDirectory)
            assertEquals(dataRoot.absoluteFile, contextWrapper.localDataDirectory)
            assertEquals(File(dataRoot, "database").absoluteFile, contextWrapper.databaseDirectory)
            assertEquals(File(dataRoot, "cache").absoluteFile, contextWrapper.cacheDirectory)
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
        val contextWrapper = ContextWrapper.createForTest(
            properties = mapOf(ContextWrapper.PACKAGE_NAME_PROPERTY to " "),
        )

        assertEquals(ContextWrapper.DEFAULT_APP_NAME, contextWrapper.appName)
        assertEquals("xymusic", contextWrapper.packageName)
    }

    /**
     * 创建 JVM 路径测试临时目录。
     */
    private fun createTempDirectory(): File {
        return Files.createTempDirectory("xy-platform-context-test").toFile()
    }
}
