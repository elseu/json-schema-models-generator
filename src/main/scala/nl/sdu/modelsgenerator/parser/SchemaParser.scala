package nl.sdu.modelsgenerator.parser

import nl.sdu.modelsgenerator.ast.{
  ArrayField,
  Field,
  ObjectField,
  PlainField,
  SchemaArray,
  SchemaBoolean,
  SchemaInteger,
  SchemaObject,
  SchemaReference,
  SchemaString,
  SchemaType
}
import play.api.libs.json.{JsObject, Json}

object SchemaParser {
  def parseObject(kebabCaseName: String, js: JsObject): ObjectField = {
    def parse(js: Option[JsObject]): Map[String, Field] = {
      js.map(
          _.fields.map {
            case (kebabCaseFieldName, jso: JsObject) =>
              kebabCaseFieldName -> parseSchema(kebabCaseFieldName, jso)
            case (name, jso) =>
              throw new IllegalArgumentException(
                s"Object: [$name] not an object: [${Json.prettyPrint(jso)}]")
          }.toMap
        )
        .getOrElse(Map())
    }

    val definitions: Map[String, Field] = parse(
      (js \ "definitions").asOpt[JsObject])

    val properties: Map[String, Field] = parse(
      (js \ "properties").asOpt[JsObject])

    val requiredFields: List[String] =
      (js \ "required").asOpt[List[String]].getOrElse(List())

    val schema: Option[String] = (js \ "$schema").asOpt[String]

    ObjectField(kebabCaseName,
                properties,
                definitions,
                requiredFields.toSet,
                schema,
                getDescription(js))
  }

  def parsePlainField(name: String,
                      schemaType: SchemaType,
                      js: JsObject): PlainField = {
    val description = getDescription(js)

    PlainField(name, schemaType, description)
  }

  def parseArrayField(name: String,
                      schemaType: SchemaType,
                      js: JsObject): ArrayField = {
    val items = (js \ "items").as[JsObject]
    val elementSchemaType = getSchemaType(items)
    val description = getDescription(js)

    ArrayField(name, elementSchemaType, description)
  }

  def parseSchema(name: String, js: JsObject): Field = {
    val schemaType = getSchemaType(js)

    schemaType match {
      case SchemaObject =>
        parseObject(name, js)

      case SchemaString | SchemaBoolean | SchemaInteger =>
        parsePlainField(name, schemaType, js)

      case schemaArray: SchemaArray =>
        parseArrayField(name, schemaArray.elementType, js)

      case SchemaReference(_) =>
        parsePlainField(name, schemaType, js)
    }
  }

  private def getDescription(js: JsObject): Option[String] =
    (js \ "description").asOpt[String]

  private def getSchemaType(js: JsObject): SchemaType = {
    val typeValue = (js \ "type")
    val refValue = (js \ "$ref")

    if (typeValue.isDefined)
      typeValue.as[String] match {
        case "string"  => SchemaString
        case "boolean" => SchemaBoolean
        case "integer" => SchemaInteger
        case "array" =>
          val elementType = getSchemaType((js \ "items").as[JsObject])
          SchemaArray(elementType)
        case "object" =>
          SchemaObject
        case _ =>
          throw new IllegalArgumentException(
            s"Unknown type in type field in object [$js]")
      } else if (refValue.isDefined)
      SchemaReference(refValue.as[String])
    else
      throw new IllegalArgumentException(s"No type in object [$js]")
  }
}
