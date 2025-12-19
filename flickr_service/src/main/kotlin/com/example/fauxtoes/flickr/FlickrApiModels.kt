package com.example.fauxtoes.flickr

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class FlickrRecentPhotosResponse(
    val photos: FlickrPhotosPage,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrSearchPhotosResponse(
    val photos: FlickrPhotosPage,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrPhoto(
    val id: String,
    val owner: String,
    val secret: String,
    val server: String,
    val farm: Int,
    val title: String,
    @Json(name = "ispublic") val isPublic: Boolean,
    @Json(name = "isfriend") val isFriend: Boolean,
    @Json(name = "isfamily") val isFamily: Boolean
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrPhotosPage(
    val page: Int,
    val pages: Int,
    val perpage: Int,
    val total: Int,
    val photo: List<FlickrPhoto>
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrPhotoInfoResponse(
    val photo: FlickrPhotoInfo,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrPhotoInfo(
    val id: String,
    val secret: String,
    val server: String,
    val farm: Int,
    @Json(name = "dateuploaded") val dateUploaded: String?,
    @Json(name = "isfavorite") val isFavorite: Boolean?,
    val license: String?,
    @Json(name = "safety_level") val safetyLevel: String?,
    val rotation: Int?,
    val owner: FlickrOwner?,
    val title: FlickrTextContent?,
    val description: FlickrTextContent?,
    val visibility: FlickrVisibility?,
    val dates: FlickrDates?,
    val views: String?,
    val editability: FlickrEditability?,
    @Json(name = "publiceditability") val publicEditability: FlickrPublicEditability?,
    val usage: FlickrUsage?,
    val comments: FlickrComments?,
    val notes: FlickrNotes?,
    val people: FlickrPeople?,
    val tags: FlickrTags?,
    val urls: FlickrUrls?,
    val media: String?,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrOwner(
    val nsid: String,
    val username: String?,
    val realname: String?,
    val location: String?,
    @Json(name = "iconserver") val iconServer: String?,
    @Json(name = "iconfarm") val iconFarm: Int?,
    @Json(name = "path_alias") val pathAlias: String?,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrTextContent(
    @Json(name = "_content") val content: String?,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrVisibility(
    @Json(name = "ispublic") val isPublic: Int,
    @Json(name = "isfriend") val isFriend: Int,
    @Json(name = "isfamily") val isFamily: Int,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrDates(
    val posted: String,
    val taken: String,
    @Json(name = "takengranularity") val takenGranularity: Int,
    @Json(name = "takenunknown") val takenUnknown: String,
    @Json(name = "lastupdate") val lastUpdate: String,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrEditability(
    @Json(name = "cancomment") val canComment: Int,
    @Json(name = "canaddmeta") val canAddMeta: Int,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrPublicEditability(
    @Json(name = "cancomment") val canComment: Int,
    @Json(name = "canaddmeta") val canAddMeta: Int,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrUsage(
    @Json(name = "candownload") val canDownload: Int,
    @Json(name = "canblog") val canBlog: Int,
    @Json(name = "canprint") val canPrint: Int,
    @Json(name = "canshare") val canShare: Int,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrComments(
    @Json(name = "_content") val content: String?,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrNotes(
    val note: List<FlickrNote>?,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrNote(
    val id: String,
    val author: String,
    @Json(name = "authorname") val authorName: String,
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
    @Json(name = "_content") val content: String,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrPeople(
    @Json(name = "haspeople") val hasPeople: Int,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrTags(
    val tag: List<FlickrTag>?,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrTag(
    val id: String,
    val author: String,
    @Json(name = "authorname") val authorName: String,
    val raw: String,
    @Json(name = "_content") val content: String,
    @Json(name = "machine_tag") val machineTag: Int,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrUrls(
    val url: List<FlickrUrl>?,
)

@Keep
@JsonClass(generateAdapter = true)
data class FlickrUrl(
    val type: String,
    @Json(name = "_content") val content: String,
)
