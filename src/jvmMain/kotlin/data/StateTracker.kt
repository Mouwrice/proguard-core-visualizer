package data

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.FileReader

class StateTracker {

    class CodeAttributeTracker(var clazz: String, var method: String, var parameters: List<String>) {
        class InstructionBlock(var variables: List<String>, var stack: List<String>, var startOffset: Int)
        class ExceptionHandlerInfo(var catchStartOffset: Int, var catchEndOffset: Int, var catchType: String)
        class InstructionTracker(var offset: Int, var instruction: String)
        class BlockEvaluationTracker(
            var startVariables: List<String>,
            var startStack: List<String>,
            var startOffset: Int,
        ) {
            class InstructionEvaluationTracker(
                var isSeenBefore: Boolean?,
                var isGeneralization: Boolean?,
                var timesSeen: Int?,
                var instruction: String?,
                var instructionOffset: Int?,
                var updatedEvaluationStack: List<InstructionBlock>?,
                var variablesBefore: List<String>?,
                var stackBefore: List<String>?,
            )
            var evaluations: List<InstructionEvaluationTracker> = ArrayList()
            var exceptionHandlerInfo: ExceptionHandlerInfo? = null

            val lastEvaluation: InstructionEvaluationTracker?
                get() = if (evaluations.isEmpty()) {
                    null
                } else {
                    evaluations.get(evaluations.size - 1)
                }
        }

        var instructions: List<InstructionTracker> = ArrayList()
        var blockEvaluations: List<BlockEvaluationTracker> = ArrayList()
        var lastEvaluationStack: List<InstructionBlock> = ArrayList()

        val lastBlockEvaluation: BlockEvaluationTracker?
            get() {
                return if (blockEvaluations.isEmpty()) {
                    null
                } else {
                    blockEvaluations.get(blockEvaluations.size - 1)
                }
            }
    }

    class ErrorTracker(
        var clazz: String,
        var method: String,
        var instructionOffset: Int,
        var message: String,
        var cause: String,
    )

    val codeAttributes: List<CodeAttributeTracker> = ArrayList()
    var error: ErrorTracker? = null

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
