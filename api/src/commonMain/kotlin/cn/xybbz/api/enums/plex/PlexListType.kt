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

package cn.xybbz.api.enums.plex

import kotlinx.serialization.Serializable

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
@Serializable
enum class PlexListType(val serialName: String) {
    all("all"),
    genre("genre")


    ;


    override fun toString(): String = serialName
}