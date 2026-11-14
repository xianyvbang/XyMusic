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

package cn.xybbz.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.ui.components.JvmSettingBaseRow
import cn.xybbz.ui.components.JvmSettingGroup
import cn.xybbz.ui.components.JvmSettingGroupGrid
import cn.xybbz.ui.components.JvmSettingItemFlow
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.AboutViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.add_link_24px
import xymusic.composeapp.generated.resources.app_icon_info
import xymusic.composeapp.generated.resources.app_name
import xymusic.composeapp.generated.resources.check_24px
import xymusic.composeapp.generated.resources.download_24px
import xymusic.composeapp.generated.resources.function_not_implemented
import xymusic.composeapp.generated.resources.info_24px
import xymusic.composeapp.generated.resources.jvm_about_screen_text_09
import xymusic.composeapp.generated.resources.jvm_about_screen_text_10
import xymusic.composeapp.generated.resources.jvm_about_screen_text_15
import xymusic.composeapp.generated.resources.jvm_about_screen_text_16
import xymusic.composeapp.generated.resources.jvm_about_screen_text_17
import xymusic.composeapp.generated.resources.jvm_about_screen_text_23
import xymusic.composeapp.generated.resources.jvm_about_screen_text_24
import xymusic.composeapp.generated.resources.jvm_about_screen_text_25
import xymusic.composeapp.generated.resources.jvm_about_screen_text_31
import xymusic.composeapp.generated.resources.jvm_about_screen_text_32
import xymusic.composeapp.generated.resources.jvm_about_screen_text_36
import xymusic.composeapp.generated.resources.jvm_about_screen_text_37
import xymusic.composeapp.generated.resources.jvm_about_screen_text_38
import xymusic.composeapp.generated.resources.jvm_about_screen_text_40
import xymusic.composeapp.generated.resources.jvm_about_screen_text_41
import xymusic.composeapp.generated.resources.logo_new

/** 关于分区品牌行 Logo 容器尺寸，对齐设计稿的 64 尺寸渐变方块。 */
private val JvmAboutLogoSize = 64.dp

/** 关于分区品牌行 Logo 图片尺寸。 */
private val JvmAboutLogoImageSize = 50.dp

/** 读数项的固定宽度，读数条内的读数项等宽排列。 */
private val JvmAboutReadoutItemWidth = 176.dp

/** 读数标签字号。 */
private val JvmAboutReadoutLabelSize = 9.5.sp

/** 读数数值字号。 */
private val JvmAboutReadoutValueSize = 15.sp

/** 关于分区链接行的开源仓库地址。 */
private const val JvmAboutRepositoryUrl = "https://github.com/xianyvbang/XyMusic"

/**
 * 设置页内嵌的关于分区。
 *
 * 布局参考设计稿的关于页：顶部是品牌行（渐变 Logo、应用名与版本读数），
 * 下方是一条等宽读数条，再往下是按设置页分组样式排布的应用信息、更新、链接与技术栈。
 * 版本信息在分区进入组合时刷新一次，避免展示上一次进入设置页时的旧数据。
 *
 * @param aboutViewModel 关于页 ViewModel，提供平台版本信息。
 * @param modifier 外层布局修饰符，通常由设置页分组网格下发宽度。
 */
@Composable
internal fun JvmAboutSettingsSection(
    aboutViewModel: AboutViewModel,
    modifier: Modifier = Modifier,
) {
    // 进入分区后刷新一次平台信息，避免品牌行的版本读数沿用旧值。
    LaunchedEffect(aboutViewModel) {
        aboutViewModel.getPlatformInfo()
    }

    val uriHandler = LocalUriHandler.current
    // PlatformInfo 尚未返回时保留 JVM 占位，保证品牌行不会出现空白版本。
    val versionInfo = aboutViewModel.versionInfo.ifBlank { "JVM" }
    val javaVersion = "JVM ${System.getProperty("java.version").orEmpty()}"
    val repositoryLabel = JvmAboutRepositoryUrl.substringAfter("https://")
    val functionNotImplemented = stringResource(Res.string.function_not_implemented)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding * 2),
    ) {
        JvmAboutHero(
            appName = stringResource(Res.string.app_name),
            versionInfo = versionInfo,
        )

        // 读数条：版本、运行时与界面框架，使用等宽小字对齐设计稿。
        JvmAboutReadouts(
            items = listOf(
                stringResource(Res.string.jvm_about_screen_text_09) to versionInfo,
                stringResource(Res.string.jvm_about_screen_text_10) to javaVersion,
                stringResource(Res.string.jvm_about_screen_text_32) to "Compose Multiplatform",
            )
        )

        JvmSettingGroupGrid {
            // 更新与链接：版本更新操作与开源仓库、问题反馈入口合并为一个分组。
            JvmSettingGroup(
                modifier = Modifier.width(it),
                title = stringResource(Res.string.jvm_about_screen_text_41),
            ) {
                JvmAboutLinkRow(
                    icon = Res.drawable.check_24px,
                    title = stringResource(Res.string.jvm_about_screen_text_40),
                    description = stringResource(Res.string.jvm_about_screen_text_23),
                    value = stringResource(Res.string.jvm_about_screen_text_38),
                    onClick = { MessageUtils.sendPopTip(functionNotImplemented) },
                )
                JvmAboutLinkRow(
                    icon = Res.drawable.download_24px,
                    title = stringResource(Res.string.jvm_about_screen_text_24),
                    description = stringResource(Res.string.jvm_about_screen_text_25),
                    value = stringResource(Res.string.jvm_about_screen_text_38),
                    onClick = { MessageUtils.sendPopTip(functionNotImplemented) },
                )
                JvmAboutLinkRow(
                    icon = Res.drawable.add_link_24px,
                    title = stringResource(Res.string.jvm_about_screen_text_36),
                    description = repositoryLabel,
                    value = stringResource(Res.string.jvm_about_screen_text_15),
                    onClick = { uriHandler.openUri(JvmAboutRepositoryUrl) },
                )
                JvmAboutLinkRow(
                    icon = Res.drawable.info_24px,
                    title = stringResource(Res.string.jvm_about_screen_text_16),
                    description = stringResource(Res.string.jvm_about_screen_text_17),
                    value = stringResource(Res.string.jvm_about_screen_text_17),
                    onClick = { MessageUtils.sendPopTip(functionNotImplemented) },
                )
            }
        }
    }
}

/**
 * 关于分区品牌行：渐变 Logo 与应用名、版本读数。
 *
 * 对齐设计稿：左侧圆角渐变 Logo，右侧应用名、说明与等宽版本读数，不加卡片底色。
 *
 * @param appName 应用名称。
 * @param versionInfo 当前版本信息。
 */
@Composable
private fun JvmAboutHero(
    appName: String,
    versionInfo: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding + XyTheme.dimens.outerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(JvmAboutLogoSize)
                .clip(RoundedCornerShape(XyTheme.dimens.dialogCorner))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                modifier = Modifier.size(JvmAboutLogoImageSize),
                painter = painterResource(Res.drawable.logo_new),
                contentScale = ContentScale.Fit,
                contentDescription = stringResource(Res.string.app_icon_info),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)) {
            XyText(
                text = appName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 26.sp,
                    letterSpacing = (-0.6).sp,
                ),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            XyTextSub(
                text = stringResource(Res.string.jvm_about_screen_text_31),
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            XyTextSub(
                text = "${stringResource(Res.string.jvm_about_screen_text_37)} $versionInfo",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 关于分区读数条。
 *
 * 参考设计稿的读数排布：每个读数项左侧是等宽小字标签，下方是加粗数值，
 * 读数项之间用一条竖向细线分隔；宽度不足时由换行容器自动换到下一行。
 *
 * @param items 读数标签与数值对。
 */
@Composable
private fun JvmAboutReadouts(items: List<Pair<String, String>>) {
    JvmSettingItemFlow {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .width(JvmAboutReadoutItemWidth)
                    // 让分隔线高度跟随读数项内容高度，避免写死高度。
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (index > 0) {
                    // 读数项之间的竖向分隔线，对齐设计稿读数区的细线。
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    )
                }
                Column(
                    modifier = Modifier.padding(start = XyTheme.dimens.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    XyTextSub(
                        text = item.first,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = JvmAboutReadoutLabelSize,
                            letterSpacing = 1.6.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    XyText(
                        text = item.second,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = JvmAboutReadoutValueSize,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * 关于分区可点击链接行。
 *
 * @param icon 行首图标。
 * @param title 行标题。
 * @param description 行说明。
 * @param value 行尾值标签。
 * @param onClick 点击事件。
 */
@Composable
private fun JvmAboutLinkRow(
    icon: DrawableResource,
    title: String,
    description: String,
    value: String,
    onClick: () -> Unit,
) {
    JvmSettingBaseRow(
        icon = icon,
        title = title,
        description = description,
        iconSelected = true,
        onClick = onClick,
        trailing = {
            JvmAboutValuePill(value = value)
        }
    )
}

/**
 * 关于分区行尾值标签。
 *
 * @param value 标签中展示的短文本。
 */
@Composable
private fun JvmAboutValuePill(value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        XyText(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
