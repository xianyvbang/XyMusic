/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.theme.XyTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.check_24px
import xymusic.composeapp.generated.resources.playlist_add_check_24px
import xymusic.composeapp.generated.resources.select
import xymusic.composeapp.generated.resources.selection_done

/**
 * 批量选择开关按钮的展示内容。
 *
 * @param text 按钮文案资源
 * @param icon 按钮图标资源
 */
internal data class SelectionToggleAppearance(
    val text: StringResource,
    val icon: DrawableResource,
)

/**
 * 根据当前选择状态计算开关按钮的文案与图标。
 *
 * 非选择态展示「选择」与添加图标，选择态切换为「完成」与勾选图标，
 * 抽成纯函数便于单元测试覆盖状态映射。
 *
 * @param isSelecting 当前是否处于选择模式
 */
internal fun selectionToggleAppearance(isSelecting: Boolean): SelectionToggleAppearance {
    return if (isSelecting) {
        SelectionToggleAppearance(
            text = Res.string.selection_done,
            icon = Res.drawable.check_24px,
        )
    } else {
        SelectionToggleAppearance(
            text = Res.string.select,
            icon = Res.drawable.playlist_add_check_24px,
        )
    }
}

/**
 * 批量选择模式开关按钮。
 *
 * 各音乐列表页面统一使用该按钮进入或退出选择模式：
 * 非选择态展示「选择」，选择态切换为带勾选图标的「完成」，
 * 避免各页面重复实现同一套图标、文案与尺寸。
 *
 * 该按钮通常与 [JvmMusicSelectionActionBar] 配合使用：
 * 点击「选择」后，由 [JvmMusicSelectionPanel] 在列表上方展开
 * [JvmMusicSelectionActionBar] 承载全选与批量操作；点击「完成」时收起操作栏并清空选中。
 * 因此选择入口与批量操作栏需要绑定同一个 [cn.xybbz.config.select.SelectControl] 状态源，
 * 保证按钮文案（选择/完成）与操作栏的显示状态始终一致。
 *
 * @param isSelecting 当前是否处于选择模式
 * @param onClick 点击回调，由调用方根据当前状态执行进入或退出选择模式
 * @param modifier 外层修饰符
 */
@Composable
fun JvmSelectionToggleButton(
    isSelecting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 文案与图标随选择状态切换，二者保持同一语义
    val appearance = selectionToggleAppearance(isSelecting)
    val actionText = stringResource(appearance.text)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(32.dp)
            // 桌面端悬停显示手型指针，明确按钮可点击
            .pointerHoverIcon(PointerIcon.Hand),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        contentPadding = PaddingValues(horizontal = 10.dp),
    ) {
        Icon(
            // 选择态切换为完成图标，明确当前点击会退出选择模式
            painter = painterResource(appearance.icon),
            contentDescription = actionText,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = actionText)
    }
}
