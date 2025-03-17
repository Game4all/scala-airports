import scala.concurrent.Future
import slick.lifted.Tag;
import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global

object DBQueries {
  /*
   * Retourne une sequence de tuples (aeroport, piste)
   */
//   def fetchAirportsWithRunwaysCode(
//       db: DatasetDB,
//       country_code: Option[String],
//   ): Future[Seq[(Airport, List[Runway])]] = {
//     // slick ne permet de faire des aggrégation comme ca ducoup on fait une requete SQL puis on regroupe avec scala
//     // val airportWithRunwaysQuery = db.airports
//     //   .filter(_. == country_code)
//     //   .joinLeft(db.runways)
//     //   .on(_.id === _.airportRef)
//     //   .result

//     // db.jdbc.run(airportWithRunwaysQuery).map { rows =>
//     //   rows
//     //     .groupBy(_._1)
//     //     .map { case (airport, groupedRows) =>
//     //       val runways = groupedRows.flatMap(_._2)
//     //       (airport, runways.toList)
//     //     }
//     //     .toSeq
//     // }
//   }
}
