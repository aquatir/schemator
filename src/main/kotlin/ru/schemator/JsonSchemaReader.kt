package ru.schemator

import SchemaValidationException
import NotJsonSchemaException
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.StringReader
import java.util.*

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

//3 -> ArrayGenericParameter.Array(
//ArrayGenericParameter.Array (
//ArrayGenericParameter.Array(if (isObject) ArrayGenericParameter.Obj(objectName!!) else ArrayGenericParameter.Primitive(primitiveName!!))
//)
//)



// metadata is a list of generatable classes
class JsonSchemaMetadataOutput(
        val entries: List<GeneratableClassMetadata>
)

/** Create metedata by parsing json schema which can be used to generate code directly */
class JsonSchemaReader(val jsonSchema: String, val launchArguments: LaunchArguments) {
    fun readSchema(): JsonSchemaMetadataOutput {

        // Read root object parameters
        val jsonElement = JsonParser.parseReader(StringReader(jsonSchema))
        if (!jsonElement.isJsonObject) {
            throw NotJsonSchemaException("Schema file is not json object -> can not be json schema")
        }

        val obj = jsonElement.asJsonObject
        if (obj.type() != SchemaTypes.obj) {
            TODO("Not implemented yet. Generating non-objects requires special handling for different json libs to parse correctly")
        }

        return JsonSchemaMetadataOutput(generateClasses(obj))
    }


    data class NameAndObjectPair(val name: String, val obj: JsonObject)

    private fun generateClasses(obj: JsonObject): List<GeneratableClassMetadata> {

        val rootName = obj.titleNullable() ?: "RootObject"
        val queue = ArrayDeque<NameAndObjectPair>()
                .apply { this.offer(NameAndObjectPair(rootName, obj)) }

        /**
         * Tail recursion to read Json Schema tree
         * [accum] -> on each step stores current generated classes
         * [objs] -> on each step stores objects which should be traversed. Works as queue so essentially json schema is traversed with BFS.
         * On each step new objects may be added here by calling [oneClass]
         */
        tailrec fun generateClasses(accum: MutableList<GeneratableClassMetadata> = mutableListOf(), objs: Queue<NameAndObjectPair>): List<GeneratableClassMetadata> {
            return if (objs.isEmpty())
                accum
            else {
                val firstOnQueue = objs.remove()
                accum.add(
                        oneClass(
                                obj = firstOnQueue.obj,
                                objs = objs,
                                rootName = firstOnQueue.name
                        )
                )

                return generateClasses(accum, objs)
            }
        }

        return generateClasses(mutableListOf(), queue)
    }


    /**
     * Generate a single class from this [JsonObject] only. Apply any children objects into [objs]. Return generated class
     */
    private fun oneClass(obj: JsonObject, objs: Queue<NameAndObjectPair>, rootName: String): GeneratableClassMetadata {

        val rootDescription: String? = obj.descriptionNullable()
        val rootRequired = getRequired(obj)

        val (primitives, arrays, objects) = splitObjectTypesByHandling(obj)

        val primsAndObjects = handlePrimitiveAndObject((primitives + objects), objs, rootRequired)
        val arrayProps = handleArrays(arrays, objs, rootRequired)

        // A head of this recursion is generated here

        return GeneratableClassMetadata(
                className = rootName,
                description = rootDescription,
                propertyMetadata = primsAndObjects + arrayProps
        )
    }

    private fun handlePrimitiveAndObject(primAndObj: List<Pair<String, JsonObject>>, objs: Queue<NameAndObjectPair>, required: List<String>): List<GeneratablePropertyMetadata> {
        return primAndObj.map {
            val title = it.first
            val value = it.second.asJsonObject
            val type = value.type()
            val innerDescription = value.descriptionNullable()

            if (type == SchemaTypes.obj) {
                // Objects should be added into recursion
                objs.add(NameAndObjectPair(it.first.capitalize(), it.second))
                ObjectPropertyMetadata(
                        propertyName = title,
                        isNullable = !required.contains(it.first),
                        comment = innerDescription,
                        objectTypeName = title.capitalize()
                )
            } else {
                PrimitivePropertyMetadata(
                        propertyName = title,
                        dataType = SchemaTypes.toMetadataType(type),
                        isNullable = !required.contains(it.first),
                        comment = innerDescription
                )
            }
        }
    }

    // Array support :
    // TODO: Support for 'contains' keyword (schema #6)
    /** Return a list of generatable properties for arrays.
     * Arrays are different from primitives and object because they can be nested inside one another */
    private fun handleArrays(primAndObj: List<Pair<String, JsonObject>>, objs: Queue<NameAndObjectPair>, required: List<String>): List<ArrayPropertyMetadata> {
        return primAndObj.map {
            val title = it.first
            val value = it.second.asJsonObject
            val innerDescription = value.descriptionNullable()

            val items = value.items() // TODO: Should also work  for 'contains' keyword for json schema 6
            val typeOfItems = items.type()

            ArrayPropertyMetadata(
                    propertyName = title,
                    isNullable = !required.contains(it.first),
                    comment = innerDescription,
                    arrayGenericParameter = when {
                        SchemaTypes.isPrimitive(typeOfItems) -> {
                            ArrayGenericParameter.Primitive(primitiveName = PrimitiveDataTypes.valueOf(typeOfItems))
                        }
                        SchemaTypes.isObject(typeOfItems) -> {
                            val innerObjectTitle = (items.titleNullable() ?: "${title}Item").capitalize()
                            objs.add(NameAndObjectPair(innerObjectTitle, items)) // Internal array objects should be added into recursion
                            ArrayGenericParameter.Obj(objectName = innerObjectTitle)
                        }
                        else -> { // handling internal array
                            var internalArrayCount = 1
                            var primitiveName: PrimitiveDataTypes? = null
                            var objectName: String? = null
                            var loopItems = items.items()
                            while (true) {
                                val internalItems = loopItems
                                val typeOfInternalItems = internalItems.type()
                                if (SchemaTypes.isPrimitive(typeOfInternalItems)) {
                                    primitiveName = PrimitiveDataTypes.valueOf(typeOfInternalItems)
                                    break
                                } else if (SchemaTypes.isObject(typeOfInternalItems)) {
                                    val innerInternalObjectTitle = (internalItems.titleNullable() ?: "${title}Item").capitalize()
                                    objs.add(NameAndObjectPair(innerInternalObjectTitle, internalItems)) // Internal array objects should be added into recursion
                                    objectName = innerInternalObjectTitle
                                    break
                                } else {
                                    internalArrayCount++
                                    loopItems = internalItems.items()
                                }
                            }
                            val isObject = objectName != null
                            wrapArrayBase(internalArrayCount, isObject, primitiveName, objectName)
                        }
                    }
            )
        }
    }


    private fun splitObjectTypesByHandling(obj: JsonObject): Triple<
            List<Pair<String, JsonObject>>,
            List<Pair<String, JsonObject>>,
            List<Pair<String, JsonObject>>
            > {

        val primitives = mutableListOf<Pair<String, JsonObject>>()
        val arrays = mutableListOf<Pair<String, JsonObject>>()
        val objects = mutableListOf<Pair<String, JsonObject>>()

        for (entry in getPropertiesFromObject(obj).entrySet()) {
            val type = entry.value.asJsonObject.type()
            val pair = entry.toPair()
            when (type) {
                SchemaTypes.obj -> objects.add(Pair(pair.first, pair.second.asJsonObject))
                SchemaTypes.array -> arrays.add(Pair(pair.first, pair.second.asJsonObject))
                SchemaTypes.number,
                SchemaTypes.string,
                SchemaTypes.datetime,
                SchemaTypes.date,
                SchemaTypes.integer -> primitives.add(Pair(pair.first, pair.second.asJsonObject))
                else -> throw NotJsonSchemaException("Type $type is not a valid Json Schema type")
            }
        }

        return Triple(primitives, arrays, objects)
    }

    private fun getRequired(obj: JsonObject): List<String> {
        val required = obj.requiredNullable()
        return if (required != null) {
            if (!required.isJsonArray) {
                throw NotJsonSchemaException("'required' field MUST be json array")
            } else {
                required.asJsonArray
                        .map { it.asString }
                        .toList()
            }
        } else {
            listOf()
        }
    }

    private fun getPropertiesFromObject(obj: JsonObject): JsonObject {
        val properties = obj.properties()
        if (!properties.isJsonObject) {
            throw NotJsonSchemaException("'properties' field MUST bu json object")
        } else {
            val asObject = properties.asJsonObject
            if (asObject.size() == 0) {
                throw SchemaValidationException("On of 'properties' fields contains no elements -> can not generate anything")
            } else {
                return asObject
            }
        }
    }
}

