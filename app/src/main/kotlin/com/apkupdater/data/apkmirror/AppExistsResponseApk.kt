package com.apkupdater.data.apkmirror

import com.apkupdater.data.ui.ApkMirrorSource
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.Link
import com.google.gson.annotations.SerializedName

data class AppExistsResponseApk(
	@SerializedName("version_code") val versionCode: Long = 0,
	val link: String = "",
	@SerializedName("publish_date") val publishDate: String? = null,
	val arches: List<String> = emptyList(),
	val dpis: List<String>? = null,
	val minapi: String = "0",
	val description: String? = null,
	val capabilities: List<String>? = null,
	@SerializedName("signatures-sha1")
	val signaturesSha1: List<String>? = emptyList(),
	@SerializedName("signatures-sha256")
	val signaturesSha256: List<String>? = emptyList()
)

fun AppExistsResponseApk.toAppUpdate(app: AppInstalled, release: AppExistsResponseRelease) = AppUpdate(
	app.name,
	app.packageName,
	release.version,
	app.version,
	versionCode,
	app.versionCode,
	ApkMirrorSource,
	app.iconUri,
	Link.Url("https://www.apkmirror.com$link"),
	release.whatsNew.orEmpty()
)
