package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.BranchTargetRecord
import data.ExceptionHandlerRecord
import data.InstructionEvaluationRecord
import data.StateTracker
import java.io.File

/**
 * This view model is a very close representation of the loaded json file.
 * The view model always belongs to a single file.
 * When loading a new file, the view model creates and returns a new instance.
 * @param file The file that is represented by this instance.
 * @param stateTracker The state tracker that is used to parse the json file.
 */
class DebuggerViewModel private constructor(val file: File, stateTracker: StateTracker) {
    /**
     * All the code attributes parsed from the json file.
     */
    val codeAttributes = stateTracker.codeAttributes

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

    var currentExceptionHandler by mutableStateOf<ExceptionHandlerRecord?>(null)
        private set

    var hasNext by mutableStateOf(false)
        private set

    var hasPrevious by mutableStateOf(false)
        private set

    private var currentBlockEvaluation: Int = 0
    private var currentEvaluation: Int = 0

    init {
        val blockEvaluations = codeAttributes[currentCodeAttribute].blockEvaluations
        val blockEvaluation = blockEvaluations[currentBlockEvaluation]
        currentExceptionHandler = blockEvaluation.exceptionHandlerInfo
        evaluation = blockEvaluation.evaluations[currentEvaluation]
        currentBlockEvaluationStack = blockEvaluation.branchEvaluationStack ?: emptyList()
        hasNext = currentCodeAttribute < codeAttributes.size - 1 ||
            currentBlockEvaluation < blockEvaluations.size - 1 ||
            currentEvaluation < blockEvaluation.evaluations.size - 1
        hasPrevious = currentCodeAttribute > 0 || currentBlockEvaluation > 0 || currentEvaluation > 0
    }

    fun nextEvaluation() {
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
        val blockEvaluations = codeAttributes[currentCodeAttribute].blockEvaluations
        val blockEvaluation = blockEvaluations[currentBlockEvaluation]
        currentExceptionHandler = blockEvaluation.exceptionHandlerInfo
        evaluation = blockEvaluation.evaluations[currentEvaluation]
        currentBlockEvaluationStack = blockEvaluation.branchEvaluationStack ?: emptyList()

        hasNext = currentCodeAttribute < codeAttributes.size - 1 ||
            currentBlockEvaluation < blockEvaluations.size - 1 ||
            currentEvaluation < blockEvaluation.evaluations.size - 1

        hasPrevious = currentCodeAttribute > 0 || currentBlockEvaluation > 0 || currentEvaluation > 0
    }

    /**
     * Loads the json file at the given path and returns a new view model.
     */
    fun loadJson(path: String): DebuggerViewModel {
        val file = File(path)
        return try {
            val stateTracker = StateTracker.fromJson(path)
            DebuggerViewModel(file, stateTracker)
        } catch (e: Exception) {
            println("Error while parsing json file: $e")
            this
        }
    }

    companion object {
        /**
         * Create a new view model given a path to a json file.
         * Used to create the initial view model.
         */
        fun fromJson(path: String): DebuggerViewModel {
            val file = File(path)
            val stateTracker = StateTracker.fromJson(path)
            return DebuggerViewModel(file, stateTracker)
        }
    }
}
