package data

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.FileReader

class StateTracker {

    /**
     * Track the evaluation of a single code attribute (one call to visitCode attribute)
     */
    class CodeAttributeTracker(
        /**
         * Clazz this code attribute is a part of.
         */
        var clazz: String,
        /**
         * Method this code attribute is from.
         */
        var method: String,
        /**
         * List of parameters given to the code attribute.
         */
        var parameters: List<String>,
    ) {
        class ErrorTracker(
            /**
             * Clazz this code attribute is a part of.
             */
            var clazz: String,
            /**
             * Method this code attribute is from.
             */
            var method: String,
            /**
             * Ths instruction offset of the instruction that caused the exception.
             */
            var instructionOffset: Int,
            /**
             * The message of the exception.
             */
            var message: String,
        )

        /**
         * Track a single instruction block. Used for tracking the instructionBlock stack generated
         * when using branches
         */
        class InstructionBlock(var variables: List<String>, var stack: List<String>, var startOffset: Int)

        /**
         * DTO for exception handling info, when a blockEvaluation has this,
         * the block regard the evaluation of an exception handler
         */
        class ExceptionHandlerInfo(var catchStartOffset: Int, var catchEndOffset: Int, var catchType: String)

        /**
         * DTO to track a single instruction
         */
        class InstructionTracker(var offset: Int, var instruction: String)

        /**
         * Track the evaluation of a single instruction block, starting at some offset in the code
         */
        class BlockEvaluationTracker(
            /**
             * Variables found at the start of the block evaluation.
             */
            var startVariables: List<String>,
            /**
             * Stack found at the start of the block evaluation.
             */
            var startStack: List<String>,
            /**
             * Start instruction offset of this block evaluation.
             */
            var startOffset: Int,
        ) {
            /**
             * Track information about the evaluation of a single instruction.
             */
            class InstructionEvaluationTracker(
                /**
                 * Has the instrcution been seen in a given context before.
                 * When true, the instrcutionBlock evaluation comes to an end
                 */
                var isSeenBefore: Boolean?,
                /**
                 * Whether the instruction has been seen a lot, if true, start generalizing the values
                 */
                var isGeneralization: Boolean?,
                /**
                 * If we generalized, we remind how much times you saw the instruction.
                 */
                var timesSeen: Int?,
                /**
                 * String representation of an instruction.
                 */
                var instruction: String?,
                /**
                 * Offset of the instruction within the code
                 */
                var instructionOffset: Int?,
                /**
                 * Current stack of instruction blocks that need to be evaluated, used for branches
                 */
                var updatedEvaluationStack: List<InstructionBlock>?,
                /**
                 * Content of the variables before the instruction.
                 */
                var variablesBefore: List<String>?,
                /**
                 * Content of the stack before the instruction.
                 */
                var stackBefore: List<String>?,
            ) {
                var jsrBlockEvalTracker: List<BlockEvaluationTracker>? = null
            }

            /**
             * List of instruction evaluation trackers.
             */
            var evaluations: List<InstructionEvaluationTracker> = ArrayList()

            /**
             * Exception handler info. If present, this instructionBlock regards an exception handler
             */
            var exceptionHandlerInfo: ExceptionHandlerInfo? = null

            /**
             * Current block evaluation stack. Not encoded if not changed.
             */
            var blockEvaluationStack: List<InstructionBlock> = ArrayList()
        }

        /**
         * List of instruction from this code attribute.
         */
        var instructions: List<InstructionTracker> = ArrayList()
        var error: ErrorTracker? = null

        /**
         * List of block evaluations that happened on this code attribute.
         */
        var blockEvaluations: List<BlockEvaluationTracker> = ArrayList()
    }

    val codeAttributes: List<CodeAttributeTracker> = ArrayList()

    companion object {
        /**
         * Tries to parse the json file at the given path
         * into a StateTracker.
         */
        fun fromJson(path: String): StateTracker {
            return Gson().fromJson(BufferedReader(FileReader(path)), StateTracker::class.java)
        }
    }
}
