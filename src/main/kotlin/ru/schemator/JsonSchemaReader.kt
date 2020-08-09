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
sealed class ArrayGenericParameter() {
    class Primitive(val primitiveName: String): ArrayGenericParameter()
    class Holder(val out: ArrayGenericParameter): ArrayGenericParameter()
}



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
        val props = (primitives + objects)
                .map {
                    val title = it.first
                    val value = it.second.asJsonObject
                    val type = value.type()
                    val innerDescription = value.descriptionNullable()

                    if (type == SchemaTypes.obj) {
                        // Objects should be added into recursion
                        objs.add(NameAndObjectPair(it.first.capitalize(), it.second))
                        ObjectPropertyMetadata(
                                propertyName = title,
                                isNullable = !rootRequired.contains(it.first),
                                comment = innerDescription,
                                objectTypeName = title.capitalize()
                        )
                    } else {
                        PrimitivePropertyMetadata(
                                propertyName = title,
                                dataType = SchemaTypes.toMetadataType(type),
                                isNullable = !rootRequired.contains(it.first),
                                comment = innerDescription
                        )
                    }
                }

        // Array support :
        // TODO: Support for 'contains' keyword (schema #6)
        // TODO: Support for arrays inside other arrays
        val arrayProps = arrays
                .map {
                    val title = it.first
                    val value = it.second.asJsonObject
                    val innerDescription = value.descriptionNullable()

                    val items = value.items() // TODO: Should also work  for 'contains' keyword for json schema 6
                    val typeOfItems = items.type()

                    // TODO: Support array inside array
                    val arrayTypeName =
                            if (SchemaTypes.isPrimitive(typeOfItems)) {
                                typeOfItems.capitalize()
                            } else {
                                val innerObjectTitle = (items.titleNullable() ?: "${title}Item").capitalize()

                                // Internal array objects should be added into recursion
                                objs.add(NameAndObjectPair(innerObjectTitle, items))
                                innerObjectTitle
                            }

                    ArrayPropertyMetadata(
                            propertyName = title,
                            isNullable = !rootRequired.contains(it.first),
                            comment = innerDescription,
                            arrayGenericParameter = ArrayGenericParameter.Primitive(primitiveName = arrayTypeName)
                    )
                }

        // A head of this recursion is generated here

        return GeneratableClassMetadata(
                className = rootName,
                description = rootDescription,
                propertyMetadata = props + arrayProps
        )
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

