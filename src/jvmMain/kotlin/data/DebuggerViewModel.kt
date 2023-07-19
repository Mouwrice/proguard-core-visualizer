package data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

class DebuggerViewModel {
    var file by mutableStateOf<File?>(null)
        private set

    var stateTracker by mutableStateOf<StateTracker?>(null)
        private set

    var currentInstruction by mutableStateOf("")
        private set

    var currentInstructionOffset by mutableStateOf(0)
        private set

    var variables by mutableStateOf<List<String>>(emptyList())
        private set

    var stack by mutableStateOf<List<String>>(emptyList())
        private set

    var currentCodeAttribute by mutableStateOf(0)
        private set

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
        val evaluation = stateTracker?.codeAttributes?.get(currentCodeAttribute)?.blockEvaluations?.get(
            currentBlockEvaluation,
        )?.evaluations?.get(currentEvaluation)
        currentInstruction = evaluation?.instruction ?: ""
        currentInstructionOffset = evaluation?.instructionOffset ?: 0

        variables = evaluation?.variablesBefore ?: emptyList()

        stack = evaluation?.stackBefore ?: emptyList()
    }

    /**
     * Loads the json file at the given path into the view model.
     */
    fun loadJson(path: String) {
        file = File(path)
        stateTracker = StateTracker.fromJson(path)
        update()
    }

    fun reset() {
        currentCodeAttribute = 0
        currentBlockEvaluation = 0
        currentEvaluation = 0
        file = null
        stateTracker = null
        currentInstruction = ""
        currentInstructionOffset = 0
        variables = emptyList()
        stack = emptyList()
    }
}
