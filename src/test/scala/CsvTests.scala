class CsvTests extends munit.FunSuite {

//TODO:  ne marche pas car problème de path pour trouver les fichiers
//   test("CSV parsing works") {
//     val pays = readCsv("countries.csv", Country.fromCsv)
//     assert(pays.head.id == 302672) // Andorre
//   }

  test(
    "parse_csv_line prend en compte les chaines de caractères echappées par un guillemet"
  ) {
    val ligne_test = "1 2, \" ZAZA, ZOZO, BOZO, \", 69LuffyLaBoulette"
    val parsed = readCsvLine(ligne_test.toList, ',', Nil, Nil, false)
    assert(parsed.length == 3)
  }

  test("Le parsing d'un aeroport depuis une ligne de CSV fonctionne") {
    val ligne =
      "6523,\"00A\",\"heliport\",\"Total Rf Heliport\",40.07080078125,-74.93360137939453,11,\"NA\",\"US\",\"US-PA\",\"Bensalem\",\"no\",\"00A\",,\"00A\",,,"
    val modele =
      Airport.fromCsv(readCsvLine(ligne.toList, ',', Nil, Nil, false))
    assertEquals(modele.id, 6523)
  }

  test("Le parsing d'un pays depuis une ligne de CSV fonctionne") {
    val ligne =
      "302618,\"AE\",\"United Arab Emirates\",\"AS\",\"http://en.wikipedia.org/wiki/United_Arab_Emirates\",\"UAE\""
    val modele =
      Country.fromCsv(readCsvLine(ligne.toList, ',', Nil, Nil, false))
    assertEquals(modele.id, 302618)
    assertEquals(modele.keywords, Some("UAE".toString))
    
  }

  test("Le parsing d'une piste depuis une ligne de CSV fonctionne") {
    val ligne = "269408,6523,\"00A\",80,80,\"ASPH-G\",1,0,\"H1\",,,,,,,,,,,";
    val modele =
      Runway.fromCsv(readCsvLine(ligne.toList, ',', Nil, Nil, false))
    assertEquals(modele.id, 269408)
  }
}
