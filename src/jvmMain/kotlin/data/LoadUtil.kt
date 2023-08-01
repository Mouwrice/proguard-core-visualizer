package data

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
import proguard.io.ClassReader
import proguard.io.DataEntryNameFilter
import proguard.io.DataEntryReader
import proguard.io.DataEntrySource
import proguard.io.DexClassReader
import proguard.io.FileSource
import proguard.io.FilteredDataEntryReader
import proguard.io.JarReader
import proguard.io.NameFilteredDataEntryReader
import proguard.util.ExtensionMatcher
import proguard.util.OrMatcher
import viewmodel.CodeAttributeViewModel
import java.io.IOException
import java.nio.file.Path

class LoadUtil {
    companion object {
        /**
         * Reads the classes from the specified jar file and returns them as a class
         * pool.
         *
         * ProgramClass instances (for processing) or
         * LibraryClass instances (more compact).
         * @return a new class pool with the read classes.
         */
        @Throws(IOException::class)
        fun readFile(
            path: Path,
        ): ClassPool {
            val classPool = ClassPool()

            // Parse all classes from the input jar and
            // collect them in the class pool.
            val source: DataEntrySource = FileSource(path.toFile())
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
                DexClassReader(false, classPoolFiller),
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

        fun classMethodMap(classPool: ClassPool): Map<String, Map<String, CodeAttributeViewModel?>> {
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
            return classMap
        }

        fun evalSingleMethod(classPool: ClassPool, clazz: String, method: String): StateTracker? {
            val tracker = JsonPrinter()
            val pe = PartialEvaluator.Builder.create().setEvaluateAllCode(true).setStateTracker(tracker).build()
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
            try {
                return StateTracker.fromJson(tracker.json)
            } catch (e: Exception) {
                println("Error while parsing json file: $e")
            }
            return null
        }
    }
}