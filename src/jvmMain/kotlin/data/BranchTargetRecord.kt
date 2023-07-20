package data

data class BranchTargetRecord(
    /**
     * Variables at the start of the block evaluation
     */
    val startVariables: List<String>,
    /**
     * Stack at the start of the block evaluation
     */
    val startStack: List<String>,
    /**
     * Instruction offset of the first instruction of the block
     */
    val startOffset: Int,
)
