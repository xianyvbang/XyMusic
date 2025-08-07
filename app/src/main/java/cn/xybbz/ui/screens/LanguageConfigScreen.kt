package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.ui.components.LazyColumnNotComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.viewmodel.LanguageConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageConfigScreen(
    modifier: Modifier = Modifier,
    languageConfigViewModel: LanguageConfigViewModel = hiltViewModel()
) {

    val navHostController = LocalNavController.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        languageConfigViewModel.getApplicationLanguageData(context)
    }

    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = Color(0xFF506464),
            bottomVerticalColor = Color(0xffae8b9c)
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "语言",
                    fontWeight = FontWeight.W900
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        navHostController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回设置"
                    )
                }
            }
        )

        LazyColumnNotComponent {
            item {
                RoundedSurfaceColumnPadding(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF5A524C), Color(0xFF726B66)),
                        tileMode = TileMode.Repeated
                    )
                ) {
                    LanguageTextItem(
                        text = "应用语言",
                        sub = languageConfigViewModel.applicationLanguage,
                        onClick = {})
                }
            }
            item {
                XyItemTextPadding(
                    text = "已选择语言",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                RoundedSurfaceColumnPadding(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF5A524C), Color(0xFF726B66)),
                        tileMode = TileMode.Repeated
                    )
                ) {
                    LanguageTextItem(
                        text = languageConfigViewModel.settingsConfig.languageType.languageName,
                        sub = languageConfigViewModel.settingsConfig.languageType.languageCode,
                        onClick = {

                        })
                }
            }
            item {
                XyItemTextPadding(
                    text = "可选择语言",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                RoundedSurfaceColumnPadding(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF5A524C), Color(0xFF726B66)),
                        tileMode = TileMode.Repeated
                    )
                ) {
                    LanguageType.entries.filter { it != languageConfigViewModel.settingsConfig.languageType }
                        .forEach {
                            LanguageTextItem(
                                text = it.languageName,
                                sub = it.languageCode,
                                onClick = {
                                    languageConfigViewModel.updateLanguageType(
                                        it,
                                        context = context
                                    )
                                })
                        }

                }
            }
        }
    }

}

@Composable
private fun LanguageTextItem(
    modifier: Modifier = Modifier,
    text: String,
    sub: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .debounceClickable() {
                onClick.invoke()
            }
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = text,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = sub,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall
        )
    }

}