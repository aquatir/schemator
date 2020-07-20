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
        val comment: String? = ""
)

// metadata is a list of generatable classes
class JsonSchemaMetadataOutput(
        val entries: List<GeneratableClass>
)

/** Create metedata by parsing json schema which can be used to generate code directly */
class MetadataReader(val jsonSchema: String, val launchArguments: LaunchArguments) {
    fun readSchema(): JsonSchemaMetadataOutput {

        val jsonElement = JsonParser.parseReader(StringReader(jsonSchema))
        if (!jsonElement.isJsonObject) {
            throw NotJsonSchemaException("Schema file is not json object -> can not be json schema")
        }

        val obj = jsonElement.asJsonObject

        if (obj.getAsJsonPrimitive(Schema.type).asString != SchemaTypes.obj) {
            TODO("Not implemented yet. Generating non-objects requires special handling for different json libs to parse correctly")
        }

        // Recursive loop should start here
        val objName = if (obj.has(Schema.title)) obj.getAsJsonPrimitive(Schema.title).asString else "RootObject"
        val description: String? = getTitle(obj)
        val required = getRequired(obj)

        // TODO: Make recursive parsing
        val props = getPropertiesFromObject(obj).entrySet()
                .map {
                    val title = it.key
                    val asObj = it.value.asJsonObject
                    val type = asObj.get(Schema.type).asJsonPrimitive.asString // TODO: safe get
                    val innerDescription = getTitle(asObj)

                    GeneratableProperty(
                            propertyName = title,
                            propertyDataType = SchemaTypes.toMetadataType(type),
                            isNullable = !required.contains(it.key),
                            comment = innerDescription
                    )

                }

        return JsonSchemaMetadataOutput(
                entries = listOf(GeneratableClass(
                        className = objName,
                        properties = props,
                        description = description
                ))
        )
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

