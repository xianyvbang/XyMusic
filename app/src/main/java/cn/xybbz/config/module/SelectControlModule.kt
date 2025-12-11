package cn.xybbz.config.module

import android.content.Context
import cn.xybbz.config.select.SelectControl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SelectControlModule {

    @Singleton
    @Provides
    fun selectListData(@ApplicationContext context: Context): SelectControl {
        return SelectControl(context)
    }
}