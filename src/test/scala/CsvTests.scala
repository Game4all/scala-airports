class CsvTests extends munit.FunSuite {

//TODO:  ne marche pas car problème de path pour trouver les fichiers
//   test("CSV parsing works") {
//     val pays = readCsv("countries.csv", Country.fromCsv)
//     assert(pays.head.id == 302672) // Andorre
//   }

  test("parse_csv_line prend en compte les chaines de caractères echappées par un guillemet") {
    val ligne_test = "1 2, \" ZAZA, ZOZO, BOZO, \", 69LuffyLaBoulette"
    val parsed = readCsvLine(ligne_test.toList, ',', Nil, Nil, false)
    assert(parsed.length == 3)
  }
}
