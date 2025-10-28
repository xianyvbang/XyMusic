package cn.xybbz.config.module

import cn.xybbz.api.client.IDataSourceManager
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
        dataSourceManager:IDataSourceManager,
        db: DatabaseClient
    ): DailyRecommender {
        val dailyRecommender = DailyRecommender(dataSourceManager, RecentHistoryCache(db.recentHistoryDao))
        return dailyRecommender
    }
}