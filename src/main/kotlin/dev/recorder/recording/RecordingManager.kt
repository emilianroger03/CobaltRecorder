package dev.recorder.recording

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.cobalt.util.ChatUtils
import java.io.File

object RecordingManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val recordingsDir: File by lazy {
        File("config/cobalt/recordings").also { it.mkdirs() }
    }

    var currentRecording: Recording? = null
        private set

    var isRecording = false
        private set

    private var recordingTick = 0

    fun startRecording(name: String, startPos: Position) {
        currentRecording = Recording(
            name = name,
            startPosition = startPos
        )
        recordingTick = 0
        isRecording = true
        ChatUtils.sendSystemMessage("Recording started: $name")
    }

    fun stopRecording(): Recording? {
        isRecording = false
        val recording = currentRecording
        if (recording != null) {
            ChatUtils.sendSystemMessage("Recording stopped: ${recording.frameCount} frames captured")
        }
        return recording
    }

    fun addFrame(frame: RecordedFrame) {
        if (!isRecording) return
        currentRecording?.frames?.add(frame.copy(tick = recordingTick))
        recordingTick++
    }

    fun saveRecording(recording: Recording): Boolean {
        return try {
            val file = File(recordingsDir, "${recording.name}.json")
            file.writeText(json.encodeToString(recording))
            ChatUtils.sendSystemMessage("Saved recording: ${recording.name}")
            true
        } catch (e: Exception) {
            ChatUtils.sendSystemMessage("Failed to save: ${e.message}")
            false
        }
    }

    fun loadRecording(name: String): Recording? {
        return try {
            val file = File(recordingsDir, "$name.json")
            if (!file.exists()) {
                ChatUtils.sendSystemMessage("Recording not found: $name")
                return null
            }
            val recording = json.decodeFromString<Recording>(file.readText())
            ChatUtils.sendSystemMessage("Loaded recording: $name (${recording.frameCount} frames)")
            recording
        } catch (e: Exception) {
            ChatUtils.sendSystemMessage("Failed to load: ${e.message}")
            null
        }
    }

    fun listRecordings(): List<String> {
        return recordingsDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }

    fun deleteRecording(name: String): Boolean {
        val file = File(recordingsDir, "$name.json")
        return if (file.exists()) {
            file.delete()
            ChatUtils.sendSystemMessage("Deleted recording: $name")
            true
        } else {
            ChatUtils.sendSystemMessage("Recording not found: $name")
            false
        }
    }
}
