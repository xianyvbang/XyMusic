package cn.xybbz.config.module

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.recommender.DailyRecommender
import cn.xybbz.config.recommender.RecentHistoryCache
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RecommenderModule {

    @Singleton
    @Provides
    fun dailyRecommender(
        dataSourceManager: DataSourceManager,
        db: DatabaseClient
    ): DailyRecommender {
        val dailyRecommender = DailyRecommender(dataSourceManager, db, RecentHistoryCache(db))
        return dailyRecommender
    }
}