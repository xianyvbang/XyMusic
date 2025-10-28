package cn.xybbz.config.recommender

import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.localdata.data.music.XyMusic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.max
import kotlin.random.Random

private val DAY_MS = TimeUnit.DAYS.toMillis(1)

data class RecommenderConfig(
    val recentPlayExcludeDays: Long = 3,
    val recommendCount: Int = 30,
    val explorationRate: Double = 0.15, // 探索比例（0..1）
    val maxCandidates: Int = 2000 // 为了性能，候选集上限
)

/**
 * DailyRecommender: 每次调用 generate() 都会重新生成推荐（基于现有数据）；
 * 但生成过程会先缩小候选集以避免对整个库遍历。
 */
class DailyRecommender(
    private val repo: IDataSourceManager,
    private val recentHistory: RecentHistoryCache,
    private val config: RecommenderConfig = RecommenderConfig()
) {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    suspend fun generate(): List<XyMusic> = withContext(ioScope.coroutineContext) {
        // 1) 拉取必要数据（最近播放用于画像）
        val recentPlays = repo.getPlayRecordMusicList(pageSize = Constants.ALBUM_MUSIC_LIST_PAGE)

        // 2) 构建用户画像（artist/genre 权重）
        val (artistPrefs: Map<String, Double>, genrePrefs) = buildUserProfile(recentPlays)

        // 3) 构建候选集（根据 artist/genre top + 部分随机采样）
        val candidates = buildCandidateSet(artistPrefs, genrePrefs)

        // 4) 从候选集评分并排序（混合得分）
        val scored = candidates.map { song ->


            val prefScore =
                (song.artistIds?.map { artistId ->
                    val artistPref = artistPrefs[artistId.toString()]
                    (artistPref ?: 0.0) * 0.7
                }?.sum() ?: 0.0) * 0.7 + (song.genreIds?.map { genreId ->
                    genrePrefs[genreId.toString()] ?: 0.0
                }?.sum() ?: 0.0) * 0.3
            val contentSim = estimateContentSimilarity(song, candidates) // 简单标签相似度，轻量
            val randomFactor = 1.0 + Random.nextDouble(-0.08, 0.12) // 小扰动
            val finalScore = (0.65 * prefScore + 0.25 * contentSim + 0.10) * randomFactor
            song to finalScore
        }

        // 5) 排序，应用去重 & 探索策略
        val sorted = scored.sortedByDescending { it.second }.map { it.first }

        // filter out recently played (exclude recent N days) and recentHistory
        val now = System.currentTimeMillis()
        val filtered = sorted.filterNot { s ->
            val daysSince =
                if (s.lastPlayedDate <= 0L) Long.MAX_VALUE else (now - s.lastPlayedDate) / DAY_MS
            daysSince < config.recentPlayExcludeDays
        }.filterNot { s ->
            // contains is suspend
            // (we'll check snapshot for performance)
            false
        }

        // To avoid multiple suspend calls in filter, get snapshot once:
        val finalFiltered = filtered.filterNot { recentHistory.contains(it.itemId) }

        // Exploration: reserve some slots for exploration (unseen / low-score)
        val n = config.recommendCount
        val exploreCount = max(1, (n * config.explorationRate).toInt())
        val exploitCount = n - exploreCount

        val exploit = finalFiltered.take(exploitCount)
        // exploration pool: items not in recent plays and low pref (or never played)
        val explorationPool = candidates.filter { c ->
            !recentHistory.contains(c.itemId) &&
                    recentPlays.none { it.itemId == c.itemId } // never in recent plays -> good exploration candidate
        }.shuffled().take(exploreCount)

        val result = (exploit + explorationPool).distinct().take(n)

        // 更新 recentHistory（异步安全）
        recentHistory.addAll(result.map { it.itemId })

        return@withContext result
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

            s.artistIds?.forEach {
                artistScore[it.toString()] = (artistScore[it.toString()] ?: 0.0) + add
            }

            if (!s.genreIds.isNullOrEmpty()) {
                s.genreIds?.forEach {
                    genreScore[it.toString()] = (genreScore[it.toString()] ?: 0.0) + add * 0.8
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

        val byArtists = repo.getMusicListByArtistIds(
            topArtists.toList(),
            pageSize = Constants.ALBUM_MUSIC_LIST_PAGE / 2
        )?:emptyList() // each artist 取部分
        val byGenres = repo.selectMusicListByGenreIds(
            topGenres.toList(),
            pageSize = Constants.ALBUM_MUSIC_LIST_PAGE / 2
        )?:emptyList()

        // 补充：如果候选少，随机从全库取一部分（分页）
        val seed = byArtists.union(byGenres).toMutableList()
        if (seed.isEmpty() || seed.size < 300) {
            val all = repo.getRandomMusicList(pageNum = 0, pageSize = Constants.ALBUM_MUSIC_LIST_PAGE / 2)
            all?.let {randomMusics ->
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
            if (other.artistIds?.any { song.artistIds?.contains(it) == true } == true) score += 0.6
            else if (!song.genreIds.isNullOrBlank() && song.genreIds?.any {
                    other.genreIds?.contains(
                        it
                    ) == true
                } == true) score += 0.3
            else if (song.album.isNotBlank() && song.album == other.album) score += 0.15
        }
        val maxPossible = (pool.size - 1) * 0.6
        return if (maxPossible <= 0) 0.0 else score / maxPossible
    }


}