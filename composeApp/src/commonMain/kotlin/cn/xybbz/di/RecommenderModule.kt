package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.recommender.DailyRecommender
import cn.xybbz.config.recommender.RecentHistoryCache
import cn.xybbz.localdata.config.DatabaseClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class RecommenderModule {

    @Singleton
    fun dailyRecommender(
        dataSourceManager: DataSourceManager,
        db: DatabaseClient
    ): DailyRecommender {
        val dailyRecommender = DailyRecommender(dataSourceManager, RecentHistoryCache(db))
        return dailyRecommender
    }
}