package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Enum ImageType.
 */
@JsonClass(generateAdapter = false)
enum class ImageType(
    private val serialName: String,
) {
    @Json(name = "Primary")
    PRIMARY("Primary"),
    @Json(name = "Art")
    ART("Art"),
    @Json(name = "Backdrop")
    BACKDROP("Backdrop"),
    @Json(name = "Banner")
    BANNER("Banner"),
    @Json(name = "Logo")
    LOGO("Logo"),
    @Json(name = "Thumb")
    THUMB("Thumb"),
    @Json(name = "Disc")
    DISC("Disc"),
    @Json(name = "Box")
    BOX("Box"),
    @Json(name = "Screenshot")
    SCREENSHOT("Screenshot"),
    @Json(name = "Menu")
    MENU("Menu"),
    @Json(name = "Chapter")
    CHAPTER("Chapter"),
    @Json(name = "BoxRear")
    BOX_REAR("BoxRear"),
    @Json(name = "Profile")
    PROFILE("Profile"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}
