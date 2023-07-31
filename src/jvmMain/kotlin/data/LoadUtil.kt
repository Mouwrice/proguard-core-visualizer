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
import proguard.io.DataEntryReader
import proguard.io.DataEntrySource
import proguard.io.DexClassReader
import proguard.io.DirectorySource
import proguard.io.FileSource
import proguard.io.JarReader
import proguard.io.NameFilteredDataEntryReader
import viewmodel.CodeAttributeViewModel
import java.nio.file.Path

class LoadUtil {
    companion object {
        fun getClassPoolFromJAR(path: Path): ClassPool {
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

            return classPool
        }

        fun getClassPoolFromApk(path: Path): ClassPool {
            val classPool = ClassPool()
            val source: DataEntrySource = DirectorySource(path.toFile())
            var classReader: DataEntryReader = NameFilteredDataEntryReader(
                "**.class",
                ClassReader(
                    false,
                    false,
                    false,
                    false,
                    null,
                    ClassPoolFiller(classPool),
                ),
            )

            // Convert dex files to a jar first
            classReader = NameFilteredDataEntryReader(
                "classes*.dex",
                DexClassReader(
                    true,
                    ClassPoolFiller(classPool),
                ),
                classReader,
            )
            source.pumpDataEntries(JarReader(classReader))

            return classPool
        }

        fun getClassPoolFromClass(path: Path): ClassPool {
            val classPool = ClassPool()

            val source = FileSource(path.toFile())

            source.pumpDataEntries(ClassReader(false, false, false, false, null, ClassPoolFiller(classPool)))

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
