package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun AiInterviewerAvatar(
    isReadingQuestion: Boolean,
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_anim")

    // Floating breathing animation (±6dp)
    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    // Eye blink cycle (scaleY squishes to ~0.08 periodically)
    val blinkScaleY by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blink_scale"
    )

    // Ear glow pulse when recording / listening
    val earGlowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRecording) 1.35f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRecording) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ear_glow"
    )

    // Audio wave accent pulse on the sides — listening/recording only.
    // (Speaking no longer drives the side waves; see viseme mouth animation below.)
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = if (isRecording) 0.35f else 0.1f,
        targetValue = if (isRecording) 1.0f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRecording) 600 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_alpha"
    )

    // Viseme-based multi-parameter mouth animatables
    val jawDropAnim = remember { Animatable(0f) }
    val lipWidthAnim = remember { Animatable(0.5f) }
    val lipCornerPullAnim = remember { Animatable(0.2f) }
    val asymmetryAnim = remember { Animatable(0f) }

    // Reusable Path objects to ensure 60fps performance without per-frame allocations
    val leftWavePath = remember { Path() }
    val rightWavePath = remember { Path() }
    val suitPath = remember { Path() }
    val leftLapelPath = remember { Path() }
    val rightLapelPath = remember { Path() }
    val shirtPath = remember { Path() }
    val collarLPath = remember { Path() }
    val collarRPath = remember { Path() }
    val tieKnotPath = remember { Path() }
    val tieBodyPath = remember { Path() }
    val neckPath = remember { Path() }
    val mouthPath = remember { Path() }
    val teethPath = remember { Path() }

    // Multi-parameter viseme speech generator — smoother & more natural:
    // weighted toward subtle/rest shapes (real speech spends most of its time
    // near-neutral, with occasional bigger accents), tighter amplitude ranges,
    // and a short hold so motion reads as continuous talking rather than
    // snap-then-freeze.
    LaunchedEffect(isReadingQuestion) {
        if (isReadingQuestion) {
            val random = Random(System.currentTimeMillis())

            val weights = floatArrayOf(0.32f, 0.10f, 0.16f, 0.16f, 0.16f, 0.10f)
            fun pickViseme(): Int {
                val r = random.nextFloat()
                var acc = 0f
                for (i in weights.indices) {
                    acc += weights[i]
                    if (r <= acc) return i
                }
                return weights.lastIndex
            }

            while (isActive) {
                // Select a phonetic speech viseme archetype
                val targetJaw: Float
                val targetWidth: Float
                val targetPull: Float
                val targetAsym: Float
                val tweenDuration: Int

                when (pickViseme()) {
                    0 -> { // Rest / plosive (M, B, P)
                        targetJaw = random.nextFloat() * 0.08f
                        targetWidth = 0.45f + random.nextFloat() * 0.10f
                        targetPull = 0.20f + random.nextFloat() * 0.10f
                        targetAsym = (random.nextFloat() - 0.5f) * 0.05f
                        tweenDuration = 90 + random.nextInt(50)
                    }
                    1 -> { // Open vowel (AA, AH, O) - moderate jaw drop, teeth visible
                        targetJaw = 0.42f + random.nextFloat() * 0.24f
                        targetWidth = 0.46f + random.nextFloat() * 0.16f
                        targetPull = 0.22f + random.nextFloat() * 0.16f
                        targetAsym = (random.nextFloat() - 0.5f) * 0.08f
                        tweenDuration = 150 + random.nextInt(70)
                    }
                    2 -> { // Spread vowel (EE, IH, EY) - horizontal stretch
                        targetJaw = 0.22f + random.nextFloat() * 0.16f
                        targetWidth = 0.66f + random.nextFloat() * 0.16f
                        targetPull = 0.42f + random.nextFloat() * 0.22f
                        targetAsym = (random.nextFloat() - 0.5f) * 0.07f
                        tweenDuration = 120 + random.nextInt(60)
                    }
                    3 -> { // Rounded vowel (OO, W, OH) - narrow puckered lips
                        targetJaw = 0.32f + random.nextFloat() * 0.18f
                        targetWidth = 0.22f + random.nextFloat() * 0.13f
                        targetPull = 0.12f + random.nextFloat() * 0.12f
                        targetAsym = (random.nextFloat() - 0.5f) * 0.05f
                        tweenDuration = 130 + random.nextInt(60)
                    }
                    4 -> { // Dental / Fricative (S, Z, TH, T) - small gap
                        targetJaw = 0.14f + random.nextFloat() * 0.12f
                        targetWidth = 0.56f + random.nextFloat() * 0.16f
                        targetPull = 0.30f + random.nextFloat() * 0.16f
                        targetAsym = (random.nextFloat() - 0.5f) * 0.07f
                        tweenDuration = 100 + random.nextInt(50)
                    }
                    else -> { // Labiodental (F, V) - slight asymmetric tuck
                        targetJaw = 0.12f + random.nextFloat() * 0.10f
                        targetWidth = 0.50f + random.nextFloat() * 0.14f
                        targetPull = 0.18f + random.nextFloat() * 0.14f
                        targetAsym = 0.03f + random.nextFloat() * 0.06f
                        tweenDuration = 95 + random.nextInt(45)
                    }
                }

                coroutineScope {
                    launch { jawDropAnim.animateTo(targetJaw, tween(tweenDuration, easing = FastOutSlowInEasing)) }
                    launch { lipWidthAnim.animateTo(targetWidth, tween(tweenDuration, easing = FastOutSlowInEasing)) }
                    launch { lipCornerPullAnim.animateTo(targetPull, tween(tweenDuration, easing = FastOutSlowInEasing)) }
                    launch { asymmetryAnim.animateTo(targetAsym, tween(tweenDuration, easing = FastOutSlowInEasing)) }
                }
                // Short hold instead of a near-double freeze — keeps motion
                // reading as continuous speech instead of snap-then-pause.
                delay((tweenDuration * 0.25).toLong())
            }
        } else {
            // Return to resting closed smile state
            coroutineScope {
                launch { jawDropAnim.animateTo(0f, tween(220, easing = FastOutSlowInEasing)) }
                launch { lipWidthAnim.animateTo(0.5f, tween(220, easing = FastOutSlowInEasing)) }
                launch { lipCornerPullAnim.animateTo(0.2f, tween(220, easing = FastOutSlowInEasing)) }
                launch { asymmetryAnim.animateTo(0f, tween(220, easing = FastOutSlowInEasing)) }
            }
        }
    }

    Box(
        modifier = modifier
            .size(260.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .graphicsLayer {
                translationY = floatOffsetY
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(260.dp)) {
            val scale = size.width / 1200f

            fun sx(x: Float) = x * scale
            fun sy(y: Float) = y * scale

            // 1. SOFT GLOW BEHIND AVATAR
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isReadingQuestion) Color(0xFF63F2FF).copy(alpha = 0.22f)
                        else if (isRecording) Color(0xFFFF9F12).copy(alpha = 0.22f)
                        else Color(0xFF6D5DF6).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(sx(600f), sy(500f)),
                    radius = sx(520f)
                ),
                center = Offset(sx(600f), sy(500f)),
                radius = sx(520f)
            )

            // 2. AUDIO ACCENT WAVES (Left and Right) — listening/recording only.
            // No longer drawn while speaking; the mouth animation carries that now.
            if (isRecording) {
                val waveColor = Color(0xFFFF9F12).copy(alpha = waveAlpha)

                leftWavePath.reset()
                leftWavePath.moveTo(sx(110f), sy(520f))
                leftWavePath.quadraticTo(sx(150f), sy(475f), sx(190f), sy(520f))
                leftWavePath.quadraticTo(sx(230f), sy(565f), sx(270f), sy(520f))
                drawPath(leftWavePath, waveColor, style = Stroke(width = sx(12f), cap = StrokeCap.Round))

                rightWavePath.reset()
                rightWavePath.moveTo(sx(1090f), sy(520f))
                rightWavePath.quadraticTo(sx(1050f), sy(475f), sx(1010f), sy(520f))
                rightWavePath.quadraticTo(sx(970f), sy(565f), sx(930f), sy(520f))
                drawPath(rightWavePath, waveColor, style = Stroke(width = sx(12f), cap = StrokeCap.Round))
            }

            // 3. SUIT BODY
            suitPath.reset()
            suitPath.moveTo(sx(145f), sy(1200f))
            suitPath.cubicTo(sx(155f), sy(1010f), sx(225f), sy(880f), sx(400f), sy(825f))
            suitPath.lineTo(sx(800f), sy(825f))
            suitPath.cubicTo(sx(975f), sy(880f), sx(1045f), sy(1010f), sx(1055f), sy(1200f))
            suitPath.close()

            drawPath(
                path = suitPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF17254D), Color(0xFF09132F)),
                    start = Offset(sx(145f), sy(825f)),
                    end = Offset(sx(1055f), sy(1200f))
                )
            )

            // LAPELS
            leftLapelPath.reset()
            leftLapelPath.moveTo(sx(390f), sy(850f))
            leftLapelPath.lineTo(sx(520f), sy(1040f))
            leftLapelPath.lineTo(sx(430f), sy(1200f))
            leftLapelPath.lineTo(sx(285f), sy(1200f))
            leftLapelPath.lineTo(sx(330f), sy(965f))
            leftLapelPath.close()
            drawPath(leftLapelPath, Color(0xFF24345F))

            rightLapelPath.reset()
            rightLapelPath.moveTo(sx(810f), sy(850f))
            rightLapelPath.lineTo(sx(680f), sy(1040f))
            rightLapelPath.lineTo(sx(770f), sy(1200f))
            rightLapelPath.lineTo(sx(915f), sy(1200f))
            rightLapelPath.lineTo(sx(870f), sy(965f))
            rightLapelPath.close()
            drawPath(rightLapelPath, Color(0xFF24345F))

            // 4. SHIRT
            shirtPath.reset()
            shirtPath.moveTo(sx(475f), sy(835f))
            shirtPath.lineTo(sx(600f), sy(900f))
            shirtPath.lineTo(sx(725f), sy(835f))
            shirtPath.lineTo(sx(690f), sy(1200f))
            shirtPath.lineTo(sx(510f), sy(1200f))
            shirtPath.close()

            drawPath(
                path = shirtPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFE9EEF5)),
                    start = Offset(sx(600f), sy(835f)),
                    end = Offset(sx(600f), sy(1200f))
                )
            )

            // COLLARS
            collarLPath.reset()
            collarLPath.moveTo(sx(475f), sy(835f))
            collarLPath.lineTo(sx(600f), sy(900f))
            collarLPath.lineTo(sx(555f), sy(1015f))
            collarLPath.lineTo(sx(450f), sy(885f))
            collarLPath.close()
            drawPath(collarLPath, Color(0xFFFFFFFF))

            collarRPath.reset()
            collarRPath.moveTo(sx(725f), sy(835f))
            collarRPath.lineTo(sx(600f), sy(900f))
            collarRPath.lineTo(sx(645f), sy(1015f))
            collarRPath.lineTo(sx(750f), sy(885f))
            collarRPath.close()
            drawPath(collarRPath, Color(0xFFFFFFFF))

            // 5. TIE
            tieKnotPath.reset()
            tieKnotPath.moveTo(sx(570f), sy(900f))
            tieKnotPath.lineTo(sx(630f), sy(900f))
            tieKnotPath.lineTo(sx(645f), sy(955f))
            tieKnotPath.lineTo(sx(600f), sy(985f))
            tieKnotPath.lineTo(sx(555f), sy(955f))
            tieKnotPath.close()
            drawPath(tieKnotPath, Color(0xFF18B9D2))

            tieBodyPath.reset()
            tieBodyPath.moveTo(sx(555f), sy(955f))
            tieBodyPath.lineTo(sx(645f), sy(955f))
            tieBodyPath.lineTo(sx(675f), sy(1135f))
            tieBodyPath.lineTo(sx(600f), sy(1200f))
            tieBodyPath.lineTo(sx(525f), sy(1135f))
            tieBodyPath.close()
            drawPath(tieBodyPath, Color(0xFF0A7F9C))

            // 6. NECK & HEAD SHELL
            neckPath.reset()
            neckPath.moveTo(sx(505f), sy(785f))
            neckPath.lineTo(sx(695f), sy(785f))
            neckPath.lineTo(sx(725f), sy(875f))
            neckPath.lineTo(sx(600f), sy(925f))
            neckPath.lineTo(sx(475f), sy(875f))
            neckPath.close()
            drawPath(neckPath, Color(0xFFDCE4EF))

            // Head Shell
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF4F7FB), Color(0xFFDCE4EF)),
                    start = Offset(sx(270f), sy(170f)),
                    end = Offset(sx(930f), sy(820f))
                ),
                topLeft = Offset(sx(270f), sy(170f)),
                size = Size(sx(660f), sy(650f)),
                cornerRadius = CornerRadius(sx(210f), sy(210f))
            )

            // Face Panel (dark glossy screen)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF101B35), Color(0xFF050B1B)),
                    start = Offset(sx(350f), sy(300f)),
                    end = Offset(sx(350f), sy(685f))
                ),
                topLeft = Offset(sx(350f), sy(300f)),
                size = Size(sx(500f), sy(385f)),
                cornerRadius = CornerRadius(sx(120f), sy(120f))
            )

            // 7. EAR MODULES
            // Left Ear
            drawOval(Color(0xFF17254D), Offset(sx(220f), sy(413f)), Size(sx(130f), sy(184f)))
            drawOval(Color(0xFF0B142D), Offset(sx(243f), sy(441f)), Size(sx(84f), sy(128f)))
            val leftEarCoreSize = Size(sx(44f * earGlowScale), sy(76f * earGlowScale))
            drawOval(
                if (isRecording) Color(0xFFFF9F12) else Color(0xFF22D3EE),
                Offset(sx(285f) - leftEarCoreSize.width / 2f, sy(505f) - leftEarCoreSize.height / 2f),
                leftEarCoreSize
            )

            // Right Ear
            drawOval(Color(0xFF17254D), Offset(sx(850f), sy(413f)), Size(sx(130f), sy(184f)))
            drawOval(Color(0xFF0B142D), Offset(sx(873f), sy(441f)), Size(sx(84f), sy(128f)))
            val rightEarCoreSize = Size(sx(44f * earGlowScale), sy(76f * earGlowScale))
            drawOval(
                if (isRecording) Color(0xFFFF9F12) else Color(0xFF22D3EE),
                Offset(sx(915f) - rightEarCoreSize.width / 2f, sy(505f) - rightEarCoreSize.height / 2f),
                rightEarCoreSize
            )

            // 8. EYES
            val eyeH = sy(116f * blinkScaleY)
            val eyeW = sx(76f)

            // Left Eye
            drawOval(
                color = Color(0xFF63F2FF),
                topLeft = Offset(sx(490f) - eyeW / 2f, sy(485f) - eyeH / 2f),
                size = Size(eyeW, eyeH)
            )
            drawOval(
                color = Color(0xFFD9FEFF),
                topLeft = Offset(sx(490f) - sx(20f), sy(485f) - (sy(35f * blinkScaleY))),
                size = Size(sx(40f), sy(70f * blinkScaleY))
            )

            // Right Eye
            drawOval(
                color = Color(0xFF63F2FF),
                topLeft = Offset(sx(710f) - eyeW / 2f, sy(485f) - eyeH / 2f),
                size = Size(eyeW, eyeH)
            )
            drawOval(
                color = Color(0xFFD9FEFF),
                topLeft = Offset(sx(710f) - sx(20f), sy(485f) - (sy(35f * blinkScaleY))),
                size = Size(sx(40f), sy(70f * blinkScaleY))
            )

            // 9. VISEME-BLENDED REALISTIC MOUTH
            val jaw = jawDropAnim.value
            val widthNorm = lipWidthAnim.value
            val pull = lipCornerPullAnim.value
            val asym = asymmetryAnim.value

            val halfW = 45f + widthNorm * 38f
            val asymY = asym * 14f

            val leftCornerX = 600f - halfW
            val leftCornerY = 585f + asymY
            val rightCornerX = 600f + halfW
            val rightCornerY = 585f - asymY

            if (jaw < 0.12f) {
                // Closed / resting smile state
                mouthPath.reset()
                mouthPath.moveTo(sx(leftCornerX), sy(leftCornerY))
                mouthPath.quadraticTo(
                    sx(600f),
                    sy(585f + 25f + pull * 10f),
                    sx(rightCornerX),
                    sy(rightCornerY)
                )
                drawPath(mouthPath, Color(0xFF57F0FF), style = Stroke(width = sx(16f), cap = StrokeCap.Round))
            } else {
                // Articulated speaking mouth cavity with blended phoneme shape
                val upperCtrlY = 580f - pull * 12f
                val lowerCtrlY = 585f + jaw * 78f + (1f - pull) * 12f

                mouthPath.reset()
                mouthPath.moveTo(sx(leftCornerX), sy(leftCornerY))
                mouthPath.quadraticTo(sx(600f), sy(upperCtrlY), sx(rightCornerX), sy(rightCornerY))
                mouthPath.quadraticTo(sx(600f), sy(lowerCtrlY), sx(leftCornerX), sy(leftCornerY))
                mouthPath.close()

                // Mouth cavity fill & contour
                drawPath(mouthPath, Color(0xFF0A0F22))
                drawPath(mouthPath, Color(0xFF57F0FF), style = Stroke(width = sx(8.5f)))

                // Brief teeth highlights on open shapes (jaw > 0.40 and width > 0.40)
                if (jaw > 0.40f && widthNorm > 0.40f) {
                    val teethHalfW = halfW * 0.65f
                    val teethDepth = (jaw * 18f).coerceAtMost(16f)

                    teethPath.reset()
                    teethPath.moveTo(sx(600f - teethHalfW), sy(upperCtrlY + 4f))
                    teethPath.quadraticTo(sx(600f), sy(upperCtrlY + 2f), sx(600f + teethHalfW), sy(upperCtrlY + 4f))
                    teethPath.quadraticTo(sx(600f), sy(upperCtrlY + 4f + teethDepth), sx(600f - teethHalfW), sy(upperCtrlY + 4f))
                    teethPath.close()

                    drawPath(teethPath, Color(0xFFF5F8FF))
                }
            }
        }
    }
}