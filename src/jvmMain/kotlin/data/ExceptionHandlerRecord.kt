package data

/**
 * DTO for exception handling info, when a blockEvaluation has this,
 * the block regard the evaluation of an exception handler
 */
class ExceptionHandlerRecord(
    /**
     * Instruction offset from where the handler starts catching
     */
    val catchStartOffset: Int,
    /**
     * Instruction offset from where the handler stops catching
     */
    val catchEndOffset: Int,
    /**
     * Instruction offset of the exception handling code
     */
    val handlerStartOffset: Int,

    /**
     * What type the handler catches
     */
    val catchType: String,
)
