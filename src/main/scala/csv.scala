import scala.Option
import scala.io.Source
import scala.annotation.tailrec

/** Lit un fichier CSV et retourne la lignes lues sous forme de classe
  * représentant une ligne du fichier
  */
def readCsv[T](filename: String, f: List[String] => T): List[T] =
  // on lit les lignes du fichier en question
  // avec un tail pour virer les entêtes de colonnes
  val lines = Source.fromFile(filename, "UTF-8").getLines().toList.tail

  @tailrec
  def read_csv_line(lines: List[String], processed: List[T]): List[T] =
    lines match
      case Nil => return processed.reverse
      case head :: tail => {
        val values = readCsvLine(head.toList, ',', Nil, Nil, false)
        // val values = head.split(",").toList
        val item = f(values)
        read_csv_line(tail, item :: processed)
      }

  return read_csv_line(lines, Nil)

/** Parse une ligne du CSV en prenant en compte ces vicieuses chaines de
  * caractères échappées avec des guillements.
  * Les regex c plus simple mais va m'écrire ca sans chat...
  */
@tailrec
def readCsvLine(
    line: List[Char],
    sep: Char,
    splits: List[String],
    curr_item: List[Char],
    in_str: Boolean
): List[String] =
  line match
    case Nil => return (curr_item.mkString :: splits).reverse
    case head :: tail => {
      if (head == '"') {
        readCsvLine(tail, sep, splits, curr_item, !in_str)
      } else if (head != sep || in_str) {
        readCsvLine(tail, sep, splits, curr_item.appended(head), in_str)
      } else {
        readCsvLine(tail, sep, curr_item.mkString :: splits, Nil, in_str)
      }
    }
