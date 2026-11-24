package cn.xybbz.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.check_24px
import xymusic.composeapp.generated.resources.playlist_add_check_24px
import xymusic.composeapp.generated.resources.select
import xymusic.composeapp.generated.resources.selection_done

/**
 * JVM 批量选择通用组件的状态映射测试。
 *
 * 覆盖选择开关按钮的文案/图标切换，以及选择操作栏在不同数据源能力、
 * 本地/歌单场景下批量操作项的可见性。
 */
class JvmSelectionComponentsTest {

    /**
     * 非选择态：按钮展示「选择」文案与添加图标。
     */
    @Test
    fun nonSelectingToggleShowsSelectTextAndAddIcon() {
        val appearance = selectionToggleAppearance(isSelecting = false)

        assertEquals(Res.string.select, appearance.text)
        assertEquals(Res.drawable.playlist_add_check_24px, appearance.icon)
    }

    /**
     * 选择态：按钮切换为「完成」文案与勾选图标。
     */
    @Test
    fun selectingToggleShowsDoneTextAndCheckIcon() {
        val appearance = selectionToggleAppearance(isSelecting = true)

        assertEquals(Res.string.selection_done, appearance.text)
        assertEquals(Res.drawable.check_24px, appearance.icon)
    }

    /**
     * 远程音乐且数据源支持删除、下载：展示删除与下载，非歌单场景不展示移除。
     */
    @Test
    fun remoteSelectionShowsDeleteAndDownload() {
        val items = resolveMusicSelectionActionItems(
            canDelete = true,
            canDownload = true,
            ifPlaylist = false,
            ifLocal = false,
        )

        assertTrue(items.showDelete)
        assertTrue(items.showDownload)
        assertFalse(items.showRemoveFromPlaylist)
    }

    /**
     * 本地歌曲选择：即使数据源支持下载，也不展示下载入口。
     */
    @Test
    fun localSelectionHidesDownload() {
        val items = resolveMusicSelectionActionItems(
            canDelete = true,
            canDownload = true,
            ifPlaylist = false,
            ifLocal = true,
        )

        assertTrue(items.showDelete)
        assertFalse(items.showDownload)
    }

    /**
     * 歌单场景：额外展示「从歌单移除」，并跟随数据源能力隐藏删除与下载。
     */
    @Test
    fun playlistSelectionShowsRemoveAction() {
        val items = resolveMusicSelectionActionItems(
            canDelete = false,
            canDownload = false,
            ifPlaylist = true,
            ifLocal = false,
        )

        assertTrue(items.showRemoveFromPlaylist)
        assertFalse(items.showDelete)
        assertFalse(items.showDownload)
    }
}
