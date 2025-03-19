import scala.Option
import scala.io.Source
import scala.io.StdIn
import scala.annotation.tailrec
import slick.jdbc.H2Profile.api._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@tailrec
def eval_loop(db: DatasetDB): Unit =
  println("")
  println("Entrez le nom du pays à chercher")
  print(">>>")

  val input = StdIn.readLine()

  // Coder les requêtes dans DBQueries

  val (pays, paires) = DBQueries.fetchCountryAirportsRunways(db, input)
  (pays, paires) match
    case (Some(p), _) => {
      println(
        f"===============================================================> ${paires.length} resultats pour ${p.name} (${p.code
            .getOrElse("??")})"
      )

      paires.foreach((ap, ru) => {
        println(f"=> ${ap.name} (${ap.ident}) - ${ru.length} pistes")
        ru.foreach(r => println(f"    - Piste ${r.id}"))
      })

    }
    case (None, _) => println("Pas de match dans la BDD :(")

  eval_loop(db)

@main def main(): Unit =
  val db = DatasetDB()
  db.populate(
    readCsv("countries.csv", Country.fromCsv),
    readCsv("runways.csv", Runway.fromCsv),
    readCsv("airports.csv", Airport.fromCsv)
  )

  // val top5 = DBQueries.fetchTopCountries(db, 15, _.desc)
  val lats = DBQueries.fetchMostCommonLatitudes(db, 15)
  println("==========> Top 15 latitudes de départ les plus communes")
  lats.foreach((lat) => {
    println(f"- ${lat}")
  })

  // eval_loop(db)

  db.close()
