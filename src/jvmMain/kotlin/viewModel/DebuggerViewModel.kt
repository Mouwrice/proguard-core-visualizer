package viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.BranchTargetRecord
import data.CodeAttributeRecord
import data.InstructionEvaluationRecord
import data.StateTracker
import java.io.File

class DebuggerViewModel {
    /**
     * The file that is currently loaded.
     */
    var file by mutableStateOf<File?>(null)
        private set

    /**
     * All the code attributes parsed from the json file.
     */
    var codeAttributes by mutableStateOf<List<CodeAttributeRecord>>(emptyList())
        private set

    /**
     * The current instruction that is being evaluated.
     */
    var evaluation by mutableStateOf<InstructionEvaluationRecord?>(
        null,
    )
        private set

    var currentCodeAttribute by mutableStateOf(0)
        private set

    var currentBlockEvaluationStack by mutableStateOf<List<BranchTargetRecord>>(
        emptyList(),
    )
        private set

    var hasNext by mutableStateOf(false)
        private set

    var hasPrevious by mutableStateOf(false)
        private set

    private var stateTracker: StateTracker? = null
    private var currentBlockEvaluation: Int = 0
    private var currentEvaluation: Int = 0

    fun nextEvaluation() {
        val codeAttributes = stateTracker?.codeAttributes ?: return

        val blockEvaluations = codeAttributes[currentCodeAttribute].blockEvaluations
        val evaluations = blockEvaluations[currentBlockEvaluation].evaluations

        if (currentEvaluation < evaluations.size - 1) {
            currentEvaluation++
        } else if (currentBlockEvaluation < blockEvaluations.size - 1) {
            currentBlockEvaluation++
            currentEvaluation = 0
        } else if (currentCodeAttribute < codeAttributes.size - 1) {
            currentCodeAttribute++
            currentBlockEvaluation = 0
            currentEvaluation = 0
        }

        update()
    }

    fun previousEvaluation() {
        val codeAttributes = stateTracker?.codeAttributes ?: return

        if (currentEvaluation > 0) {
            currentEvaluation--
        } else if (currentBlockEvaluation > 0) {
            currentBlockEvaluation--
            currentEvaluation =
                codeAttributes[currentCodeAttribute].blockEvaluations[currentBlockEvaluation].evaluations.size - 1
        } else if (currentCodeAttribute > 0) {
            currentCodeAttribute--
            currentBlockEvaluation = codeAttributes[currentCodeAttribute].blockEvaluations.size - 1
            currentEvaluation =
                codeAttributes[currentCodeAttribute].blockEvaluations[currentBlockEvaluation].evaluations.size - 1
        }

        update()
    }

    private fun update() {
        val codeAttributes = stateTracker?.codeAttributes ?: return

        val blockEvaluations = codeAttributes[currentCodeAttribute].blockEvaluations
        val blockEvaluation = blockEvaluations[currentBlockEvaluation]
        evaluation = blockEvaluation.evaluations[currentEvaluation]
        currentBlockEvaluationStack = blockEvaluation.branchEvaluationStack

        hasNext = currentCodeAttribute < codeAttributes.size - 1 ||
            currentBlockEvaluation < blockEvaluations.size - 1 ||
            currentEvaluation < blockEvaluation.evaluations.size - 1

        hasPrevious = currentCodeAttribute > 0 || currentBlockEvaluation > 0 || currentEvaluation > 0
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

    /**
     * Resets the view model to its initial state.
     */
    fun reset() {
        hasNext = false
        hasPrevious = false

        currentEvaluation = 0
        currentBlockEvaluation = 0
        currentCodeAttribute = 0

        currentBlockEvaluationStack = emptyList()
        codeAttributes = emptyList()

        evaluation = null
        stateTracker = null
        file = null
    }
}
