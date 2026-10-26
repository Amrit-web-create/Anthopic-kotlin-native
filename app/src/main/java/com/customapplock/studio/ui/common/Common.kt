package com.customapplock.studio.ui.common

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.customapplock.studio.R
import com.customapplock.studio.domain.HOME_PKG
import com.customapplock.studio.domain.MASTER_PKG
import com.customapplock.studio.ui.theme.Palette
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/** When true the lock designs hide their drawing trail (shoulder-surfing protection). */
val LocalStealth = compositionLocalOf { false }

class Haptics(private val v: View) {
    fun tick() { v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) }
    fun snap() { v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }
    fun success() {
        v.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.LONG_PRESS
        )
    }
    fun error() {
        v.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS
        )
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val v = LocalView.current
    return remember(v) { Haptics(v) }
}

fun Context.findFragmentActivity(): FragmentActivity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is FragmentActivity) return c
        c = c.baseContext
    }
    return null
}

@Composable
fun AppIcon(pkg: String, size: Dp, loader: suspend (String) -> ImageBitmap?) {
    if (pkg == MASTER_PKG || pkg == HOME_PKG) {
        Box(
            Modifier.size(size).clip(RoundedCornerShape(size / 3.4f))
                .background(Brush.linearGradient(listOf(Palette.Blue, Palette.BlueDeep))),
            contentAlignment = Alignment.Center
        ) {
            GlyphIcon(if (pkg == HOME_PKG) Glyph.HOME else Glyph.LOCK, iconSize = size * 0.55f, tint = Color.White)
        }
        return
    }
    val bmp by produceState<ImageBitmap?>(null, pkg) { value = loader(pkg) }
    Box(Modifier.size(size).clip(RoundedCornerShape(size / 3.4f)), contentAlignment = Alignment.Center) {
        val b = bmp
        if (b != null) Image(b, null, Modifier.fillMaxSize())
        else Box(Modifier.fillMaxSize().background(Palette.SurfaceHigh))
    }
}

/** 320x50 banner, shown on the lock overlay only. Fixed height + margins so it never touches lock controls. */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    Box(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(50.dp), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { c ->
                AdView(c).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = ctx.getString(R.string.admob_banner_id)
                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { it.destroy() }
        )
    }
}
