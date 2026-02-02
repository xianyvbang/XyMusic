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

package cn.xybbz.config.recommender

import android.util.Log
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.localdata.data.music.XyMusic
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.max
import kotlin.random.Random

private val DAY_MS = TimeUnit.DAYS.toMillis(1)

data class RecommenderConfig(
    val recentPlayExcludeDays: Long = 3,
    val recommendCount: Int = 50,
    val explorationRate: Double = 0.15, // 探索比例（0..1）
    val maxCandidates: Int = 400 // 为了性能，候选集上限
)

/**
 * DailyRecommender: 每次调用 generate() 都会重新生成推荐（基于现有数据）；
 * 但生成过程会先缩小候选集以避免对整个库遍历。
 */
class DailyRecommender(
    private val repo: DataSourceManager,
    private val recentHistory: RecentHistoryCache,
    private val config: RecommenderConfig = RecommenderConfig()
) {


    suspend fun generate() {
        // 1) 拉取必要数据（最近播放用于画像）
        val recentPlays =
            repo.getPlayRecordMusicList(pageSize = (Constants.ALBUM_MUSIC_LIST_PAGE * 0.8).toInt())
        // 2) 构建用户画像（artist/genre 权重）
        val (artistPrefs: Map<String, Double>, genrePrefs) = buildUserProfile(recentPlays)

        // 3) 构建候选集（根据 artist/genre top + 部分随机采样）
        val candidatesStartTime = System.currentTimeMillis()
        val candidates = buildCandidateSet(artistPrefs, genrePrefs)
        val candidatesEndTime = System.currentTimeMillis()
        Log.i(
            "DailyRecommender",
            "Building candidate set took ${candidatesEndTime - candidatesStartTime} ms"
        )

        // 4) 从候选集评分并排序（混合得分）
        val scored = candidates.map { song ->
            val prefScore =
                (song.artistIds?.sumOf { artistId ->
                    val artistPref = artistPrefs[artistId]
                    (artistPref ?: 0.0) * 0.7
                } ?: 0.0) * 0.7 + (song.genreIds
                    ?.sumOf { genreId ->
                        genrePrefs[genreId] ?: 0.0
                    } ?: 0.0) * 0.3
            val contentSim = estimateContentSimilarity(song, candidates) // 简单标签相似度，轻量
            val randomFactor = 1.0 + Random.nextDouble(-0.08, 0.12) // 小扰动
            val finalScore = (0.65 * prefScore + 0.25 * contentSim + 0.10) * randomFactor
            song to finalScore
        }

        // 5) 排序，应用去重 & 探索策略
        val sorted = scored.sortedByDescending { it.second }.map { it.first }

        val scoringStartTime2 = System.currentTimeMillis()
        // filter out recently played (exclude recent N days) and recentHistory
        val now = System.currentTimeMillis()
        val filtered = sorted.filterNot { s ->
            val daysSince =
                if (s.lastPlayedDate <= 0L) config.recentPlayExcludeDays else (now - s.lastPlayedDate) / DAY_MS
            daysSince < config.recentPlayExcludeDays
        }.filterNot { s ->
            // contains is suspend
            // (we'll check snapshot for performance)
            false
        }

        val scoringStartTime4 = System.currentTimeMillis()
        // To avoid multiple suspend calls in filter, get snapshot once:
        val finalFiltered = recentHistory.contains(filtered)
        val scoringEndTime4 = System.currentTimeMillis()
        Log.i("DailyRecommender", "Scoring room filter ${scoringEndTime4 - scoringStartTime4} ms")

        val scoringStartTime5 = System.currentTimeMillis()
        // Exploration: reserve some slots for exploration (unseen / low-score)
        val n = config.recommendCount
        val exploreCount = max(1, (n * config.explorationRate).toInt())
        val exploitCount = n - exploreCount

        val exploit = finalFiltered.take(exploitCount)
        // exploration pool: items not in recent plays and low pref (or never played)

        val explorationPool = recentHistory.contains(candidates) { music ->
            recentPlays.none { it.itemId == music.itemId } // never in recent plays -> good exploration candidate
        }.shuffled().take(exploreCount)
        //大于5分钟的音乐都过滤
        val xyMusics = (exploit + explorationPool).distinctBy { it.itemId }
        val filter = xyMusics
            .filter { it.runTimeTicks.toFloat() / 1000 / 60.0 <= 6 }
        val result =
            filter
                .take(n)
        val scoringEndTime5 = System.currentTimeMillis()
        Log.i(
            "DailyRecommender",
            "Scoring room and play list filter ${scoringEndTime5 - scoringStartTime5} ms"
        )
        val scoringEndTime2 = System.currentTimeMillis()
        Log.i("DailyRecommender", "Scoring list filter ${scoringEndTime2 - scoringStartTime2} ms")
        Log.i("=====", "推荐音乐获取大小:${result.size} ")
        // 更新 recentHistory（异步安全）
        recentHistory.addAll(
            result,
            repo.getConnectionId()
        )
    }

    // 画像：计算 artist/genre 权重（简单加权：播放次数 & 最近播放 & liked）
    private fun buildUserProfile(recentPlays: List<XyMusic>): Pair<Map<String, Double>, Map<String, Double>> {
        val artistScore = mutableMapOf<String, Double>()
        val genreScore = mutableMapOf<String, Double>()
        val now = System.currentTimeMillis()

        // 权重设计：离得近的播放更重要；liked 给额外分
        recentPlays.forEach { s ->
            val recencyWeight =
                if (s.lastPlayedDate <= 0L) 0.0 else 1.0 / (1.0 + (now - s.lastPlayedDate).toDouble() / DAY_MS)
            val base = 1.0 + log10((s.playedCount + 1).toDouble()) // playcount 的次级增长
            val likedBoost = if (s.ifFavoriteStatus) 1.0 else 0.0
            val add = base * (0.6 * recencyWeight + 0.4) + likedBoost

            if (!s.artistIds.isNullOrEmpty())
                s.artistIds?.forEach {
                    artistScore[it] = (artistScore[it] ?: 0.0) + add
                }

            if (!s.genreIds.isNullOrEmpty()) {
                s.genreIds?.forEach {
                    genreScore[it] = (genreScore[it] ?: 0.0) + add * 0.8
                }
            }
        }

        // 归一化到 [0,1]
        normalizeMap(artistScore)
        normalizeMap(genreScore)
        return artistScore to genreScore
    }

    private fun normalizeMap(map: MutableMap<String, Double>) {
        val max = map.values.maxOrNull() ?: return
        if (max <= 0.0) return
        map.keys.toList().forEach { k ->
            map[k] = map[k]!! / max
        }
    }

    // 构建候选集：优先 artist/genre top，再补充随机采样以覆盖全库
    private suspend fun buildCandidateSet(
        artistPrefs: Map<String, Double>,
        genrePrefs: Map<String, Double>
    ): List<XyMusic> {
        // take top artist and top genres
        val topArtists =
            artistPrefs.entries.sortedByDescending { it.value }.take(10).map { it.key }.toSet()
        val topGenres =
            genrePrefs.entries.sortedByDescending { it.value }.take(6).map { it.key }.toSet()

        Log.i("DailyRecommender", "Top artists: $topArtists")
        val startTime = System.currentTimeMillis()
        val byArtists = repo.getMusicListByArtistIds(
            topArtists.toList(),
            pageSize = (Constants.ALBUM_MUSIC_LIST_PAGE * 0.8).toInt()
        ) ?: emptyList() // each artist 取部分
        val endTime = System.currentTimeMillis()
        Log.i("DailyRecommender", "Fetching music by artists took ${endTime - startTime} ms")

        Log.i("DailyRecommender", "Top genres: $topGenres")
        val startTime1 = System.currentTimeMillis()
        val byGenres = repo.selectMusicListByGenreIds(
            topGenres.toList(),
            pageSize = (Constants.ALBUM_MUSIC_LIST_PAGE * 0.4).toInt()
        ) ?: emptyList()
        val endTime1 = System.currentTimeMillis()
        Log.i("DailyRecommender", "Fetching music by genres took ${endTime1 - startTime1} ms")
        // 补充：如果候选少，随机从全库取一部分（分页）
        val seed = byArtists.union(byGenres).toMutableList()
        if (seed.isEmpty() || seed.size < config.maxCandidates) {
            val all =
                repo.getRandomMusicList(
                    pageNum = 0,
                    pageSize = config.maxCandidates - seed.size
                )
            all?.let { randomMusics ->
                seed.addAll(randomMusics)
            }
        }

        // 限制最大候选数以控制计算量
        return seed.distinct().take(config.maxCandidates)
    }

    // 估算内容相似度（轻量：基于 artist/genre/album 标签的占比）
    private fun estimateContentSimilarity(song: XyMusic, pool: List<XyMusic>): Double {
        if (pool.isEmpty()) return 0.0
        var score = 0.0
        pool.forEach { other ->
            if (other.itemId == song.itemId) return@forEach
            if (!other.artistIds.isNullOrEmpty() && other.artistIds
                    ?.any { song.artistIds?.contains(it) == true } == true
            ) score += 0.6
            else if (!song.genreIds.isNullOrEmpty() && song.genreIds
                    ?.any {
                        other.genreIds?.contains(
                            it
                        ) == true
                    } == true
            ) score += 0.3
            else if (song.album.isNotBlank() && song.album == other.album) score += 0.15
        }
        val maxPossible = (pool.size - 1) * 0.6
        return if (maxPossible <= 0) 0.0 else score / maxPossible
    }


}