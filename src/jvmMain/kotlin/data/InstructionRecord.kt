package data

/**
 * DTO to track a single instruction
 */
data class InstructionRecord(
    /**
     * The offset of the instruction
     */
    val offset: Int,
    /**
     * String representation of the instruction
     */
    val instruction: String,
)
