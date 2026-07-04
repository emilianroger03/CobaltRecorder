package dev.recorder.playback

import net.minecraft.world.InteractionHand
import org.cobalt.Cobalt.minecraft
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.module.impl.misc.Rotations
import org.cobalt.util.ChatUtils
import org.cobalt.util.InventoryUtils
import org.cobalt.util.KeybindUtils
import org.cobalt.util.PlayerUtils
import org.cobalt.util.rotation.Rotation
import dev.recorder.recording.Recording
import dev.recorder.recording.RecordedFrame
import dev.recorder.recording.ActionType
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.asin

object PlaybackExecutor {

    var isPlaying = false
        private set

    private var currentRecording: Recording? = null
    private var currentFrameIndex = 0
    private var loopsRemaining = 0
    private var totalLoops = 0

    private const val ETHERWARP_RANGE = 57.0
    private const val ETHERWARP_THRESHOLD = 3.0  // Distance to trigger etherwarp vs walk

    init {
        EventBus.register(this)
    }

    fun startPlayback(recording: Recording, loops: Int = 1) {
        if (isPlaying) {
            ChatUtils.sendSystemMessage("Already playing! Use /rec stop first.")
            return
        }

        currentRecording = recording
        currentFrameIndex = 0
        loopsRemaining = loops
        totalLoops = loops
        isPlaying = true

        ChatUtils.sendSystemMessage("Starting playback: ${recording.name} (${loops} loops)")

        // Teleport to start position if available
        recording.startPosition?.let { pos ->
            val player = minecraft.player ?: return
            val dist = distanceTo(pos.x, pos.y, pos.z)
            if (dist > ETHERWARP_THRESHOLD) {
                ChatUtils.sendSystemMessage("Etherwarping to start position...")
                etherwarpTo(pos.x, pos.y, pos.z)
            }
        }
    }

    fun stopPlayback() {
        isPlaying = false
        currentRecording = null
        currentFrameIndex = 0
        loopsRemaining = 0

        // Release all keys
        KeybindUtils.stopMovement()
        KeybindUtils.setKeyState(minecraft.options.keyAttack, false)
        KeybindUtils.setKeyState(minecraft.options.keyUse, false)

        Rotations.stop()
        ChatUtils.sendSystemMessage("Playback stopped")
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.Start) {
        if (!isPlaying) return
        if (minecraft.player == null || minecraft.level == null) {
            stopPlayback()
            return
        }
        if (minecraft.screen != null) return  // Pause if GUI open

        val recording = currentRecording ?: return

        if (currentFrameIndex >= recording.frames.size) {
            // Loop or stop
            loopsRemaining--
            if (loopsRemaining > 0) {
                currentFrameIndex = 0
                ChatUtils.sendSystemMessage("Loop ${totalLoops - loopsRemaining + 1}/$totalLoops")
                // Return to start
                recording.startPosition?.let { etherwarpTo(it.x, it.y, it.z) }
            } else {
                ChatUtils.sendSystemMessage("Playback complete!")
                stopPlayback()
            }
            return
        }

        val frame = recording.frames[currentFrameIndex]
        executeFrame(frame)
        currentFrameIndex++
    }

    private fun executeFrame(frame: RecordedFrame) {
        val player = minecraft.player ?: return

        // Handle position - etherwarp if far, otherwise let movement handle it
        val dist = distanceTo(frame.x, frame.y, frame.z)
        if (dist > ETHERWARP_THRESHOLD) {
            etherwarpTo(frame.x, frame.y, frame.z)
        }

        // Handle rotation
        val targetRotation = Rotation(frame.yaw, frame.pitch)
        if (!Rotations.running) {
            Rotations.start(targetRotation)
        }

        // Handle hotbar slot
        if (player.inventory.selected != frame.selectedSlot) {
            InventoryUtils.selectHotbarSlot(frame.selectedSlot)
        }

        // Handle movement keys
        KeybindUtils.setKeyState(minecraft.options.keyShift, frame.sneaking)
        KeybindUtils.setKeyState(minecraft.options.keySprint, frame.sprinting)

        // Handle clicks
        KeybindUtils.setKeyState(minecraft.options.keyAttack, frame.leftClick)
        KeybindUtils.setKeyState(minecraft.options.keyUse, frame.rightClick)

        // Handle inventory actions
        frame.inventoryAction?.let { action ->
            when (action.type) {
                ActionType.CLICK_SLOT -> InventoryUtils.clickSlot(action.slot)
                ActionType.SWAP_HOTBAR -> InventoryUtils.selectHotbarSlot(action.slot)
                ActionType.DROP_ITEM -> player.drop(false)
                ActionType.ARMOR_EQUIP -> {
                    // Find and equip armor by name
                    val slot = InventoryUtils.findItemInInventory(action.itemName)
                    if (slot != -1) {
                        InventoryUtils.clickSlot(slot)
                    }
                }
            }
        }
    }

    private fun etherwarpTo(x: Double, y: Double, z: Double) {
        val player = minecraft.player ?: return

        // Calculate rotation to target
        val targetRotation = getRotationTo(player.x, player.y + player.eyeHeight, player.z, x, y, z)
        PlayerUtils.setRotation(targetRotation)

        // Shift+right-click for etherwarp
        KeybindUtils.setKeyState(minecraft.options.keyShift, true)
        KeybindUtils.rightClick()

        // Will release shift on next frame based on recording
    }

    private fun getRotationTo(fromX: Double, fromY: Double, fromZ: Double, toX: Double, toY: Double, toZ: Double): Rotation {
        val dx = toX - fromX
        val dy = toY - fromY
        val dz = toZ - fromZ
        val dist = sqrt(dx * dx + dz * dz)

        val yaw = (-atan2(dx, dz) * 180.0 / Math.PI).toFloat()
        val pitch = (-atan2(dy, dist) * 180.0 / Math.PI).toFloat()

        return Rotation(yaw, pitch)
    }

    private fun distanceTo(x: Double, y: Double, z: Double): Double {
        val player = minecraft.player ?: return Double.MAX_VALUE
        val dx = player.x - x
        val dy = player.y - y
        val dz = player.z - z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun getStatus(): String {
        if (!isPlaying) return "Not playing"
        val recording = currentRecording ?: return "No recording"
        return "Playing: ${recording.name} | Frame: $currentFrameIndex/${recording.frameCount} | Loop: ${totalLoops - loopsRemaining + 1}/$totalLoops"
    }
}
