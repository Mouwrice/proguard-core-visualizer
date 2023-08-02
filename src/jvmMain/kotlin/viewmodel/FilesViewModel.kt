package viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.LoadUtil
import data.StateTracker
import proguard.classfile.ClassPool
import java.nio.file.Path
import kotlin.io.path.extension

/**
 * A model to keep track and manage the files that are opened in the visualizer.
 * When the currentCodeAttribute has not been loaded, this will load it for you
 */
class FilesViewModel {
    /**
     * Map of path to <pair of <ClassPool; map of class to <map of method to viewModel>>>
     */
    var files by mutableStateOf(emptyMap<Path, Pair<ClassPool?, Map<String, Map<String, CodeAttributeViewModel?>>>>())
        private set

    /**
     * Currently selected path
     */
    var curPath by mutableStateOf<Path?>(null)

    /**
     * Currently selected clazz
     */
    var curClazz by mutableStateOf<String?>(null)

    /**
     * Currently selected method
     */
    var curMethod by mutableStateOf<String?>(null)

    var valueFactoryOption by mutableStateOf(LoadUtil.ValueFactoryOption.Basic)
        private set

    fun setEvalFactoryAndUpdate(evaluationMethod: LoadUtil.ValueFactoryOption) {
        this.valueFactoryOption = evaluationMethod
        reEvalCurMethod()
    }

    /**
     * Close a file
     */
    fun closeFile(path: Path) {
        files = files.toMutableMap().apply { remove(path) }
        if (curPath == path) {
            curPath = null
            curClazz = null
            curMethod = null
        }
    }

    private fun reEvalCurMethod(): CodeAttributeViewModel? {
        curPath?.let { path ->
            curClazz?.let { clazz ->
                curMethod?.let { method ->
                    files[path]?.first?.let { classPool ->
                        LoadUtil.trackerFromMethod(classPool, clazz, method, valueFactoryOption)?.let {
                            val codeAttribute = it.codeAttributes[0]
                            val newViewModel = CodeAttributeViewModel(codeAttribute)
                            val clazzMap = files.getValue(path).second
                            val methodMap = clazzMap.getValue(clazz)
                            files = files.plus(
                                Pair(
                                    path,
                                    Pair(
                                        classPool,
                                        clazzMap.plus(Pair(clazz, methodMap.plus(Pair(method, newViewModel)))),
                                    ),
                                ),
                            )
                            return newViewModel
                        }
                    }
                }
            }
        }
        return null
    }

    /**
     * Current viewModel, if not try to load it from the ClassPool
     */
    val currentCodeAttributeViewModel by derivedStateOf {
        files[curPath]?.second?.get(curClazz)?.get(curMethod)?.let { return@derivedStateOf it }
        return@derivedStateOf reEvalCurMethod()
    }

    /**
     * Loads the json file at the given path and add it to the list of files.
     */
    private fun loadJson(path: Path) {
        try {
            val stateTracker = StateTracker.fromJson(path)

            val some = stateTracker.codeAttributes.groupBy { it.clazz }
                .mapValues {
                    it.value.groupBy { map -> map.method }.mapValues { map -> CodeAttributeViewModel(map.value[0]) }
                }

            files = files.plus(Pair(path, Pair(null, some)))
        } catch (e: Exception) {
            println("Error while parsing json file: $e")
        }
    }

    /**
     * Loads every file type from the [FileTypes] enum except for [FileTypes.JSON].
     */
    private fun loadJar(path: Path) {
        val classPool = LoadUtil.readFile(path)
        files = files.plus(Pair(path, Pair(classPool, LoadUtil.classMethodMap(classPool))))
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
