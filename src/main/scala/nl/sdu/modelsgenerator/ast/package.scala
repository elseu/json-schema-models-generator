package nl.sdu.modelsgenerator

package object ast {
  def normalizeName(kebab: String): String = {
    val words = kebab.split("-").toList
    (words.head :: words.tail.map(_.capitalize)).mkString("")
  }
}
