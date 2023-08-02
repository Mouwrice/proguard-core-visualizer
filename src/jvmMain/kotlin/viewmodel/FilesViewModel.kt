package viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.LoadUtil
import data.LoadedPath
import data.OwnClazz
import data.OwnMethod
import data.StateTracker
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
    var files by mutableStateOf(emptyMap<Path, LoadedPath>())
        private set

    /**
     * Currently selected path
     */
    var curPath by mutableStateOf<LoadedPath?>(null)

    /**
     * Currently selected clazz
     */
    var curClazz by mutableStateOf<OwnClazz?>(null)

    /**
     * Currently selected method
     */
    var curMethod by mutableStateOf<OwnMethod?>(null)

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
        if (curPath?.path == path) {
            curPath = null
            curClazz = null
            curMethod = null
        }
    }

    private fun reEvalCurMethod(): CodeAttributeViewModel? {
        curPath?.let { path ->
            curClazz?.let { clazz ->
                curMethod?.let { method ->
                    path.classPool?.let { classPool ->
                        LoadUtil.evalSingleMethod(classPool, clazz.name, method.name, valueFactoryOption)?.let {
                            val codeAttribute = it.codeAttributes[0]
                            val newViewModel = CodeAttributeViewModel(codeAttribute)
                            files = files.plus(
                                Pair(
                                    path.path,
                                    LoadedPath(
                                        path.path, path.classPool,
                                        path.classMap.plus(
                                            Pair(
                                                clazz.name,
                                                OwnClazz(
                                                    clazz.name,
                                                    clazz.methodMap.plus(
                                                        Pair(
                                                            method.name,
                                                            OwnMethod(method.name, newViewModel),
                                                        ),
                                                    ),
                                                ),
                                            ),
                                        ),
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
        curMethod?.codeAttributeViewModel?.let { return@derivedStateOf it }
        return@derivedStateOf reEvalCurMethod()
    }

    /**
     * Loads the json file at the given path and add it to the list of files.
     */
    private fun loadJson(path: Path) {
        try {
            val stateTracker = StateTracker.fromJson(path)

            val classMap = stateTracker
                .codeAttributes
                .groupBy { it.clazz }
                .mapValues { (clazzName, clazzEntries) ->
                    OwnClazz(
                        clazzName,
                        clazzEntries
                            .groupBy { map -> map.method }
                            .mapValues { (method, methodEntries) ->
                                OwnMethod(method, CodeAttributeViewModel(methodEntries[0]))
                            },
                    )
                }

            files = files.plus(
                Pair(
                    path, LoadedPath(path, null, classMap),
                ),
            )
        } catch (e: Exception) {
            println("Error while parsing json file: $e")
        }
    }

    /**
     * Loads every file type from the [FileTypes] enum except for [FileTypes.JSON].
     */
    private fun loadJar(path: Path) {
        val classPool = LoadUtil.readFile(path)
        files = files.plus(
            Pair(
                path,
                LoadedPath(path, classPool, LoadUtil.classMethodMap(classPool)),
            ),
        )
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
