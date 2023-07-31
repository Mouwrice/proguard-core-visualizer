package viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.StateTracker
import proguard.classfile.ClassPool
import proguard.classfile.attribute.Attribute
import proguard.classfile.attribute.visitor.AllAttributeVisitor
import proguard.classfile.attribute.visitor.AttributeNameFilter
import proguard.classfile.visitor.AllClassVisitor
import proguard.classfile.visitor.AllMethodVisitor
import proguard.classfile.visitor.ClassPoolFiller
import proguard.classfile.visitor.ClassPrinter
import proguard.evaluation.PartialEvaluator
import proguard.evaluation.util.jsonPrinter.JsonPrinter
import proguard.io.ClassReader
import proguard.io.DataEntryReader
import proguard.io.DataEntrySource
import proguard.io.DexClassReader
import proguard.io.DirectorySource
import proguard.io.FileSource
import proguard.io.JarReader
import proguard.io.NameFilteredDataEntryReader
import java.nio.file.Path
import kotlin.io.path.extension

/**
 * A model to keep track and manage the files that are opened in the visualizer.
 */
class FilesViewModel {
    var files by mutableStateOf(emptyList<File>())
        private set

    private fun addFile(file: File) {
        files = files.plus(file)
    }

    fun closeFile(index: Int) {
        files.getOrNull(index)?.let {
            files = files.minus(it)
        }
    }

    private var fileIndex by mutableStateOf(0)

    private val currentFile by derivedStateOf { files.getOrNull(fileIndex) }

    private var codeAttributeIndex by mutableStateOf(0)

    val currentCodeAttribute by derivedStateOf { currentFile?.codeAttributeViewModels?.getOrNull(codeAttributeIndex) }

    fun selectCodeAttribute(fileIndex: Int, attributeIndex: Int) {
        this.fileIndex = fileIndex
        this.codeAttributeIndex = attributeIndex
    }

    private fun evaluate(classPool: ClassPool): JsonPrinter {
        val tracker = JsonPrinter()
        val pe = PartialEvaluator.Builder.create()
            .setEvaluateAllCode(true).setStateTracker(tracker).build()
        classPool.accept(
            AllClassVisitor(
                AllMethodVisitor(
                    AllAttributeVisitor(
                        AttributeNameFilter(Attribute.CODE, pe),
                    ),
                ),
            ),
        )

        return tracker
    }

    private fun parseTracker(path: Path, tracker: JsonPrinter) {
        try {
            tracker.printState()
            val file = File(path, StateTracker.fromJson(tracker.json).codeAttributes)
            addFile(file)
        } catch (e: Exception) {
            println("Error while parsing json file: $e")
        }
    }

    /**
     * Loads the json file at the given path and add it to the list of files.
     */
    private fun loadJson(path: Path) {
        try {
            val stateTracker = StateTracker.fromJson(path)
            val file = File(path, stateTracker.codeAttributes)
            addFile(file)
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
                ClassReader(
                    false,
                    false,
                    false,
                    false,
                    null,
                    ClassPoolFiller(classPool),
                ),
            ),
        )

        val tracker = evaluate(classPool)
        parseTracker(path, tracker)
    }

    /**
     * Loads the apk file and tries to evaluate it.
     */
    private fun loadApk(path: Path) {
        val classPool = ClassPool()

        val source: DataEntrySource = DirectorySource(
            path.toFile(),
        )

        var classReader: DataEntryReader = NameFilteredDataEntryReader(
            "**.class",
            ClassReader(
                false,
                false,
                false,
                false,
                null,
                ClassPrinter(),
            ),
        )

        // Convert dex files to a jar first
        classReader = NameFilteredDataEntryReader(
            "classes*.dex",
            DexClassReader(
                true,
                ClassPrinter(),
            ),
        )

        source.pumpDataEntries(classReader)

        val tracker = evaluate(classPool)
        parseTracker(path, tracker)
    }

    /**
     * Loads the class file at the given path and tries to evaluate it.
     */
    private fun loadClass(path: Path) {
        val classPool = ClassPool()

        val source = FileSource(path.toFile())

        source.pumpDataEntries(ClassReader(false, false, false, false, null, ClassPoolFiller(classPool)))

        val tracker = evaluate(classPool)
        parseTracker(path, tracker)
    }

    fun loadFile(path: Path) {
        try {
            val type = FileTypes.valueOf(path.extension.uppercase())
            when (type) {
                FileTypes.JSON -> loadJson(path)
                FileTypes.JAR -> loadJar(path)
                FileTypes.APK -> loadApk(path)
                FileTypes.CLASS -> loadClass(path)
            }
        } catch (e: IllegalArgumentException) {
            println("Unsupported file type: $e")
        }
    }
}
