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

    var variables by mutableStateOf("")
        private set

    var stack by mutableStateOf("")
        private set

    var currentCodeAttribute by mutableStateOf(0)
        private set

    private var currentBlockEvaluation: Int = 0
    private var currentEvaluation: Int = 0

    fun nextEvaluation() {
        if (currentEvaluation < (
                (
                    stateTracker?.codeAttributes?.get(currentCodeAttribute)?.blockEvaluations?.get(
                        currentBlockEvaluation,
                    )?.evaluations?.size?.minus(1)
                    ) ?: 0
                )
        ) {
            currentEvaluation++
        } else if (currentBlockEvaluation < (
                (
                    stateTracker?.codeAttributes?.get(
                        currentCodeAttribute,
                    )?.blockEvaluations?.size?.minus(1)
                    ) ?: 0
                )
        ) {
            currentBlockEvaluation++
            currentEvaluation = 0
        } else if (currentCodeAttribute < ((stateTracker?.codeAttributes?.size?.minus(1)) ?: 0)) {
            currentCodeAttribute++
            currentBlockEvaluation = 0
            currentEvaluation = 0
        }
        update()
    }

    fun previousEvaluation() {
        if (currentEvaluation > 0) {
            currentEvaluation--
        } else if (currentBlockEvaluation > 0) {
            currentBlockEvaluation--
            currentEvaluation = (
                stateTracker?.codeAttributes?.get(currentCodeAttribute)?.blockEvaluations?.get(
                    currentBlockEvaluation,
                )?.evaluations?.size?.minus(1) ?: 0
                )
        } else if (currentCodeAttribute > 0) {
            currentCodeAttribute--
            currentBlockEvaluation =
                (
                    stateTracker?.codeAttributes?.get(currentCodeAttribute)?.blockEvaluations?.size?.minus(1)
                        ?: 0
                    )
            currentEvaluation = (
                stateTracker?.codeAttributes?.get(currentCodeAttribute)?.blockEvaluations?.get(
                    currentBlockEvaluation,
                )?.evaluations?.size?.minus(1) ?: 0
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

        variables = evaluation?.variablesBefore.toString()

        stack = evaluation?.stackBefore.toString()
    }

    /**
     * Loads the json file at the given path into the view model.
     */
    fun loadJson(path: String) {
        file = File(path)
        stateTracker = StateTracker.fromJson(path)
        update()
    }
}
