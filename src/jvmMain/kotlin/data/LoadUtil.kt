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
import proguard.io.ClassFilter
import proguard.io.ClassReader
import proguard.io.DataEntrySource
import proguard.io.FileSource
import proguard.io.JarReader
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

            return classPool
        }

        fun classMethodMap(classPool: ClassPool) {
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
        }
    }
}
