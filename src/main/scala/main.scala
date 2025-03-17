import scala.Option
import scala.io.Source
import scala.annotation.tailrec
import slick.jdbc.H2Profile.api._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@main def main(): Unit =
  // liste des trucs chargés
  val airports = readCsv("airports.csv", Airport.fromCsv)
  val countries = readCsv("countries.csv", Country.fromCsv)
  val runways = readCsv("runways.csv", Runway.fromCsv)

  val db = DatasetDB()
  db.populate(countries, runways, airports)
  println("Populating de la BDD OK")

  // Coder les requêtes dans DBQueries

  // TODO: FINIR LES QUERIES
  // val c = Await.result(DBQueries.fetchAirportsWithRunways(db), Duration.Inf)

  // println(c.length)

  db.close()
