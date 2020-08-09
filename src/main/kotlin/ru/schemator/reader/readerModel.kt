package ru.schemator.reader


enum class PrimitiveDataTypes {
    string, integer, double, datetime, date
}

/** Can also generate  */
class GeneratableClassMetadata(
        val className: String,
        val propertyMetadata: List<GeneratablePropertyMetadata>,
        val description: String? = null
)


sealed class GeneratablePropertyMetadata(
        val propertyName: String,
        val isNullable: Boolean,
        val comment: String? = ""
)

class PrimitivePropertyMetadata(propertyName: String,
                                isNullable: Boolean,
                                comment: String? = "",
                                val dataType: PrimitiveDataTypes) : GeneratablePropertyMetadata(propertyName, isNullable, comment)

class ObjectPropertyMetadata(propertyName: String,
                             isNullable: Boolean,
                             comment: String? = "",
                             val objectTypeName: String) : GeneratablePropertyMetadata(propertyName, isNullable, comment)

class ArrayPropertyMetadata(propertyName: String,
                            isNullable: Boolean,
                            comment: String? = "",
                            val arrayGenericParameter: ArrayGenericParameter): GeneratablePropertyMetadata(propertyName, isNullable, comment)

/** Either primitive or array-inside-array parameter */
sealed class ArrayGenericParameter {
    class Primitive(val primitiveName: PrimitiveDataTypes): ArrayGenericParameter()
    class Obj(val objectName: String): ArrayGenericParameter()
    class Array(val out: ArrayGenericParameter): ArrayGenericParameter()
}

fun wrapArrayBase(iterations: Int, isObject: Boolean, primitiveName: PrimitiveDataTypes?, objName: String?): ArrayGenericParameter {
    val root = if (isObject) ArrayGenericParameter.Obj(objName!!) else ArrayGenericParameter.Primitive(primitiveName!!)
    return if (iterations == 0) {
        root
    } else {
        wrapArray(iterations - 1, ArrayGenericParameter.Array(root))
    }
}

fun wrapArray(iterations: Int, root: ArrayGenericParameter): ArrayGenericParameter {
    return if (iterations == 0) {
        return root
    } else wrapArray(iterations - 1, ArrayGenericParameter.Array(root))
}


// metadata is a list of generatable classes
class JsonSchemaMetadataOutput(
        val entries: List<GeneratableClassMetadata>
)
