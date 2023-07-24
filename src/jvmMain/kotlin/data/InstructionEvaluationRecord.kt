package data

/**
 * Track information about the evaluation of a single instruction.
 */
data class InstructionEvaluationRecord(
    /**
     * Has the instruction been seen in a given context before.
     * When true, the instructionBlock evaluation comes to an end
     */
    val skipEvaluation: Boolean,
    /**
     * Whether the instruction has been seen a lot, if true, start generalizing the values
     */
    val isGeneralization: Boolean,
    /**
     * If we generalized, we remind how much times you saw the instruction.
     */
    val evaluationCount: Int,

    /**
     * String representation of an instruction.
     */
    val instruction: String,

    /**
     * Offset of the instruction within the code
     */
    val instructionOffset: Int,

    /**
     * Current stack of instruction blocks that need to be evaluated, used for branches,
     * only given when the instruction alters the branch evaluation stack
     */
    val updatedEvaluationStack: List<BranchTargetRecord>?,

    /**
     * Content of the variables before the instruction.
     */
    val variablesBefore: List<String>,

    /**
     * Content of the stack before the instruction.
     */
    val stackBefore: List<String>,
    val jsrBlockEvaluations: List<InstructionBlockEvaluationRecord>?,
)
