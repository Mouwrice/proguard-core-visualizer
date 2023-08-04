package data

import com.guardsquare.proguard.assembler.io.JbcReader
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
import proguard.evaluation.BasicInvocationUnit
import proguard.evaluation.PartialEvaluator
import proguard.evaluation.util.jsonprinter.JsonPrinter
import proguard.io.ClassReader
import proguard.io.DataEntry
import proguard.io.DataEntryNameFilter
import proguard.io.DataEntryReader
import proguard.io.DataEntrySource
import proguard.io.DexClassReader
import proguard.io.FileSource
import proguard.io.FilteredDataEntryReader
import proguard.io.JarReader
import proguard.io.NameFilteredDataEntryReader
import proguard.io.StreamingDataEntry
import proguard.util.ExtensionMatcher
import proguard.util.OrMatcher
import viewmodel.CodeAttributeViewModel
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

data class LoadedMethod(val name: String, val codeAttributeViewModel: CodeAttributeViewModel?)
data class LoadedClass(val name: String, val methodMap: Map<String, LoadedMethod>)

/**
 * Representation of a loaded and parsed file.
 * @param content The original content of the file which is used for the editor.
 */
data class LoadedPath(
    val path: Path,
    val classPool: ClassPool?,
    val classMap: Map<String, LoadedClass>,
    val content: String? = null,
)

class LoadUtil {
    companion object {
        fun loadJar(
            path: Path,
        ): ClassPool {
            if (path.endsWith(".${FileTypes.JBC.extension}")) {
                return parseJbcString(path.name, Files.readString(path))
            }

            return loadJar(FileSource(path.toFile()))
        }

        fun loadJar(
            entry: DataEntry,
        ): ClassPool {
            return loadJar { reader ->
                reader?.read(
                    entry,
                )
            }
        }

        /**
         * Reads the classes from the specified jar file and returns them as a class
         * pool.
         *
         * ProgramClass instances (for processing) or
         * LibraryClass instances (more compact).
         * @return a new class pool with the read classes.
         */
        @Throws(IOException::class)
        fun loadJar(
            source: DataEntrySource,
        ): ClassPool {
            val classPool = ClassPool()

            // Parse all classes from the input jar and
            // collect them in the class pool.
            val classPoolFiller = ClassPoolFiller(classPool)
            var classReader: DataEntryReader = NameFilteredDataEntryReader(
                "**.class",
                ClassReader(
                    false,
                    false,
                    false,
                    false,
                    null,
                    classPoolFiller,
                ),
            )

            // Convert dex files to a JAR first.
            classReader = NameFilteredDataEntryReader(
                "classes*.dex",
                DexClassReader(true, classPoolFiller),
                classReader,
            )

            // Extract files from an archive if necessary.
            classReader = FilteredDataEntryReader(
                DataEntryNameFilter(ExtensionMatcher("aar")),
                JarReader(
                    NameFilteredDataEntryReader(
                        "classes.jar",
                        JarReader(classReader),
                    ),
                ),
                FilteredDataEntryReader(
                    DataEntryNameFilter(
                        OrMatcher(
                            ExtensionMatcher("jar"),
                            ExtensionMatcher("zip"),
                            ExtensionMatcher("apk"),
                        ),
                    ),
                    JarReader(classReader),
                    classReader,
                ),
            )
            source.pumpDataEntries(classReader)
            return classPool
        }

        fun parseJbcString(name: String, jbcString: String): ClassPool {
            val programClassPool = ClassPool()

            val jbcReader: DataEntryReader = JbcReader(
                ClassPoolFiller(programClassPool),
            )
            jbcReader.read(StreamingDataEntry(name, jbcString.byteInputStream()))

            return programClassPool
        }

        fun classMethodMap(classPool: ClassPool): Map<String, LoadedClass> {
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
                                        classMap.getOrPut(clazz.name) { HashMap() }[
                                            method.getName(clazz) + method.getDescriptor(
                                                clazz,
                                            ),
                                        ] = null
                                    }
                                },
                            ),
                        ),
                    ),
                ),
            )
            return classMap.mapValues { (clazz, methodMap) ->
                LoadedClass(clazz, methodMap.mapValues { LoadedMethod(it.key, it.value) })
            }
        }

        fun evaluateMethod(
            classPool: ClassPool,
            clazz: String,
            method: String,
            valueFactoryType: ValueFactoryType,
        ): String {
            val tracker = JsonPrinter()
            val valueFactory = valueFactoryType.toValueFactory()
            val pe = PartialEvaluator.Builder.create()
                .setValueFactory(valueFactory)
                .setInvocationUnit(BasicInvocationUnit(valueFactory))
                .setStateTracker(tracker)
                .build()
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
                                        if (visitedMethod.getName(clazz) + visitedMethod.getDescriptor(clazz) == method) {
                                            pe.visitCodeAttribute(clazz, visitedMethod, codeAttribute)
                                        }
                                    }
                                },
                            ),
                        ),
                    ),
                ),
            )
            return tracker.json
        }

        fun trackerFromMethod(
            classPool: ClassPool,
            clazz: String,
            method: String,
            valueFactoryType: ValueFactoryType,
        ): StateTracker? {
            try {
                return StateTracker.fromJson(evaluateMethod(classPool, clazz, method, valueFactoryType))
            } catch (e: Exception) {
                println("Error while parsing json file: $e")
            }
            return null
        }
    }
}
