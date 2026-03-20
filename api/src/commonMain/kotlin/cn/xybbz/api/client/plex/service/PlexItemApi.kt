package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.plex.data.ItemInfoResponse
import cn.xybbz.api.client.plex.data.PlexLibrary
import cn.xybbz.api.client.plex.data.PlexLibraryItemResponse
import cn.xybbz.api.client.plex.data.PlexResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters

class PlexItemApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun getSongs(
        sectionKey: String,
        selectType: String,
        type: Int? = 10,
        sort: String? = null,
        title: String? = null,
        includeCollections: Int = 1,
        includeMeta: Int = 1,
        includeExternalMedia: Int = 1,
        start: Int,
        pageSize: Int,
        artistId: String? = null,
        albumId: String? = null,
        //使用逗号分割
        genreIds: String? = null,
        albumCollection: Int? = null,
        trackCollection: Int? = null,
        artistCollection: Int? = null,
        decade: String? = null,
        albumDecade: String? = null,
        albumStartYear: Int? = null,
        albumEndYear: Int? = null,
        artistTitle: String? = null,
        params: Map<String, String>? = null
    ): PlexResponse<PlexLibraryItemResponse> {
        return httpClient.get("/library/sections/${sectionKey}/${selectType}") {
            parameters {
                append("type", type)
                append("sort", sort)
                append("title", title)
                append("includeCollections", includeCollections)
                append("includeMeta", includeMeta)
                append("includeExternalMedia", includeExternalMedia)
                append("X-Plex-Container-Start", start)
                append("X-Plex-Container-Size", pageSize)
                append("artist.id", artistId)
                append("album.id", albumId)
                append("genre", genreIds)
                append("album.collection", albumCollection)
                append("track.collection", trackCollection)
                append("artist.collection", artistCollection)
                append("decade", decade)
                append("album.decade", albumDecade)
                append("album.year>>=", albumStartYear)
                append("album.year<<=", albumEndYear)
                append("artist.title", artistTitle)
                appendAll(params)

            }
        }.body()
    }


    suspend fun getLibraryInfo(
        sectionKey: String,
        includeConcerts: Int = 1,
        includeExtras: Int = 1,
        includeOnDeck: Int = 1,
        includePopularLeaves: Int = 1,
        includePreferences: Int = 1,
        includeChapters: Int = 1,
        includeStations: Int = 1,
        includeMarkers: Int = 1,
        includeExternalMedia: Int = 1,
        asyncAugmentMetadata: Int = 1,
        includeRelated: Int = 1,
        checkFiles: Int = 1,
        asyncRefreshAnalysis: Int = 1,
        asyncRefreshLocalMediaAgent: Int = 1,
    ): PlexResponse<ItemInfoResponse> {
        return httpClient.get("/library/metadata/${sectionKey}") {
            parameters {
                append("includeConcerts", includeConcerts)
                append("includeExtras", includeExtras)
                append("includeOnDeck", includeOnDeck)
                append("includePopularLeaves", includePopularLeaves)
                append("includePreferences", includePreferences)
                append("includeChapters", includeChapters)
                append("includeStations", includeStations)
                append("includeMarkers", includeMarkers)
                append("includeExternalMedia", includeExternalMedia)
                append("asyncAugmentMetadata", asyncAugmentMetadata)
                append("includeRelated", includeRelated)
                append("checkFiles", checkFiles)
                append("asyncRefreshAnalysis", asyncRefreshAnalysis)
                append("asyncRefreshLocalMediaAgent", asyncRefreshLocalMediaAgent)
            }
        }.body()
    }


    suspend fun getGenres(
        sectionKey: String,
        type: Int? = 10,
        sort: String? = null,
        title: String? = null,
        start: Int? = null,
        pageSize: Int? = null
    ): PlexResponse<PlexLibrary> {
        return httpClient.get("/library/sections/${sectionKey}/genre") {
            parameters {
                append("type", type)
                append("sort", sort)
                append("title", title)
                append("X-Plex-Container-Start", start)
                append("X-Plex-Container-Size", pageSize)
            }
        }.body()
    }
}