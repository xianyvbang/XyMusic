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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.viewmodel.LanguageConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageConfigScreen(
    languageConfigViewModel: LanguageConfigViewModel = hiltViewModel<LanguageConfigViewModel>()
) {

    val navigator = LocalNavigator.current
    val context = LocalContext.current

    val languageType by remember {
        derivedStateOf {
            languageConfigViewModel.settingsConfig.languageType
        }
    }

    key(languageType) {
        XyColumnScreen(
            modifier = Modifier.brashColor(
                topVerticalColor = languageConfigViewModel.backgroundConfig.languageBrash[0],
                bottomVerticalColor = languageConfigViewModel.backgroundConfig.languageBrash[1]
            )
        ) {
            TopAppBarComponent(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    TopAppBarTitle(
                        title = stringResource(R.string.language)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigator.goBack()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.return_setting_screen)
                        )
                    }
                }
            )

            LazyColumnNotComponent {
                item {
                    XyItemTextPadding(
                        text = stringResource(R.string.selected_language),
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
                            languageType = languageConfigViewModel.settingsConfig.languageType,
                            enabled = false,
                            onClick = {})
                    }
                }
                item {
                    XyItemTextPadding(
                        text = stringResource(R.string.available_languages),
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
                                    languageType = it,
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


}

@Composable
private fun LanguageTextItem(
    modifier: Modifier = Modifier,
    languageType: LanguageType?,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .debounceClickable(enabled = enabled) {
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
            text = languageType?.languageName ?: "",
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = languageType?.languageCode ?: "",
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall
        )
    }

}