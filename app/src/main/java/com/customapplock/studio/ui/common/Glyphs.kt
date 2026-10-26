package com.customapplock.studio.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.customapplock.studio.domain.LockType
import com.customapplock.studio.ui.theme.Palette

enum class Glyph { LOCK, SHIELD, ACCESS, OVERLAY, BELL, CAMERA, EYE, CHECK, HOME }

/** Hand-drawn vector icons (no emoji, no extra icon library). */
@Composable
fun GlyphIcon(glyph: Glyph, modifier: Modifier = Modifier, iconSize: Dp = 24.dp, tint: Color = Color.White) {
    Canvas(modifier.size(iconSize)) {
        val w = size.width
        val h = size.height
        val sw = w * 0.085f
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val hole = Color(0xAA05080F)
        when (glyph) {
            Glyph.LOCK -> {
                drawRoundRect(tint, Offset(w * .18f, h * .44f), Size(w * .64f, h * .46f), CornerRadius(w * .12f))
                drawArc(tint, 180f, 180f, false, Offset(w * .30f, h * .12f), Size(w * .40f, h * .64f), style = stroke)
                drawCircle(hole, w * .07f, Offset(w * .5f, h * .62f))
                drawLine(hole, Offset(w * .5f, h * .62f), Offset(w * .5f, h * .76f), sw * .8f, StrokeCap.Round)
            }
            Glyph.SHIELD -> {
                val p = Path()
                p.moveTo(w * .5f, h * .06f)
                p.lineTo(w * .88f, h * .2f)
                p.lineTo(w * .88f, h * .5f)
                p.quadraticBezierTo(w * .88f, h * .8f, w * .5f, h * .95f)
                p.quadraticBezierTo(w * .12f, h * .8f, w * .12f, h * .5f)
                p.lineTo(w * .12f, h * .2f)
                p.close()
                drawPath(p, tint)
                val c = Path()
                c.moveTo(w * .34f, h * .5f)
                c.lineTo(w * .46f, h * .62f)
                c.lineTo(w * .68f, h * .38f)
                drawPath(c, hole, style = Stroke(width = w * .09f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            Glyph.ACCESS -> {
                drawCircle(tint, w * .09f, Offset(w * .5f, h * .16f))
                drawLine(tint, Offset(w * .2f, h * .36f), Offset(w * .8f, h * .36f), sw, StrokeCap.Round)
                drawLine(tint, Offset(w * .5f, h * .36f), Offset(w * .5f, h * .62f), sw, StrokeCap.Round)
                drawLine(tint, Offset(w * .5f, h * .62f), Offset(w * .34f, h * .9f), sw, StrokeCap.Round)
                drawLine(tint, Offset(w * .5f, h * .62f), Offset(w * .66f, h * .9f), sw, StrokeCap.Round)
            }
            Glyph.OVERLAY -> {
                drawRoundRect(tint.copy(alpha = .45f), Offset(w * .1f, h * .1f), Size(w * .55f, h * .55f), CornerRadius(w * .1f))
                drawRoundRect(tint, Offset(w * .35f, h * .35f), Size(w * .55f, h * .55f), CornerRadius(w * .1f), style = Stroke(width = sw))
            }
            Glyph.BELL -> {
                val p = Path()
                p.moveTo(w * .22f, h * .72f)
                p.quadraticBezierTo(w * .30f, h * .62f, w * .30f, h * .42f)
                p.quadraticBezierTo(w * .30f, h * .2f, w * .5f, h * .2f)
                p.quadraticBezierTo(w * .70f, h * .2f, w * .70f, h * .42f)
                p.quadraticBezierTo(w * .70f, h * .62f, w * .78f, h * .72f)
                p.close()
                drawPath(p, tint)
                drawCircle(tint, w * .07f, Offset(w * .5f, h * .83f))
                drawCircle(tint, w * .045f, Offset(w * .5f, h * .14f))
            }
            Glyph.CAMERA -> {
                drawRoundRect(tint, Offset(w * .1f, h * .28f), Size(w * .8f, h * .54f), CornerRadius(w * .1f))
                drawRoundRect(tint, Offset(w * .34f, h * .18f), Size(w * .32f, h * .14f), CornerRadius(w * .04f))
                drawCircle(hole, w * .17f, Offset(w * .5f, h * .55f))
                drawCircle(tint, w * .1f, Offset(w * .5f, h * .55f))
            }
            Glyph.EYE -> {
                val p = Path()
                p.moveTo(w * .08f, h * .5f)
                p.quadraticBezierTo(w * .5f, h * .1f, w * .92f, h * .5f)
                p.quadraticBezierTo(w * .5f, h * .9f, w * .08f, h * .5f)
                p.close()
                drawPath(p, tint, style = stroke)
                drawCircle(tint, w * .12f, Offset(w * .5f, h * .5f))
            }
            Glyph.CHECK -> {
                val p = Path()
                p.moveTo(w * .2f, h * .52f)
                p.lineTo(w * .42f, h * .74f)
                p.lineTo(w * .8f, h * .28f)
                drawPath(p, tint, style = Stroke(width = w * .12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            Glyph.HOME -> {
                val p = Path()
                p.moveTo(w * .5f, h * .12f)
                p.lineTo(w * .92f, h * .5f)
                p.lineTo(w * .8f, h * .5f)
                p.lineTo(w * .8f, h * .88f)
                p.lineTo(w * .2f, h * .88f)
                p.lineTo(w * .2f, h * .5f)
                p.lineTo(w * .08f, h * .5f)
                p.close()
                drawPath(p, tint)
            }
        }
    }
}

/** Pictogram for every lock design. */
@Composable
fun LockTypeIcon(type: LockType, modifier: Modifier = Modifier, iconSize: Dp = 28.dp, tint: Color = Color.White) {
    Canvas(modifier.size(iconSize)) {
        val w = size.width
        val h = size.height
        val sw = w * 0.07f
        val dim = tint.copy(alpha = .35f)
        when (type) {
            LockType.NUMBER_PATTERN -> {
                val xs = floatArrayOf(.2f, .5f, .8f)
                val on = intArrayOf(0, 3, 4, 5, 8)
                for (k in 1 until on.size) {
                    val a = on[k - 1]
                    val b = on[k]
                    drawLine(tint, Offset(w * xs[a % 3], h * xs[a / 3]), Offset(w * xs[b % 3], h * xs[b / 3]), sw, StrokeCap.Round)
                }
                for (i in 0 until 9) {
                    drawCircle(if (i in on) tint else dim, w * .075f, Offset(w * xs[i % 3], h * xs[i / 3]))
                }
            }
            LockType.JIGSAW -> {
                drawRoundRect(tint, Offset(w * .12f, h * .22f), Size(w * .62f, h * .62f), CornerRadius(w * .1f))
                drawCircle(tint, w * .11f, Offset(w * .86f, h * .53f))
                drawCircle(Color(0xFF05080F), w * .09f, Offset(w * .43f, h * .22f))
            }
            LockType.SCROLL_WHEEL -> {
                drawRoundRect(dim, Offset(w * .10f, h * .18f), Size(w * .22f, h * .64f), CornerRadius(w * .08f))
                drawRoundRect(tint, Offset(w * .39f, h * .1f), Size(w * .22f, h * .8f), CornerRadius(w * .08f))
                drawRoundRect(dim, Offset(w * .68f, h * .18f), Size(w * .22f, h * .64f), CornerRadius(w * .08f))
            }
            LockType.COLOR_SEQUENCE -> {
                drawCircle(tint, w * .2f, Offset(w * .32f, h * .36f))
                drawCircle(tint.copy(alpha = .7f), w * .2f, Offset(w * .68f, h * .36f))
                drawCircle(tint.copy(alpha = .45f), w * .2f, Offset(w * .5f, h * .68f))
            }
            LockType.SHAPE_MATCH -> {
                drawCircle(tint, w * .17f, Offset(w * .3f, h * .3f))
                drawRoundRect(tint.copy(alpha = .7f), Offset(w * .56f, h * .14f), Size(w * .3f, h * .3f), CornerRadius(w * .05f))
                val p = Path()
                p.moveTo(w * .5f, h * .56f)
                p.lineTo(w * .74f, h * .9f)
                p.lineTo(w * .26f, h * .9f)
                p.close()
                drawPath(p, tint.copy(alpha = .5f))
            }
            LockType.CLOCK_HAND -> {
                drawCircle(tint, w * .4f, Offset(w * .5f, h * .5f), style = Stroke(width = sw))
                drawLine(tint, Offset(w * .5f, h * .5f), Offset(w * .5f, h * .26f), sw, StrokeCap.Round)
                drawLine(tint, Offset(w * .5f, h * .5f), Offset(w * .68f, h * .6f), sw, StrokeCap.Round)
            }
            LockType.EMOJI_MATRIX -> {
                for (r in 0 until 3) for (c in 0 until 3) {
                    val on = (r + c) % 2 == 0
                    drawRoundRect(
                        if (on) tint else dim,
                        Offset(w * (.12f + c * .30f), h * (.12f + r * .30f)),
                        Size(w * .22f, h * .22f), CornerRadius(w * .06f)
                    )
                }
            }
            LockType.CALCULATOR -> {
                drawRoundRect(tint, Offset(w * .16f, h * .08f), Size(w * .68f, h * .84f), CornerRadius(w * .1f), style = Stroke(width = sw))
                drawRoundRect(tint, Offset(w * .26f, h * .18f), Size(w * .48f, h * .16f), CornerRadius(w * .04f))
                for (r in 0 until 2) for (c in 0 until 3) {
                    drawCircle(tint, w * .05f, Offset(w * (.31f + c * .19f), h * (.5f + r * .17f)))
                }
            }
            LockType.BUBBLE_POP -> {
                drawCircle(tint, w * .22f, Offset(w * .34f, h * .62f))
                drawCircle(tint.copy(alpha = .65f), w * .16f, Offset(w * .7f, h * .4f))
                drawCircle(tint.copy(alpha = .4f), w * .1f, Offset(w * .42f, h * .24f))
            }
            LockType.SWIPE -> {
                drawLine(tint, Offset(w * .18f, h * .76f), Offset(w * .8f, h * .24f), sw, StrokeCap.Round)
                drawLine(tint, Offset(w * .8f, h * .24f), Offset(w * .5f, h * .26f), sw, StrokeCap.Round)
                drawLine(tint, Offset(w * .8f, h * .24f), Offset(w * .76f, h * .54f), sw, StrokeCap.Round)
            }
        }
    }
}

/** Pulsing shield used on the home dashboard. */
@Composable
fun ShieldBadge(active: Boolean, modifier: Modifier = Modifier, badgeSize: Dp = 88.dp) {
    val t = rememberInfiniteTransition(label = "shield")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing)), label = "p")
    Box(modifier.size(badgeSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            for (k in 0 until 2) {
                val q = (p + k * 0.5f) % 1f
                drawCircle(
                    Palette.BlueLight.copy(alpha = (1f - q) * (if (active) .35f else .08f)),
                    r * (0.55f + 0.45f * q), center
                )
            }
            drawCircle(
                Brush.radialGradient(listOf(Palette.BlueLight, Palette.BlueDeep), center = center, radius = r * .6f),
                r * .55f, center
            )
        }
        GlyphIcon(Glyph.SHIELD, iconSize = badgeSize * 0.42f, tint = Color.White)
    }
}
