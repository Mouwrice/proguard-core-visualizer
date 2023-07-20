package data

/**
 * Track the evaluation of a single code attribute (one call to visitCode attribute)
 */
data class CodeAttributeRecord(
    /**
     * Clazz this code attribute is a part of.
     */
    val clazz: String,

    /**
     * Method this code attribute is from.
     */
    val method: String,
    /**
     * List of instruction from this code attribute.
     */
    val instructions: List<InstructionRecord>,
    /**
     * List of parameters given to the code attribute.
     */
    val parameters: List<String>,
    val error: ErrorRecord?,
    /**
     * List of block evaluations that happened on this code attribute.
     */
    val blockEvaluations: List<InstructionBlockEvaluationRecord>,
)
