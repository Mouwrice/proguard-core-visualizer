package data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

class DebuggerViewModel {
    var file by mutableStateOf<File?>(null)
        private set

    var codeAttributes by mutableStateOf<List<StateTracker.CodeAttributeTracker>>(emptyList())
        private set

    var currentInstruction by mutableStateOf("")
        private set

    var evaluation by mutableStateOf<StateTracker.CodeAttributeTracker.BlockEvaluationTracker.InstructionEvaluationTracker?>(
        null,
    )
        private set

    var currentInstructionOffset by mutableStateOf(0)
        private set

    var currentCodeAttribute by mutableStateOf(0)
        private set

    var currentBlockEvaluationStack by mutableStateOf<List<StateTracker.CodeAttributeTracker.InstructionBlock>>(
        emptyList(),
    )
        private set

    private var stateTracker: StateTracker? = null
    private var currentBlockEvaluation: Int = 0
    private var currentEvaluation: Int = 0

    fun nextEvaluation() {
        val codeAttributes = stateTracker?.codeAttributes
        val blockEvaluations = codeAttributes?.get(currentCodeAttribute)?.blockEvaluations
        val evaluations = blockEvaluations?.get(currentBlockEvaluation)?.evaluations
        if (currentEvaluation < (
                (
                    evaluations?.size?.minus(1)
                    )
                    ?: 0
                )
        ) {
            currentEvaluation++
        } else if (currentBlockEvaluation < (
                (
                    blockEvaluations?.size?.minus(1)
                    ) ?: 0
                )
        ) {
            currentBlockEvaluation++
            currentEvaluation = 0
        } else if (currentCodeAttribute < ((codeAttributes?.size?.minus(1)) ?: 0)) {
            currentCodeAttribute++
            currentBlockEvaluation = 0
            currentEvaluation = 0
        }
        update()
    }

    fun previousEvaluation() {
        val codeAttributes = stateTracker?.codeAttributes
        val blockEvaluations = codeAttributes?.get(currentCodeAttribute)?.blockEvaluations
        val evaluations = blockEvaluations?.get(currentBlockEvaluation)?.evaluations
        if (currentEvaluation > 0) {
            currentEvaluation--
        } else if (currentBlockEvaluation > 0) {
            currentBlockEvaluation--
            currentEvaluation = (
                codeAttributes?.get(currentCodeAttribute)?.blockEvaluations?.get(
                    currentBlockEvaluation,
                )?.evaluations?.size?.minus(1) ?: 0
                )
        } else if (currentCodeAttribute > 0) {
            currentCodeAttribute--
            currentBlockEvaluation =
                (
                    blockEvaluations?.size?.minus(1)
                        ?: 0
                    )
            currentEvaluation = (
                evaluations?.size?.minus(1) ?: 0
                )
        }
        update()
    }

    private fun update() {
        val blockEvaluations = stateTracker?.codeAttributes?.get(currentCodeAttribute)?.blockEvaluations
        val blockEvaluation = blockEvaluations?.get(currentBlockEvaluation)
        evaluation = blockEvaluation?.evaluations?.get(currentEvaluation)

        currentBlockEvaluationStack = blockEvaluation?.blockEvaluationStack ?: emptyList()
        currentInstruction = evaluation?.instruction ?: ""
        currentInstructionOffset = evaluation?.instructionOffset ?: 0
    }

    /**
     * Loads the json file at the given path into the view model.
     */
    fun loadJson(path: String) {
        reset()
        file = File(path)
        try {
            stateTracker = StateTracker.fromJson(path)
        } catch (e: Exception) {
            println("Error while parsing json file: $e")
            return
        }
        codeAttributes = stateTracker?.codeAttributes ?: emptyList()
        update()
    }

    fun reset() {
        currentCodeAttribute = 0
        currentBlockEvaluation = 0
        currentBlockEvaluationStack = emptyList()
        currentEvaluation = 0
        currentInstruction = ""
        currentInstructionOffset = 0
        evaluation = null
        codeAttributes = emptyList()
        stateTracker = null
        file = null
    }
}
