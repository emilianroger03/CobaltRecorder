package dev.recorder.recording

import net.minecraft.world.entity.EquipmentSlot
import org.cobalt.Cobalt.minecraft
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.util.ChatUtils

object Recorder {

    private var lastSelectedSlot = -1
    private var lastArmorState: List<String> = emptyList()
    private var wasLeftClicking = false
    private var wasRightClicking = false

    init {
        EventBus.register(this)
    }

    fun startRecording(name: String) {
        val player = minecraft.player
        if (player == null) {
            ChatUtils.sendSystemMessage("Cannot record: not in game")
            return
        }

        val startPos = Position(player.x, player.y, player.z)
        lastSelectedSlot = player.inventory.selected
        lastArmorState = getArmorState()
        wasLeftClicking = false
        wasRightClicking = false

        RecordingManager.startRecording(name, startPos)
    }

    fun stopRecording(): Recording? {
        return RecordingManager.stopRecording()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.Start) {
        if (!RecordingManager.isRecording) return

        val player = minecraft.player ?: return

        // Detect inventory changes
        var inventoryAction: InventoryAction? = null

        // Check hotbar slot change
        val currentSlot = player.inventory.selected
        if (currentSlot != lastSelectedSlot) {
            inventoryAction = InventoryAction(
                type = ActionType.SWAP_HOTBAR,
                slot = currentSlot,
                itemName = player.inventory.getItem(currentSlot).hoverName.string
            )
            lastSelectedSlot = currentSlot
        }

        // Check armor changes
        val currentArmor = getArmorState()
        if (currentArmor != lastArmorState) {
            // Find which slot changed
            for (i in currentArmor.indices) {
                if (i < lastArmorState.size && currentArmor[i] != lastArmorState[i]) {
                    inventoryAction = InventoryAction(
                        type = ActionType.ARMOR_EQUIP,
                        slot = i,
                        itemName = currentArmor[i]
                    )
                    break
                }
            }
            lastArmorState = currentArmor
        }

        // Get click states
        val leftClicking = minecraft.options.keyAttack.isDown
        val rightClicking = minecraft.options.keyUse.isDown

        // Record frame
        val frame = RecordedFrame(
            tick = 0,  // Will be set by RecordingManager
            x = player.x,
            y = player.y,
            z = player.z,
            yaw = player.yRot,
            pitch = player.xRot,
            selectedSlot = currentSlot,
            sneaking = player.isShiftKeyDown,
            sprinting = player.isSprinting,
            leftClick = leftClicking,
            rightClick = rightClicking,
            inventoryAction = inventoryAction,
            armorSlots = currentArmor
        )

        RecordingManager.addFrame(frame)

        wasLeftClicking = leftClicking
        wasRightClicking = rightClicking
    }

    private fun getArmorState(): List<String> {
        val player = minecraft.player ?: return emptyList()
        return listOf(
            player.getItemBySlot(EquipmentSlot.HEAD).hoverName.string,
            player.getItemBySlot(EquipmentSlot.CHEST).hoverName.string,
            player.getItemBySlot(EquipmentSlot.LEGS).hoverName.string,
            player.getItemBySlot(EquipmentSlot.FEET).hoverName.string
        )
    }
}
