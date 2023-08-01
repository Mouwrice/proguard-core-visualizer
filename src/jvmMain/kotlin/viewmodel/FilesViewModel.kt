package viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.CodeAttributeRecord
import data.LoadUtil
import data.StateTracker
import java.nio.file.Path
import kotlin.io.path.extension

/**
 * A model to keep track and manage the files that are opened in the visualizer.
 * When the currentCodeAttribute has not been loaded, this will load it for you
 */
class FilesViewModel {
    var files by mutableStateOf(emptyMap<Path, Map<String, Map<String, CodeAttributeViewModel?>>>())
        private set

    var curPath by mutableStateOf<Path?>(null)

    var curClazz by mutableStateOf<String?>(null)

    var curMethod by mutableStateOf<String?>(null)

    private fun addFile(file: Path, codeAttributes: List<CodeAttributeRecord>) {
        val some = codeAttributes.groupBy { it.clazz }
            .mapValues {
                it.value.groupBy { map -> map.method }.mapValues { map -> CodeAttributeViewModel(map.value[0]) }
            }

        files = files.plus(Pair(file, some))
    }

    fun closeFile(path: Path) {
        files = files.toMutableMap().apply { remove(path) }
        if (curPath == path) {
            curPath = null
            curClazz = null
            curMethod = null
        }
    }

    val currentCodeAttributeViewModel by derivedStateOf {
        curPath?.let { path ->
            curClazz?.let { clazz ->
                curMethod?.let { method ->
                    val potentialViewModel = files[curPath]?.get(curClazz)?.get(curMethod)
                    potentialViewModel?.let { return@derivedStateOf it }

                    val classPool = LoadUtil.readJar(path)
                    LoadUtil.evalSingleMethod(classPool, clazz, method)?.let {
                        val codeAttribute = it.codeAttributes[0]
                        val newViewModel = CodeAttributeViewModel(codeAttribute)
                        val clazzMap = files.getValue(path)
                        val methodMap = clazzMap.getValue(clazz)
                        files = files.plus(
                            Pair(
                                path,
                                clazzMap.plus(Pair(clazz, methodMap.plus(Pair(method, newViewModel)))),
                            ),
                        )
                        return@derivedStateOf newViewModel
                    }
                }
            }
        }
        return@derivedStateOf null
    }

    /**
     * Loads the json file at the given path and add it to the list of files.
     */
    private fun loadJson(path: Path) {
        try {
            val stateTracker = StateTracker.fromJson(path)
            addFile(path, stateTracker.codeAttributes)
        } catch (e: Exception) {
            println("Error while parsing json file: $e")
        }
    }

    /**
     * Loads every file type from the [FileTypes] enum except for [FileTypes.JSON].
     */
    private fun loadJar(path: Path) {
        files = files.plus(Pair(path, LoadUtil.classMethodMap(LoadUtil.readJar(path))))
    }

    fun loadFile(path: Path) {
        try {
            val type = FileTypes.valueOf(path.extension.uppercase())
            when (type) {
                FileTypes.JSON -> loadJson(path)
                FileTypes.JAR, FileTypes.APK, FileTypes.CLASS, FileTypes.AAR, FileTypes.DEX, FileTypes.ZIP -> loadJar(path)
            }
        } catch (e: IllegalArgumentException) {
            println("Unsupported file type: $e")
        }
    }
}
