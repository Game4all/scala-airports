import scala.concurrent.Future
import slick.lifted.Tag;
import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global

object DBQueries {

  /** Récupère la liste des aéroports et le pays qui matchent la requête (peut
    * être soit un code soit un bout de nom) + les pistes groupées par aéroport
    */
  def fetchCountryAirportsRunways(
      db: DatasetDB,
      query: String
  ): (Option[Country], List[(Airport, List[Runway])]) =
    // la query en MAJ si jamais il s'agit d'un code pays ISO
    val qUpper = query.toUpperCase()

    // on return du vide si on a rien
    val match_country: Country = db
      .executeSync(
        _.countries
          .filter(c => c.code === Some(qUpper) || c.name.like(f"%%$query%s%%"))
          .take(1)
          .result
      )
      .toList match
      case Nil       => return (None, Nil)
      case head :: _ => head

    // query pour recupérer les aeroports qui matchent le code ISO pays
    val ap_query =
      db.airports
        .filter(a =>
          a.iso_country === match_country.code.getOrElse("") || a.iso_region
            .like(qUpper)
        )

    // obligé de faire le groupBy hors du SQL car pour une raison obscure ca lâche une erreur
    val ap_runways =
      db.executeSync(_ =>
        ap_query
          .joinLeft(db.runways)
          .on((ap, ru) => ru.airportIdent.like(ap.ident))
          .sortBy((ap, ru) => ap.ident)
          .result
      ).toList
        .groupBy(_._1)
        .view
        .mapValues { values =>
          values.collect { case (_, Some(b)) => b }
        }
        .toMap
        .toList

    return (Some(match_country), ap_runways)
}
