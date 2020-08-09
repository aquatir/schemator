package ru.schemator.reader

import SchemaValidationException
import NotJsonSchemaException
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import ru.schemator.LaunchArguments
import ru.schemator.SchemaTypes
import ru.schemator.descriptionNullable
import ru.schemator.items
import ru.schemator.properties
import ru.schemator.requiredNullable
import ru.schemator.titleNullable
import ru.schemator.type
import java.io.StringReader
import java.util.*


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

        val objects = objectToListOfPairs(obj)
        val generatabableObjects = handleObjects(objects, objs, rootRequired)

        return GeneratableClassMetadata(
                className = rootName,
                description = rootDescription,
                propertyMetadata = generatabableObjects
        )
    }

    private fun handleObjects(primAndObj: List<Pair<String, JsonObject>>, objs: Queue<NameAndObjectPair>, required: List<String>): List<GeneratablePropertyMetadata> {
        return primAndObj.map {
            val title = it.first
            val value = it.second.asJsonObject
            val type = value.type()
            val innerDescription = value.descriptionNullable()

            if (SchemaTypes.isObject(type)) {
                // Objects should be added into recursion
                objs.add(NameAndObjectPair(it.first.capitalize(), it.second))
                ObjectPropertyMetadata(
                        propertyName = title,
                        isNullable = !required.contains(it.first),
                        comment = innerDescription,
                        objectTypeName = title.capitalize()
                )
            } else if (SchemaTypes.isPrimitive(type)) {
                PrimitivePropertyMetadata(
                        propertyName = title,
                        dataType = SchemaTypes.toMetadataType(type),
                        isNullable = !required.contains(it.first),
                        comment = innerDescription
                )
            } else {
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
                                        val innerInternalObjectTitle = (internalItems.titleNullable()
                                                ?: "${title}Item").capitalize()
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
    }


    /** Get all root properties of object as list of pairs.
     * Used to split them by type: see git log if required*/
    private fun objectToListOfPairs(obj: JsonObject):
            List<Pair<String, JsonObject>> {

        return getPropertiesFromObject(obj).entrySet().map {
            val pair = it.toPair()
            Pair(pair.first, pair.second.asJsonObject)
        }
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

