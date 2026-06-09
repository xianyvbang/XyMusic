package cn.xybbz.api.enums.subsonic

enum class AlbumType(val type:String) {
    RANDOM("random"),
    NEWEST("newest"),
    HIGHEST("highest"),
    FREQUENT("frequent"),
    RECENT("recent"),
    STARRED("starred"),
    ALPHABETICAL_BY_NAME("alphabeticalByName"),
    ALPHABETICAL_BY_ARTIST("alphabeticalByArtist"),
    BY_YEAR("byYear"),
    BY_GENRE("byGenre ");

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = type
}