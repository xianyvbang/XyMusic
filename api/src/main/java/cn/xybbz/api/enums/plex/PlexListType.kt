package cn.xybbz.api.enums.plex

import com.squareup.moshi.JsonClass

/**
 * all: All items in the section.
 * unwatched: Items that have not been played.
 * newest: Items that are recently released.
 * recentlyAdded: Items that are recently added to the library.
 * recentlyViewed: Items that were recently viewed.
 * onDeck: Items to continue watching.
 * collection: Items categorized by collection.
 * edition: Items categorized by edition.
 * genre: Items categorized by genre.
 * year: Items categorized by year of release.
 * decade: Items categorized by decade.
 * director: Items categorized by director.
 * actor: Items categorized by starring actor.
 * country: Items categorized by country of origin.
 * contentRating: Items categorized by content rating.
 * rating: Items categorized by rating.
 * resolution: Items categorized by resolution.
 * firstCharacter: Items categorized by the first letter.
 * folder: Items categorized by folder.
 * albums: Items categorized by album.
 */
@JsonClass(generateAdapter = false)
enum class PlexListType(val serialName: String) {
    all("all"),
    genre("genre")


    ;


    override fun toString(): String = serialName
}