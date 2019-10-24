package nl.sdu.modelsgenerator.builder

import java.io.{File, FileOutputStream}

import nl.sdu.modelsgenerator.ast._
import nl.sdu.modelsgenerator.parser.SchemaParser
import play.api.libs.json.{JsObject, Json}

import scala.io.Source

object ScalaBuilder extends App {
  private def build(asProperty: Boolean = false, required: Boolean = true)(
      field: Field): String = field match {
    case pf: PlainField  => buildPlainField(pf, asProperty, required)
    case of: ObjectField => buildObjectField(of, asProperty, required)
    case af: ArrayField  => buildArrayField(af, required)
  }

  private def buildPlainField(plainField: PlainField,
                              asProperty: Boolean,
                              required: Boolean): String = {
    val comment =
      s"${plainField.kebabCaseName}${plainField.description.map(d => s": $d").getOrElse("")}"

    if (asProperty) {
      if (required)
        s"// ${comment}\n${avoidKeywords(plainField.lowerCaseName)}: ${buildSchemaType(plainField.stype)}"
      else
        s"// ${comment}\n${avoidKeywords(plainField.lowerCaseName)}: Option[${buildSchemaType(plainField.stype)}]"
    } else
      s"// ${comment}\ntype ${plainField.upperCaseName} = ${buildSchemaType(plainField.stype)}"
  }

  private def buildObjectField(objectField: ObjectField,
                               asProperty: Boolean,
                               required: Boolean): String = {
    if (asProperty) {
      if (required)
        s"/* ${objectField.kebabCaseName}${objectField.description
          .map(d => s": ${d}")
          .getOrElse("")} */ ${objectField.lowerCaseName}: ${objectField.upperCaseName}"
      else
        s"/* ${objectField.kebabCaseName}${objectField.description
          .map(d => s": ${d}")
          .getOrElse("")} */ ${objectField.lowerCaseName}: Option[${objectField.upperCaseName}]"
    } else {
//      Figure out how to reference definitions which are just strings
      val defs = objectField.definitions.values
      val definitions = defs.map(build(false))

      val nestedObjects = objectField.properties.values
        .collect {
          case of: ObjectField => of
        }
        .map(build())

      val properties =
        objectField.properties.values.map(prop =>
          build(true, objectField.requiredFields(prop.kebabCaseName))(prop))

      val description =
        objectField.description.map(d => s": ${d}").getOrElse("")
      val comment = s"// ${objectField.kebabCaseName}${description}"
      val fields = properties.mkString("(\n  ", ",\n  ", "\n)")
      val format =
        if (properties.size <= 22)
          s"implicit val format: Format[${objectField.upperCaseName}] = Json.format[${objectField.upperCaseName}]"
        else
          s"implicit val format: Format[${objectField.upperCaseName}] = Jsonx.formatCaseClass[${objectField.upperCaseName}]"

      val companionObject =
        if (properties.nonEmpty) {
          s"\n\nobject ${objectField.upperCaseName} {\n  ${format}\n}"
        } else {
          s"""

          |object ${objectField.upperCaseName} {
          |  implicit val format: Format[${objectField.upperCaseName}] = new Format[${objectField.upperCaseName}] {
          |    override def reads(json: JsValue): JsResult[${objectField.upperCaseName}] =
          |      JsSuccess(${objectField.upperCaseName}())

          |    override def writes(o: ${objectField.upperCaseName}): JsValue =
          |      JsObject(Map[String, JsValue]())
          |  }
          |}""".stripMargin
        }

      val caseClass =
        s"${comment}\ncase class ${objectField.upperCaseName} ${fields}"

      s"${definitions.mkString("\n")}\n${nestedObjects.mkString("\n")}\n${caseClass}${companionObject}"
    }
  }

  private def buildArrayField(arrayField: ArrayField,
                              required: Boolean): String = {
    if (required)
      s"${avoidKeywords(arrayField.lowerCaseName)}: Seq[${buildSchemaType(arrayField.stype)}]"
    else
      s"${avoidKeywords(arrayField.lowerCaseName)}: Option[Seq[${buildSchemaType(arrayField.stype)}]]"

  }

  private def avoidKeywords(name: String): String = name match {
    case "type"     => "`type`"
    case "abstract" => "`abstract`"
    case _          => name
  }
  private def buildSchemaType(schemaType: SchemaType): String =
    schemaType match {
      case SchemaObject  => s"Nothing"
      case SchemaString  => "String"
      case SchemaBoolean => "Boolean"
      case SchemaInteger => "Integer"
      case SchemaArray(elementType) =>
        s"List[${buildSchemaType(elementType)}]"
      case sr: SchemaReference =>
        s"${sr.upperCaseName}"
    }

  private def writeToFile(f: File, s: String): Unit = {
    if (f.exists()) f.delete()
    f.createNewFile()
    if (!f.canWrite)
      throw new IllegalArgumentException(s"Can’t write to file: $f")

    val fos = new FileOutputStream(f)

    try {
      fos.write(s.getBytes("UTF-8"))
    } finally {
      fos.close()
    }
  }

  private val source = Source
//    .fromFile("/Users/paco/Downloads/gaston-index-json-schema.json")
    .fromFile("../schema/json/sws-metadata/sws-metadata.json")
    .mkString("")
  private val obj = Json.parse(source).as[JsObject]

  private val schema: ObjectField = SchemaParser.parseObject("metadata", obj)

  private val imports =
    """import play.api.libs.json.{Format, JsObject, JsResult, JsSuccess, JsValue, Json}
      |import ai.x.play.json.Jsonx

      |""".stripMargin

  private val output: String =
    s"${imports}object Schema {\n${build()(schema)}\n}"
  writeToFile(new File("/tmp/Foo.scala"), output)
}
