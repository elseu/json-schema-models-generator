package nl.sdu.modelsgenerator.ast

sealed trait SchemaType

case object SchemaObject extends SchemaType
case object SchemaString extends SchemaType
case object SchemaBoolean extends SchemaType
case object SchemaInteger extends SchemaType
case class SchemaArray(elementType: SchemaType) extends SchemaType
case class SchemaReference(reference: String) extends SchemaType with Named {
  override val kebabCaseName: String = reference.split("/").last
}
