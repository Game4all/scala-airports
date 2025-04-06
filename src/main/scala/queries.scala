import scala.concurrent.Future
import slick.lifted.Tag;
import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.lifted.ColumnOrdered
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DBQueries {

  /** Récupère la liste des aéroports et le pays qui matchent la requête (peut
    * être soit un code soit un bout de nom) + les pistes groupées par aéroport
    */
  def fetchCountryAirportsRunways(
      db: DatasetDB,
      query: String
  ): Future[(Option[Country], List[(Airport, List[Runway])])] = {
    // la query en MAJ si jamais il s'agit d'un code pays ISO
    val qUpper = query.toUpperCase()

    // un pays matchant le début de nom ou le CODE ISO spécifié.
    val match_country: Future[Option[Country]] = db
      .execute { q =>
        q.countries
          .filter(c => c.code === Some(qUpper) || c.name.like(f"%%$query%s%%"))
          .take(1)
          .result
      }
      .map(_.headOption)

    match_country
      .flatMap(matchCountry => {
        matchCountry match
          case None => Future.successful((None, Nil))

          case Some(matchCountry) =>
            // aeroports qui matchent le code pays
            val ap_query = (q: DBTables) =>
              q.airports
                .filter(a =>
                  a.iso_country === matchCountry.code.getOrElse(
                    ""
                  ) || a.iso_region
                    .like(qUpper)
                )

            // jointure aeroport x voies associees
            val ap_runways: Future[List[(Airport, List[Runway])]] = db
              .execute { q =>
                ap_query(q)
                  .joinLeft(q.runways)
                  .on((ap, ru) => ru.airport_ident === ap.ident)
                  .sortBy((ap, ru) => ap.ident)
                  .result
              }
              .map(
                apRunways =>
                  { // aggregation hors requête, peut être regarder pour le faire dans le SQL
                    apRunways.toList
                      .groupBy(_._1)
                      .view
                      .mapValues { values =>
                        values.collect { case (_, Some(b)) => b }
                      }
                      .toMap
                      .toList
                  }
              )

            // le resultat final
            ap_runways.map((Some(matchCountry), _))
      })
  }

  /** Recupère les pays avec le plus ou moins d'aéroports selon la condition
    * d'ordre de classement (_.asc ou _.desc)
    */
  def fetchTopCountries(
      db: DatasetDB,
      n_results: Int,
      ordering: Rep[Int] => ColumnOrdered[Int]
  ): Future[List[(Country, Int)]] =
    db
      .execute(f =>
        f.countries
          .join(f.airports)
          .on((c, a) => a.iso_country === c.code)
          .groupBy((c, a) => c)
          .map((c, a) => (c, a.length))
          .sortBy((c, a) => ordering(a))
          .take(n_results)
          .result
      )
      .map(_.toList)

  // TODO: Améliorer la perf de cette fonction, 2 min pour les resultats c chaud 💀
  /** Récupère les surfaces des pistes d'atterissages par pays WARNING: PREND
    * DEUX BONNES MINUTES
    */
  def fetchSurfaceTypesPerCountry(
      db: DatasetDB
  ): Future[List[(Country, List[Option[String]])]] =
    db
      .execute(f =>
        f.countries
          .join(f.airports)
          .on((c, a) => a.iso_country === c.code)
          .join(f.runways)
          .on((cu_ap, r) => r.airport_ident === cu_ap._2.ident)
          .map((cu_ap, r) => (cu_ap._1, r.surface))
          .distinctOn((cu, s) => (cu, s))
          .result
      )
      .map(
        _.toList
          .groupBy(_._1)
          .view
          .mapValues(_.map(_._2))
          .toList
      )

  /** Récupère les n latitudes de départ les plus communes.
    */
  def fetchMostCommonLatitudes(
      db: DatasetDB,
      n_results: Int
  ): Future[List[Option[String]]] =
    db.execute(q =>
      q.runways
        .groupBy(_.le_ident)
        .map((pos, g) => (pos, g.length))
        .sortBy((pos, g) => g.desc)
        .map((pos, g) => pos)
        .take(n_results)
        .result
    ).map(_.toList)

  /** Récupère les paires code pays - nom pays pour le dropdown On garde cette
    * requête en synchrone pour simplifier le chargement initial de l'interface
    */
  def fetchCountryDropdown(db: DatasetDB): List[(String, String)] =
    Await
      .result(
        db.execute(_.countries.map(c => (c.code, c.name)).result),
        Duration("5s")
      )
      .collect { case (Some(code), name) => (code, name) }
      .sorted
      .toList

}
