package com.example.fauxtoes.flickr

/**
 * Enum representing the size suffixes for Flickr photo URLs.
 * See: https://www.flickr.com/services/api/misc.urls.html
 */
enum class FlickrPhotoSize(val suffix: String) {
    /** Small square 75x75 */
    SQUARE_75("_s"),

    /** Large square 150x150 */
    SQUARE_150("_q"),

    /** Thumbnail, 100 on longest side */
    THUMBNAIL_100("_t"),

    /** Small, 240 on longest side */
    SMALL_240("_m"),

    /** Small, 320 on longest side */
    SMALL_320("_n"),

    /** Small, 400 on longest side */
    SMALL_400("_w"),

    /** Medium, 500 on longest side (no suffix) */
    MEDIUM_500(""),

    /** Medium, 640 on longest side */
    MEDIUM_640("_z"),

    /** Medium, 800 on longest side */
    MEDIUM_800("_c"),

    /** Large, 1024 on longest side */
    LARGE_1024("_b"),

    /** Large, 1600 on longest side */
    LARGE_1600("_h"),
    /** Large, 2048 on longest side */
    LARGE_2048("_k"),

    /** Extra Large, 3072 on longest side */
    XLARGE_3K("_3k"),

    /** Extra Large, 4096 on longest side */
    XLARGE_4K("_4k"),

    /** Extra Large, 5120 on longest side */
    XLARGE_5K("_5k"),

    /** Extra Large, 6144 on longest side */
    XLARGE_6K("_6k"),

    /** Extra Large, 8192 on longest side */
    XLARGE_8K("_8k"),

    /** Original image, either a jpg, gif or png, depending on source format */
    ORIGINAL("_o"),
}

