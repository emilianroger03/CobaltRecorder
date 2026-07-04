package dev.recorder.recording

import kotlinx.serialization.Serializable

@Serializable
data class Recording(
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val frames: MutableList<RecordedFrame> = mutableListOf(),
    val startPosition: Position? = null
) {
    val duration: Int get() = frames.lastOrNull()?.tick ?: 0
    val frameCount: Int get() = frames.size
}

@Serializable
data class Position(
    val x: Double,
    val y: Double,
    val z: Double
)
