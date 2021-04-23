package moe.tlaster.swiper

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*
import kotlin.math.absoluteValue
import kotlin.math.withSign

@Composable
fun rememberSwiperState(
    onStart: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onEnd: () -> Unit = {},
): SwiperState {
    return remember {
        SwiperState(
            onStart = onStart,
            onDismiss = onDismiss,
            onEnd = onEnd,
        )
    }
}

@Stable
class SwiperState(
    internal val onStart: () -> Unit = {},
    internal val onDismiss: () -> Unit = {},
    internal val onEnd: () -> Unit = {},
) {
    internal var maxHeight: Int = 0
        set(value) {
            field = value
            _offset.updateBounds(lowerBound = -value.toFloat(), upperBound = value.toFloat())
        }
    internal var dismissed by mutableStateOf(false)
    private var _offset = Animatable(0f)

    val offset: Float
        get() = _offset.value

    val progress: Float
        get() = (offset.absoluteValue / (if (maxHeight == 0) 1 else maxHeight)).coerceIn(
            maximumValue = 1f,
            minimumValue = 0f
        )

    internal suspend fun snap(value: Float) {
        _offset.snapTo(value)
    }

    internal suspend fun fling(velocity: Float) {
        val value = _offset.value
        when {
            velocity.absoluteValue > 4000f -> {
                dismiss(velocity)
            }
            value.absoluteValue < maxHeight * 0.5 -> {
                restore()
            }
            value.absoluteValue < maxHeight -> {
                dismiss(velocity)
            }
        }
    }

    private suspend fun dismiss(velocity: Float) {
        dismissed = true
        _offset.animateTo(maxHeight.toFloat().withSign(_offset.value), initialVelocity = velocity)
        onDismiss.invoke()
    }

    private suspend fun restore() {
        onEnd.invoke()
        _offset.animateTo(0f)
    }
}