package com.apkupdater.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.content.ContextCompat
import com.apkupdater.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import okhttp3.OkHttpClient
import java.security.MessageDigest
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// A clickable modifier that will disable the default ripple
fun Modifier.clickableNoRipple(onClick: () -> Unit) = this.
	clickable(MutableInteractionSource(), null, onClick = onClick)

// Launches a coroutine and executes it inside the mutex
fun CoroutineScope.launchWithMutex(
	mutex: Mutex,
	context: CoroutineContext = EmptyCoroutineContext,
	block: suspend CoroutineScope.() -> Unit
) = launch(context) {
	mutex.withLock {
		block()
	}
}

fun Boolean?.orFalse() = this ?: false

fun PackageInfo.name(context: Context) = applicationInfo.loadLabel(context.packageManager).toString()

fun Context.getAppIcon(packageName: String) = runCatching {
	packageManager.getApplicationIcon(packageName)
}.getOrElse {
	Log.e("getAppIcon", "App not found. Uninstalled most likely.")
	ContextCompat.getDrawable(this, R.drawable.ic_root)
}

fun Context.getAppName(packageName: String): String = runCatching {
	packageManager.getPackageInfo(packageName, 0).name(this)
}.getOrDefault("")

inline fun <reified T> List<Flow<T>>.combine(crossinline block: suspend (Array<T>) -> Unit) =
	combine(this) { block(it) }

fun ByteArray.toSha1(): String = MessageDigest
	.getInstance("SHA-1")
	.digest(this)
	.joinToString(separator = "", transform = { "%02x".format(it) })

fun String.toSha1Aptoide(): String = chunked(2).joinToString(separator = ":") { it.uppercase() }

fun ByteArray.toSha256(): String = MessageDigest
	.getInstance("SHA-256")
	.digest(this)
	.joinToString(separator = "", transform = { "%02x".format(it) })

fun PackageInfo.getSignature(): ByteArray = runCatching {
	if (Build.VERSION.SDK_INT >= 28) {
		signingInfo.apkContentsSigners[0].toByteArray()
	} else {
		@Suppress("DEPRECATION")
		signatures[0].toByteArray()
	}
}.getOrDefault(ByteArray(0))

fun PackageInfo.getSignatureSha1(): String = getSignature().toSha1()

fun PackageInfo.getSignatureSha256(): String = getSignature().toSha256()

fun millisUntilHour(hour: Int): Long {
	val calendar = Calendar.getInstance()
	if (calendar.get(Calendar.HOUR_OF_DAY) >= hour) calendar.add(Calendar.HOUR, 24)
	calendar.set(Calendar.HOUR_OF_DAY, hour)
	calendar.set(Calendar.MINUTE, 0)
	return calendar.timeInMillis - System.currentTimeMillis()
}

suspend fun AtomicBoolean.lock() {
	while (get()) yield()
	set(true)
}

fun AtomicBoolean.unlock() = set(false)

fun Intent.getIntentExtra(): Intent? = when {
	Build.VERSION.SDK_INT > 33 -> getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
	else -> @Suppress("DEPRECATION") getParcelableExtra(Intent.EXTRA_INTENT)
}

fun Intent.getAppId() = runCatching {
	action?.split(".")?.get(1)?.toInt()
}.getOrNull()

fun PackageManager.isAndroidTv() = hasSystemFeature(PackageManager.FEATURE_LEANBACK)

fun Context.isAndroidTv() = packageManager.isAndroidTv()

fun randomUUID() = UUID.randomUUID().toString()

fun isDark() = Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
	val spanned = this@toAnnotatedString
	append(spanned.toString())
	getSpans(0, spanned.length, Any::class.java).forEach { span ->
		val start = getSpanStart(span)
		val end = getSpanEnd(span)
		when (span) {
			is StyleSpan -> when (span.style) {
				Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
				Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
				Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
			}
			is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
			is ForegroundColorSpan -> addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
		}
	}
}

fun OkHttpClient.Builder.addUserAgentInterceptor(agent: String) = addNetworkInterceptor {
	it.proceed(it.request().newBuilder().header("User-Agent", agent).build())
}

fun filterVersionTag(version: String) = version
	.replace(Regex("^\\D*"), "")
	//.replace(Regex("\\D+\$"), "") // In case we want to remove non-numeric at end too

fun Float.to2f() = String
	.format("%.2f", this)
	.replace('.', DecimalFormatSymbols.getInstance(Locale.getDefault()).decimalSeparator)
