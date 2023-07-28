package viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.CodeAttributeRecord

/**
 * A model to store and interact with a single [CodeAttributeRecord].
 */
class CodeAttributeViewModel(val codeAttribute: CodeAttributeRecord) {

    var instructionIndex by mutableStateOf(0)

    val instruction by derivedStateOf { codeAttribute.instructions.getOrNull(instructionIndex) }

    private var evaluationBlockIndex by mutableStateOf(0)

    fun updateEvaluationBlockIndex(value: Int) {
        evaluationBlockIndex = value
        evaluationIndex = 0
    }

    val evaluationBlock by derivedStateOf { codeAttribute.blockEvaluations.getOrNull(evaluationBlockIndex) }

    var evaluationIndex by mutableStateOf(0)

    val evaluation by derivedStateOf { evaluationBlock?.evaluations?.getOrNull(evaluationIndex) }

    // Whether to show the final results of the evaluation for each instruction.
    var display by mutableStateOf(Display.EVALUATIONS)

    // region Previous and next
    val hasPrevious by derivedStateOf {
        when (display) {
            Display.EVALUATIONS -> evaluationBlockIndex > 0 || evaluationIndex > 0
            Display.RESULTS -> instructionIndex > 0
        }
    }

    val hasNext by derivedStateOf {
        when (display) {
            Display.EVALUATIONS -> {
                val blockEvaluations = codeAttribute.blockEvaluations
                val blockEvaluation = blockEvaluations.getOrNull(evaluationBlockIndex)
                blockEvaluation != null && (
                    evaluationBlockIndex < blockEvaluations.size - 1 ||
                        evaluationIndex < blockEvaluation.evaluations.size - 1
                    )
            }

            Display.RESULTS -> instructionIndex < codeAttribute.instructions.size - 1
        }
    }

    private fun previousInstruction() {
        if (instructionIndex > 0) {
            instructionIndex--
        }
    }

    private fun nextInstruction() {
        if (instructionIndex < codeAttribute.instructions.size - 1) {
            instructionIndex++
        }
    }

    private fun previousEvaluation() {
        if (evaluationIndex > 0) {
            // There are more evaluations in the current block, go to the previous one.
            evaluationIndex--
        } else if (evaluationBlockIndex > 0) {
            // There are no more evaluations in the current block, go to the previous block.
            evaluationBlockIndex--
            evaluationIndex = (evaluationBlock?.evaluations?.size ?: 1) - 1
        }
    }

    private fun nextEvaluation() {
        val blockEvaluations = codeAttribute.blockEvaluations
        val evaluations = blockEvaluations.getOrNull(evaluationBlockIndex)?.evaluations
        if (evaluations != null) {
            if (evaluationIndex < evaluations.size - 1) {
                // There are more evaluations in the current block, go to the next one.
                evaluationIndex++
            } else if (evaluationBlockIndex < blockEvaluations.size - 1) {
                // There are no more evaluations in the current block, go to the next block.
                evaluationBlockIndex++
                evaluationIndex = 0
            }
        }
    }

    fun next() {
        when (display) {
            Display.EVALUATIONS -> nextEvaluation()
            Display.RESULTS -> nextInstruction()
        }
    }

    fun previous() {
        when (display) {
            Display.EVALUATIONS -> previousEvaluation()
            Display.RESULTS -> previousInstruction()
        }
    }
    // endregion

    fun switchDisplay() {
        display = when (display) {
            Display.EVALUATIONS -> Display.RESULTS
            Display.RESULTS -> Display.EVALUATIONS
        }
    }
}
