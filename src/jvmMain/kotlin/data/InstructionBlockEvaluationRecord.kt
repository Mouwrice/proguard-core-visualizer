package data

/**
 * Track the evaluation of a single instruction block, starting at some offset in the code
 */
data class InstructionBlockEvaluationRecord(
    /**
     * List of instruction evaluation trackers.
     */
    val evaluations: List<InstructionEvaluationRecord?>?,
    /**
     * Exception handler info. If present, this instructionBlock regards an exception handler
     */
    val exceptionHandlerInfo: ExceptionHandlerRecord?,
    /**
     * Variables found at the start of the block evaluation.
     */
    val startVariables: List<String>,
    /**
     * Stack found at the start of the block evaluation.
     */
    val startStack: List<String>,
    /**
     * Start instruction offset of this block evaluation.
     */
    val startOffset: Int,
    /**
     * Current branch evaluation stack
     */
    val branchEvaluationStack: List<BranchTargetRecord>,
)
