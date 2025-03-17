import scala.Option
import scala.io.Source
import scala.annotation.tailrec

@main def main(): Unit =
  // liste des
  val airports = readCsv("airports.csv", Airport.fromCsv)
  val countries = readCsv("countries.csv", Country.fromCsv)
  val runways = readCsv("runways.csv", Runway.fromCsv)
  print(runways.head)
