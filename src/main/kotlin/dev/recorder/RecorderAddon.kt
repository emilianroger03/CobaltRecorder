package dev.recorder

import org.cobalt.addon.Addon
import org.cobalt.command.CommandManager
import org.cobalt.util.ChatUtils
import dev.recorder.command.RecorderCommand
import dev.recorder.recording.Recorder
import dev.recorder.playback.PlaybackExecutor

class RecorderAddon : Addon {

    override fun onLoad() {
        // Initialize components (they register themselves to EventBus in init blocks)
        Recorder
        PlaybackExecutor

        // Register command
        CommandManager.register(RecorderCommand())

        ChatUtils.sendSystemMessage("Cobalt Recorder loaded!")
        ChatUtils.sendSystemMessage("Use /rec for commands")
    }

    override fun onUnload() {
        // Stop any active recording/playback
        if (dev.recorder.recording.RecordingManager.isRecording) {
            Recorder.stopRecording()
        }
        if (PlaybackExecutor.isPlaying) {
            PlaybackExecutor.stopPlayback()
        }

        ChatUtils.sendSystemMessage("Cobalt Recorder unloaded")
    }
}
