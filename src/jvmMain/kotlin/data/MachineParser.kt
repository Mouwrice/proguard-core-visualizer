package data

import com.google.gson.GsonBuilder
import java.nio.file.Files

class MachineParser {
    internal class StateTracker {
        internal class CodeAttributeTracker(var clazz: String, var method: String, var parameters: String) {
            internal class InstructionBlock(var variables: String, var stack: String, var startOffset: Int)
            internal class InstructionTracker(var offset: Int, var instruction: String)
            internal class BlockEvaluationTracker(
                var startVariables: String,
                var startStack: String,
                var startOffset: Int,
            ) {
                internal class InstructionEvaluationTracker(
                    var isSeenBefore: Boolean?,
                    var isGeneralization: Boolean?,
                    timesSeen: Int?,
                    var instruction: String?,
                    var instructionOffset: Int?,
                    var updatedEvaluationStack: List<InstructionBlock>?,
                    var variablesBefore: String?,
                    var stackBefore: String?,
                ) {
                    var timesSeen: Int? = null

                    companion object {
                        fun seenIndicator(): InstructionEvaluationTracker {
                            return InstructionEvaluationTracker(
                                true,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                            )
                        }

                        fun generalizationIndicator(timesSeen: Int): InstructionEvaluationTracker {
                            return InstructionEvaluationTracker(
                                null,
                                true,
                                timesSeen,
                                null,
                                null,
                                null,
                                null,
                                null,
                            )
                        }

                        fun instructionTracker(
                            instruction: String?,
                            instructionOffset: Int,
                            evaluationBlockStack: List<InstructionBlock>?,
                            variablesBefore: String?,
                            stackBefore: String?,
                        ): InstructionEvaluationTracker {
                            return InstructionEvaluationTracker(
                                null,
                                null,
                                null,
                                instruction,
                                instructionOffset,
                                evaluationBlockStack,
                                variablesBefore,
                                stackBefore,
                            )
                        }
                    }
                }

                var evaluations: List<InstructionEvaluationTracker> = ArrayList()

                val lastEvaluation: InstructionEvaluationTracker?
                    get() = if (evaluations.isEmpty()) {
                        null
                    } else {
                        evaluations.get(evaluations.size - 1)
                    }
            }

            var instructions: List<InstructionTracker> = ArrayList()
            var blockEvaluations: List<BlockEvaluationTracker> = ArrayList()

            val lastBlockEvaluation: BlockEvaluationTracker?
                get() {
                    return if (blockEvaluations.isEmpty()) {
                        null
                    } else {
                        blockEvaluations.get(blockEvaluations.size - 1)
                    }
                }
        }

        internal class ErrorTracker(
            var clazz: String,
            var method: String,
            var instructionOffset: Int,
            var message: String,
            var cause: String,
        )

        val codeAttributes: List<CodeAttributeTracker> = ArrayList()
        var error: ErrorTracker? = null
        val lastCodeAttribute: CodeAttributeTracker?
            get() {
                return if (codeAttributes.isEmpty()) {
                    null
                } else {
                    codeAttributes.get(codeAttributes.size - 1)
                }
            }
    }

    private val gson = GsonBuilder().setPrettyPrinting().create()

    public fun parsejson() {
        val tracker = gson.fromJson(Files.readString(java.nio.file.Path.of("pe-state.json")), StateTracker::class.java)
        print(tracker.lastCodeAttribute?.clazz)
    }
}
