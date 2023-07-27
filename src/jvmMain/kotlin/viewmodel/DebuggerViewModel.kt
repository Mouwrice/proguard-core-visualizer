package viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.StateTracker
import java.nio.file.Path

/**
 * This view model is a very close representation of the loaded json file.
 */
class DebuggerViewModel {
    var openedFiles by mutableStateOf(emptyList<Pair<Path, StateTracker>>())
        private set

    fun addFile(file: Path, stateTracker: StateTracker) {
        openedFiles = openedFiles.plus(Pair(file, stateTracker))
        updateAttributeIndex(0)
    }

    fun closeFile(index: Int) {
        openedFiles = openedFiles.minus(openedFiles[index])
    }

    var fileIndex by mutableStateOf(0)
        private set

    fun updateFileIndex(value: Int) {
        updateAttributeIndex(0)
        fileIndex = value
    }

    val codeAttributes by derivedStateOf { openedFiles.getOrNull(fileIndex)?.second?.codeAttributes ?: emptyList() }

    val path by derivedStateOf { openedFiles.getOrNull(fileIndex)?.first }

    var attributeIndex by mutableStateOf(0)
        private set

    fun updateAttributeIndex(value: Int) {
        instructionIndex = 0
        updateEvaluationBlockIndex(0)
        attributeIndex = value
    }

    val codeAttribute by derivedStateOf { codeAttributes.getOrNull(attributeIndex) }

    var instructionIndex by mutableStateOf(0)

    val instruction by derivedStateOf { codeAttribute?.instructions?.getOrNull(instructionIndex) }

    private var evaluationBlockIndex by mutableStateOf(0)

    fun updateEvaluationBlockIndex(value: Int) {
        evaluationBlockIndex = value
        evaluationIndex = 0
    }

    val evaluationBlock by derivedStateOf { codeAttribute?.blockEvaluations?.getOrNull(evaluationBlockIndex) }

    var evaluationIndex by mutableStateOf(0)

    val evaluation by derivedStateOf { evaluationBlock?.evaluations?.getOrNull(evaluationIndex) }

    val hasNext by derivedStateOf<Boolean> {
        when (display) {
            Display.EVALUATIONS -> {
                val blockEvaluations = codeAttribute?.blockEvaluations
                val blockEvaluation = blockEvaluations?.getOrNull(evaluationBlockIndex)
                blockEvaluation != null && (
                    evaluationBlockIndex < blockEvaluations.size - 1 ||
                        evaluationIndex < blockEvaluation.evaluations.size - 1
                    )
            }
            Display.RESULTS -> instructionIndex < (codeAttribute?.instructions?.size ?: 0) - 1
        }
    }

    val hasPrevious by derivedStateOf {
        when (display) {
            Display.EVALUATIONS -> evaluationBlockIndex > 0 || evaluationIndex > 0
            Display.RESULTS -> instructionIndex > 0
        }
    }

    // Whether to show the final results of the evaluation for each instruction.
    var display by mutableStateOf(Display.EVALUATIONS)

    private fun previousInstruction() {
        if (instructionIndex > 0) {
            instructionIndex--
        }
    }

    private fun previousEvaluation() {
        if (evaluationIndex > 0) {
            evaluationIndex--
        } else if (evaluationBlockIndex > 0) {
            evaluationBlockIndex--
            evaluationIndex = (evaluationBlock?.evaluations?.size ?: 1) - 1
        }
    }

    private fun nextInstruction() {
        if (instructionIndex < (codeAttribute?.instructions?.size ?: 0) - 1) {
            instructionIndex++
        }
    }

    private fun nextEvaluation() {
        val blockEvaluations = codeAttribute?.blockEvaluations
        val evaluations = blockEvaluations?.getOrNull(evaluationBlockIndex)?.evaluations
        if (evaluations != null) {
            if (evaluationIndex < evaluations.size - 1) {
                evaluationIndex++
            } else if (evaluationBlockIndex < blockEvaluations.size - 1) {
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

    fun switchDisplay() {
        display = when (display) {
            Display.EVALUATIONS -> Display.RESULTS
            Display.RESULTS -> Display.EVALUATIONS
        }
    }

    /**
     * Loads the json file at the given path and returns a new view model.
     */
    fun loadJson(path: Path): DebuggerViewModel {
        return try {
            val stateTracker = StateTracker.fromJson(path)
            addFile(path, stateTracker)
            return this
        } catch (e: Exception) {
            println("Error while parsing json file: $e")
            this
        }
    }

    /**
     * Loads the given json string and returns a new view model.
     */
    fun loadJson(path: Path, json: String): DebuggerViewModel {
        return try {
            val stateTracker = StateTracker.fromJson(json)
            addFile(path, stateTracker)
            return this
        } catch (e: Exception) {
            println("Error while parsing json file: $e")
            this
        }
    }
}
