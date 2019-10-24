package nl.sdu.modelsgenerator.ast

sealed trait Field extends Named {
  def stype: SchemaType

  def description: Option[String]
}

final case class PlainField(kebabCaseName: String,
                            stype: SchemaType,
                            description: Option[String])
    extends Field

final case class ObjectField(
    kebabCaseName: String,
    properties: Map[String, Field],
    definitions: Map[String, Field],
    requiredFields: Set[String],
    schema: Option[String],
    description: Option[String]
) extends Field {
  override val stype: SchemaType = SchemaObject
}

final case class ArrayField(kebabCaseName: String,
                            stype: SchemaType,
                            description: Option[String])
    extends Field
