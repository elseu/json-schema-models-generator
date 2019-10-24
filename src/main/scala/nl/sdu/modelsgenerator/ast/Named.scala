package nl.sdu.modelsgenerator.ast

trait Named {
  def kebabCaseName: String
  def lowerCaseName: String = normalizeName(kebabCaseName)
  def upperCaseName: String = lowerCaseName.capitalize
}
