package viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.FileTypes
import data.LoadUtil
import data.LoadedClass
import data.LoadedMethod
import data.LoadedPath
import data.StateTracker
import proguard.classfile.ClassPool
import proguard.io.StreamingDataEntry
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name

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
    var curClazz by mutableStateOf<LoadedClass?>(null)

    /**
     * Currently selected method
     */
    var curMethod by mutableStateOf<LoadedMethod?>(null)

    var valueFactoryOption by mutableStateOf(LoadUtil.ValueFactoryOption.Basic)
        private set

    var scratchCount by mutableStateOf(0)
        private set

    /**
     * Whether the editor should be shown.
     * The editor currently is a scratchpad for writing jbc code.
     */
    var showEditor by mutableStateOf(false)

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
                        LoadUtil.trackerFromMethod(classPool, clazz.name, method.name, valueFactoryOption)?.let {
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
                                                LoadedClass(
                                                    clazz.name,
                                                    clazz.methodMap.plus(
                                                        Pair(
                                                            method.name,
                                                            LoadedMethod(method.name, newViewModel),
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

    private fun loadJson(path: Path, stateTracker: StateTracker) {
        try {
            val classMap = stateTracker
                .codeAttributes
                .groupBy { it.clazz }
                .mapValues { (clazzName, clazzEntries) ->
                    LoadedClass(
                        clazzName,
                        clazzEntries
                            .groupBy { map -> map.method }
                            .mapValues { (method, methodEntries) ->
                                LoadedMethod(method, CodeAttributeViewModel(methodEntries[0]))
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
     * Loads the json file at the given path and add it to the list of files.
     */
    private fun loadJson(path: Path) {
        loadJson(path, StateTracker.fromJson(path))
    }

    private fun loadClassPool(path: Path, classPool: ClassPool) {
        files = files.plus(
            Pair(
                path,
                LoadedPath(path, classPool, LoadUtil.classMethodMap(classPool)),
            ),
        )
    }

    /**
     * Loads every file type from the [FileTypes] enum except for [FileTypes.JSON].
     */
    private fun loadPath(path: Path) {
        loadClassPool(path, LoadUtil.loadJar(path))
    }

    private fun loadJBC(path: Path, content: String) {
        val classPool = LoadUtil.parseJbcString(path.name, content)
        files = files.plus(
            Pair(
                path,
                LoadedPath(path, classPool, LoadUtil.classMethodMap(classPool)),
            ),
        )
    }

    fun loadScratch(content: String, fileType: FileTypes) {
        val path = Path.of("scratch-file$scratchCount.${fileType.extension}")
        when (fileType) {
            FileTypes.JSON -> loadJson(path, StateTracker.fromJson(content))
            FileTypes.JBC -> loadJBC(path, content)
            FileTypes.JAR, FileTypes.APK, FileTypes.CLASS, FileTypes.AAR, FileTypes.DEX, FileTypes.ZIP ->
                loadClassPool(
                    path,
                    LoadUtil.loadJar(
                        StreamingDataEntry(
                            path.fileName.name,
                            content.byteInputStream(),
                        ),
                    ),
                )
        }
        scratchCount++
    }

    fun loadFile(path: Path) {
        try {
            val type = FileTypes.valueOf(path.extension.uppercase())
            when (type) {
                FileTypes.JSON -> loadJson(path)
                FileTypes.JAR, FileTypes.APK, FileTypes.CLASS, FileTypes.AAR, FileTypes.DEX, FileTypes.ZIP, FileTypes.JBC -> loadPath(path)
            }
        } catch (e: IllegalArgumentException) {
            println("Unsupported file type: $e")
        }
    }
}
