package cn.xybbz.config.module

import android.content.Context
import cn.xybbz.config.alarm.AlarmConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AlarmModule {

    @Singleton
    @Provides
    fun alarmConfig(@ApplicationContext context: Context): AlarmConfig {
        val alarmConfig = AlarmConfig(context)
        alarmConfig.createGetUpAlarmManager(context, 0)
        return alarmConfig;
    }
}