package dev.recorder.command

import org.cobalt.command.Command
import org.cobalt.command.annotation.DefaultHandler
import org.cobalt.command.annotation.SubCommand
import org.cobalt.util.ChatUtils
import dev.recorder.recording.Recorder
import dev.recorder.recording.RecordingManager
import dev.recorder.playback.PlaybackExecutor

class RecorderCommand : Command(
    name = "rec",
    aliases = arrayOf("record", "macro")
) {

    @DefaultHandler
    fun defaultHandler() {
        ChatUtils.sendSystemMessage("=== Cobalt Recorder ===")
        ChatUtils.sendSystemMessage("/rec start <name> - Start recording")
        ChatUtils.sendSystemMessage("/rec stop - Stop recording/playback")
        ChatUtils.sendSystemMessage("/rec save - Save current recording")
        ChatUtils.sendSystemMessage("/rec play <name> - Play a recording")
        ChatUtils.sendSystemMessage("/rec loop <name> <count> - Loop playback")
        ChatUtils.sendSystemMessage("/rec list - List all recordings")
        ChatUtils.sendSystemMessage("/rec delete <name> - Delete a recording")
        ChatUtils.sendSystemMessage("/rec status - Show current status")
    }

    @SubCommand("start")
    fun startRecording(name: String = "recording_${System.currentTimeMillis()}") {
        if (RecordingManager.isRecording) {
            ChatUtils.sendSystemMessage("Already recording! Use /rec stop first.")
            return
        }
        if (PlaybackExecutor.isPlaying) {
            ChatUtils.sendSystemMessage("Cannot record while playing!")
            return
        }
        Recorder.startRecording(name)
    }

    @SubCommand("stop")
    fun stopRecording() {
        if (PlaybackExecutor.isPlaying) {
            PlaybackExecutor.stopPlayback()
            return
        }
        if (RecordingManager.isRecording) {
            val recording = Recorder.stopRecording()
            if (recording != null) {
                RecordingManager.saveRecording(recording)
            }
            return
        }
        ChatUtils.sendSystemMessage("Nothing to stop")
    }

    @SubCommand("save")
    fun saveRecording() {
        val recording = RecordingManager.currentRecording
        if (recording == null) {
            ChatUtils.sendSystemMessage("No recording to save")
            return
        }
        RecordingManager.saveRecording(recording)
    }

    @SubCommand("play")
    fun playRecording(name: String) {
        if (RecordingManager.isRecording) {
            ChatUtils.sendSystemMessage("Cannot play while recording!")
            return
        }
        val recording = RecordingManager.loadRecording(name)
        if (recording != null) {
            PlaybackExecutor.startPlayback(recording, 1)
        }
    }

    @SubCommand("loop")
    fun loopRecording(name: String, count: Int = 1) {
        if (RecordingManager.isRecording) {
            ChatUtils.sendSystemMessage("Cannot play while recording!")
            return
        }
        val recording = RecordingManager.loadRecording(name)
        if (recording != null) {
            PlaybackExecutor.startPlayback(recording, count)
        }
    }

    @SubCommand("list")
    fun listRecordings() {
        val recordings = RecordingManager.listRecordings()
        if (recordings.isEmpty()) {
            ChatUtils.sendSystemMessage("No recordings found")
            return
        }
        ChatUtils.sendSystemMessage("=== Recordings (${recordings.size}) ===")
        recordings.forEach { name ->
            ChatUtils.sendSystemMessage("  - $name")
        }
    }

    @SubCommand("delete")
    fun deleteRecording(name: String) {
        RecordingManager.deleteRecording(name)
    }

    @SubCommand("status")
    fun showStatus() {
        when {
            RecordingManager.isRecording -> {
                val recording = RecordingManager.currentRecording
                ChatUtils.sendSystemMessage("Recording: ${recording?.name} | Frames: ${recording?.frameCount}")
            }
            PlaybackExecutor.isPlaying -> {
                ChatUtils.sendSystemMessage(PlaybackExecutor.getStatus())
            }
            else -> {
                ChatUtils.sendSystemMessage("Idle - not recording or playing")
            }
        }
    }
}
