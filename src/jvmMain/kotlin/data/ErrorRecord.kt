package data

data class ErrorRecord(
    /**
     * Ths instruction offset of the instruction that caused the exception.
     */
    val instructionOffset: Int,
    /**
     * The message of the exception.
     */
    val message: String,
)
