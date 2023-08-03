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
import proguard.evaluation.BasicInvocationUnit
import proguard.evaluation.PartialEvaluator
import proguard.evaluation.ReferenceTracingValueFactory
import proguard.evaluation.util.jsonprinter.JsonPrinter
import proguard.evaluation.value.ArrayReferenceValueFactory
import proguard.evaluation.value.BasicValueFactory
import proguard.evaluation.value.DetailedArrayValueFactory
import proguard.evaluation.value.IdentifiedValueFactory
import proguard.evaluation.value.ParticularValueFactory
import proguard.evaluation.value.RangeValueFactory
import proguard.evaluation.value.TypedReferenceValueFactory
import proguard.evaluation.value.ValueFactory
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

data class LoadedMethod(val name: String, val codeAttributeViewModel: CodeAttributeViewModel?)
data class LoadedClass(val name: String, val methodMap: Map<String, LoadedMethod>)
data class LoadedPath(val path: Path, val classPool: ClassPool?, val classMap: Map<String, LoadedClass>)

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

        fun evaluateMethod(classPool: ClassPool, clazz: String, method: String, valueFactoryOption: ValueFactoryOption): String {
            val tracker = JsonPrinter()
            val valueFactory = valueFactoryOption.toValueFactory()
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

        fun trackerFromMethod(classPool: ClassPool, clazz: String, method: String, valueFactoryOption: ValueFactoryOption): StateTracker? {
            try {
                return StateTracker.fromJson(evaluateMethod(classPool, clazz, method, valueFactoryOption))
            } catch (e: Exception) {
                println("Error while parsing json file: $e")
            }
            return null
        }
    }

    enum class ValueFactoryOption {
        Basic {
            override fun toString(): String {
                return "Basic Value Factory"
            }

            override fun toValueFactory(): ValueFactory {
                return BasicValueFactory()
            }
        },
        Particular {
            override fun toString(): String {
                return "Particular Value Factory"
            }

            override fun toValueFactory(): ValueFactory {
                return ParticularValueFactory()
            }
        },
        Range {
            override fun toString(): String {
                return "Range Value Factory"
            }

            override fun toValueFactory(): ValueFactory {
                return RangeValueFactory()
            }
        },
        ArrayReference {
            override fun toString(): String {
                return "Array Reference Value Factory"
            }

            override fun toValueFactory(): ValueFactory {
                return ArrayReferenceValueFactory()
            }
        },
        Identified {
            override fun toString(): String {
                return "Identified Value Factory"
            }

            override fun toValueFactory(): ValueFactory {
                return IdentifiedValueFactory()
            }
        },
        ReferenceTracing {
            override fun toString(): String {
                return "Reference Tracing Value Factory"
            }

            override fun toValueFactory(): ValueFactory {
                return ReferenceTracingValueFactory(BasicValueFactory())
            }
        },
        TypedReference {
            override fun toString(): String {
                return "Typed Reference Value Factory"
            }

            override fun toValueFactory(): ValueFactory {
                return TypedReferenceValueFactory()
            }
        },
        DetailedArrayReference {
            override fun toString(): String {
                return "Detailed Array Reference Value Factory"
            }

            override fun toValueFactory(): ValueFactory {
                return DetailedArrayValueFactory()
            }
        }, ;
        abstract fun toValueFactory(): ValueFactory
    }
}
