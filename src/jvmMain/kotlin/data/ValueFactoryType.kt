package data

import proguard.evaluation.ParticularReferenceValueFactory
import proguard.evaluation.ReferenceTracingValueFactory
import proguard.evaluation.value.ArrayReferenceValueFactory
import proguard.evaluation.value.BasicValueFactory
import proguard.evaluation.value.DetailedArrayValueFactory
import proguard.evaluation.value.IdentifiedValueFactory
import proguard.evaluation.value.ParticularValueFactory
import proguard.evaluation.value.RangeValueFactory
import proguard.evaluation.value.TypedReferenceValueFactory

enum class ValueFactoryType {
    Basic {
        override fun toString(): String {
            return "Basic Value Factory"
        }

        override fun toValueFactory(): proguard.evaluation.value.ValueFactory {
            return BasicValueFactory()
        }
    },
    Particular {
        override fun toString(): String {
            return "Particular Value Factory"
        }

        override fun toValueFactory(): proguard.evaluation.value.ValueFactory {
            return ParticularValueFactory()
        }
    },
    Range {
        override fun toString(): String {
            return "Range Value Factory"
        }

        override fun toValueFactory(): proguard.evaluation.value.ValueFactory {
            return RangeValueFactory()
        }
    },
    ArrayReference {
        override fun toString(): String {
            return "Array Reference Value Factory"
        }

        override fun toValueFactory(): proguard.evaluation.value.ValueFactory {
            return ArrayReferenceValueFactory()
        }
    },
    Identified {
        override fun toString(): String {
            return "Identified Value Factory"
        }

        override fun toValueFactory(): proguard.evaluation.value.ValueFactory {
            return IdentifiedValueFactory()
        }
    },
    ReferenceTracing {
        override fun toString(): String {
            return "Reference Tracing Value Factory"
        }

        override fun toValueFactory(): proguard.evaluation.value.ValueFactory {
            return ReferenceTracingValueFactory(BasicValueFactory())
        }
    },
    TypedReference {
        override fun toString(): String {
            return "Typed Reference Value Factory"
        }

        override fun toValueFactory(): proguard.evaluation.value.ValueFactory {
            return TypedReferenceValueFactory()
        }
    },
    DetailedArrayReference {
        override fun toString(): String {
            return "Detailed Array Reference Value Factory"
        }

        override fun toValueFactory(): proguard.evaluation.value.ValueFactory {
            return DetailedArrayValueFactory()
        }
    },
    ParticularReferenceValueType {
        override fun toString(): String {
            return "Particular Reference Value Factory"
        }

        override fun toValueFactory(): proguard.evaluation.value.ValueFactory {
            return ParticularReferenceValueFactory()
        }
    }, ;
    abstract fun toValueFactory(): proguard.evaluation.value.ValueFactory
}
