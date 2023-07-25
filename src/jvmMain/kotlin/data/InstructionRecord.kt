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

    /**
     * Contains the final result computations from the partial evaluator regarding the variables of this instruction.
     */
    val finalVariablesBefore: List<String>?,
    /**
     * Contains the final result computations from the partial evaluator regarding the stack of this instruction.
     */
    val finalStackBefore: List<String>?,
    /**
     * Contains the final result computations from the partial evaluator regarding the target instructions of this instruction.
     */
    val finalTargetInstructions: List<Int>?,
    /**
     * Contains the final result computations from the partial evaluator regarding the source instructions of this instruction.
     */
    val finalOriginInstructions: List<Int>?,

)
