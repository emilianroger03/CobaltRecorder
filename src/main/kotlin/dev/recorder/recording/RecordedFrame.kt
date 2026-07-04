package dev.recorder.recording

import kotlinx.serialization.Serializable

@Serializable
data class RecordedFrame(
    val tick: Int,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val selectedSlot: Int,
    val sneaking: Boolean,
    val sprinting: Boolean,
    val leftClick: Boolean,
    val rightClick: Boolean,
    val inventoryAction: InventoryAction? = null,
    val armorSlots: List<String> = emptyList()  // Item names in armor slots
)

@Serializable
data class InventoryAction(
    val type: ActionType,
    val slot: Int,
    val itemName: String = ""
)

@Serializable
enum class ActionType {
    CLICK_SLOT,
    SWAP_HOTBAR,
    DROP_ITEM,
    ARMOR_EQUIP
}
