import scala.concurrent.Future
import slick.lifted.Tag;
import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.lifted.ColumnOrdered

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
    val ap_query = (q: DBTables) =>
      q.airports
        .filter(a =>
          a.iso_country === match_country.code.getOrElse("") || a.iso_region
            .like(qUpper)
        )

    // obligé de faire le groupBy hors du SQL car pour une raison obscure ca lâche une erreur
    val ap_runways =
      db.executeSync(q =>
        ap_query(q)
          .joinLeft(q.runways)
          .on((ap, ru) => ru.airport_ident.like(ap.ident))
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

    /*
     * Recupère les pays avec le plus ou moins d'aéroports selon la condition d'ordre de classement (_.asc ou _.desc)
     */
  def fetchTopCountries(
      db: DatasetDB,
      n_results: Int,
      ordering: Rep[Int] => ColumnOrdered[Int]
  ): List[(Country, Int)] =
    db
      .executeSync(f =>
        f.countries
          .join(f.airports)
          .on((c, a) => a.iso_country === c.code)
          .groupBy((c, a) => c)
          .map((c, a) => (c, a.length))
          .sortBy((c, a) => ordering(a))
          .take(n_results)
          .result
      )
      .toList

  // def fetchSurfaceTypesPerCountry(db: DatasetDB): List[(Country, List[Option[String]])] =


}
 