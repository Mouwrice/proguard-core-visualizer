package viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.CodeAttributeRecord
import data.StateTracker
import proguard.classfile.ClassPool
import proguard.classfile.Clazz
import proguard.classfile.Method
import proguard.classfile.attribute.Attribute
import proguard.classfile.attribute.CodeAttribute
import proguard.classfile.attribute.visitor.AllAttributeVisitor
import proguard.classfile.attribute.visitor.AttributeNameFilter
import proguard.classfile.attribute.visitor.AttributeVisitor
import proguard.classfile.visitor.AllClassVisitor
import proguard.classfile.visitor.AllMethodVisitor
import proguard.classfile.visitor.ClassPoolFiller
import proguard.classfile.visitor.FilteredClassVisitor
import proguard.evaluation.PartialEvaluator
import proguard.evaluation.util.jsonPrinter.JsonPrinter
import proguard.io.ClassFilter
import proguard.io.ClassReader
import proguard.io.DataEntrySource
import proguard.io.FileSource
import proguard.io.JarReader
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
        files = files.minus(path)
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

                    // try to construct the viewModel
                    val classPool = ClassPool()

                    val source: DataEntrySource = FileSource(
                        path.toFile(),
                    )

                    source.pumpDataEntries(
                        JarReader(
                            false,
                            ClassFilter(
                                ClassReader(
                                    false,
                                    false,
                                    false,
                                    false,
                                    null,
                                    ClassPoolFiller(classPool),
                                ),
                            ),
                        ),
                    )

                    val tracker = JsonPrinter()
                    val pe = PartialEvaluator.Builder.create()
                        .setEvaluateAllCode(true).setStateTracker(tracker).build()
                    classPool.accept(
                        FilteredClassVisitor(
                            clazz,
                            AllMethodVisitor(
                                AllAttributeVisitor(
                                    AttributeNameFilter(
                                        Attribute.CODE,
                                        object : AttributeVisitor {
                                            override fun visitCodeAttribute(
                                                clazz: Clazz,
                                                visitedMethod: Method,
                                                codeAttribute: CodeAttribute,
                                            ) {
                                                if (visitedMethod.getName(clazz) == method) {
                                                    pe.visitCodeAttribute(clazz, visitedMethod, codeAttribute)
                                                }
                                            }
                                        },
                                    ),
                                ),
                            ),
                        ),
                    )
                    try {
                        val newViewModel = CodeAttributeViewModel(StateTracker.fromJson(tracker.json).codeAttributes[0])
                        val clazzMap = files.getValue(path)
                        val methodMap = clazzMap.getValue(clazz)
                        files = files.plus(Pair(path, clazzMap.plus(Pair(clazz, methodMap.plus(Pair(method, newViewModel))))))
                        return@derivedStateOf newViewModel
                    } catch (e: Exception) {
                        println("Error while parsing json file: $e")
                    }
                }
            }
        }
        return@derivedStateOf null
    }

    /**
     * Loads the json file at the given path and returns a new view model.
     */
    private fun loadJson(path: Path) {
        try {
            val stateTracker = StateTracker.fromJson(path)
            addFile(path, stateTracker.codeAttributes)
        } catch (e: Exception) {
            println("Error while parsing json file: $e")
        }
    }

    private fun loadJar(path: Path) {
        val classPool = ClassPool()

        val source: DataEntrySource = FileSource(
            path.toFile(),
        )

        source.pumpDataEntries(
            JarReader(
                false,
                ClassFilter(
                    ClassReader(
                        false,
                        false,
                        false,
                        false,
                        null,
                        ClassPoolFiller(classPool),
                    ),
                ),
            ),
        )

        val classMap: MutableMap<String, MutableMap<String, CodeAttributeViewModel?>> = HashMap()
        classPool.accept(
            AllClassVisitor(
                AllMethodVisitor(
                    AllAttributeVisitor(
                        AttributeNameFilter(
                            Attribute.CODE,
                            object : AttributeVisitor {
                                override fun visitCodeAttribute(
                                    clazz: Clazz,
                                    method: Method,
                                    codeAttribute: CodeAttribute,
                                ) {
                                    classMap.getOrPut(clazz.name) { HashMap() }[method.getName(clazz)] = null
                                }
                            },
                        ),
                    ),
                ),
            ),
        )

        files = files.plus(Pair(path, classMap))
    }

    fun loadFile(path: Path) {
        when (path.extension) {
            "json" -> loadJson(path)
            "jar" -> loadJar(path)
        }
    }
}
