package ru.schemator

import NotActionableSchemaException
import NotJsonSchemaException
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.StringReader

enum class DataTypes {
    string, integer, double, obj, datetime, date, array
}

/** Can also generate  */
class GeneratableClass(
        val className: String,
        val properties: List<GeneratableProperty>,
        val description: String? = null
)

class GeneratableProperty(
        val propertyName: String,
        val propertyDataType: DataTypes,
        val isNullable: Boolean,
        val objectTypeName: String? = null, // Only available for objects and arrays of objects
        val comment: String? = ""
) {
    fun isObj() = propertyDataType == DataTypes.obj
    fun isArray() = propertyDataType == DataTypes.array
}

// metadata is a list of generatable classes
class JsonSchemaMetadataOutput(
        val entries: List<GeneratableClass>
)

/** Create metedata by parsing json schema which can be used to generate code directly */
class MetadataReader(val jsonSchema: String, val launchArguments: LaunchArguments) {
    fun readSchema(): JsonSchemaMetadataOutput {

        // Read root object parameters
        val jsonElement = JsonParser.parseReader(StringReader(jsonSchema))
        if (!jsonElement.isJsonObject) {
            throw NotJsonSchemaException("Schema file is not json object -> can not be json schema")
        }

        val obj = jsonElement.asJsonObject
        if (obj.getAsJsonPrimitive(Schema.type).asString != SchemaTypes.obj) {
            TODO("Not implemented yet. Generating non-objects requires special handling for different json libs to parse correctly")
        }

        // Recursively generate properties
        val classes = listOfClasses(
                obj = obj,
                rootName = if (obj.has(Schema.title)) obj.getAsJsonPrimitive(Schema.title).asString else "RootObject"
        )

        return JsonSchemaMetadataOutput(
                entries = classes
        )
    }

    private fun listOfClasses(obj: JsonObject, rootName: String): List<GeneratableClass> {

        val rootDescription: String? = getTitle(obj)
        val rootRequired = getRequired(obj)

        val (primitives, arrays, objects) = splitObjectTypesByHandling(obj)
        val props = (primitives + objects)
                .map {
                    val title = it.first
                    val value = it.second.asJsonObject
                    val type = value.get(Schema.type).asJsonPrimitive.asString // TODO: safe get
                    val innerDescription = getTitle(value)

                    GeneratableProperty(
                            propertyName = title,
                            propertyDataType = SchemaTypes.toMetadataType(type),
                            isNullable = !rootRequired.contains(it.first),
                            comment = innerDescription,
                            objectTypeName = if (type == SchemaTypes.obj) title.capitalize() else null
                    )
                }

        // TODO: handle arrays... here

        // A head of this recursion is generated here
        val classFromRoot = GeneratableClass(
                className = rootName,
                description = rootDescription,
                properties = props
        )

        // TODO: Tail recursion.
        return objects.flatMap { listOfClasses(it.second, it.first.capitalize()) } + classFromRoot
    }



    private fun splitObjectTypesByHandling(obj: JsonObject): Triple<
            List<Pair<String,JsonObject>>,
            List<Pair<String,JsonObject>>,
            List<Pair<String,JsonObject>>
            >
    {

        val primitives = mutableListOf<Pair<String,JsonObject>>()
        val arrays = mutableListOf<Pair<String,JsonObject>>()
        val objects = mutableListOf<Pair<String, JsonObject>>()

        for (entry in getPropertiesFromObject(obj).entrySet()) {
            val type = entry.value.asJsonObject.get(Schema.type).asJsonPrimitive.asString
            val pair = entry.toPair()
            when(type) {
                SchemaTypes.obj -> objects.add(Pair(pair.first, pair.second.asJsonObject))
                SchemaTypes.array -> arrays.add(Pair(pair.first, pair.second.asJsonObject))
                SchemaTypes.number,
                SchemaTypes.string,
                SchemaTypes.integer -> primitives.add(Pair(pair.first, pair.second.asJsonObject))
                else -> throw NotJsonSchemaException("Type $type is not a valid Json Schema type")
            }
        }

        return Triple(primitives, arrays, objects)
    }

    private fun getRequired(obj: JsonObject): List<String> {
        return if (obj.has(Schema.required)) {
            val required = obj.get(Schema.required)
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
        val properties = obj.get(Schema.properties)
        if (!properties.isJsonObject) {
            throw NotJsonSchemaException("'properties' field MUST bu json object")
        } else {
            val asObject = properties.asJsonObject
            if (asObject.size() == 0) {
                throw NotActionableSchemaException("On of 'properties' fields contains no elements -> can not generate anything")
            } else {
                return asObject
            }
        }
    }

    private fun getTitle(obj: JsonObject): String? {
        return if (obj.has(Schema.description)) obj.getAsJsonPrimitive(Schema.description).asString else null
    }

}

