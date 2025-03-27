import slick.lifted.Tag;
import slick.jdbc.H2Profile.api._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import javax.xml.crypto.Data

/** Base de données du dataset de l'aéroport
  */
class DatasetDB {
  private val CONNECTION_STRING = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"

  // la DB mise en privé pour forcer de passer par les tables slick
  private val jdbc = Database.forURL(
    url = CONNECTION_STRING,
    driver = "org.h2.Driver",
    keepAliveConnection = true
  )

  private val tables = DBTables()

  /** Remplit la BDD avec les collections de modèles passées en paramètres.
    */
  def populate(c: List[Country], r: List[Runway], a: List[Airport]): Unit =
    Await.result(
      Future.sequence(
        Seq(
          this.execute(_.airports ++= a),
          this.execute(_.runways ++= r),
          this.execute(_.countries ++= c)
        )
      ),
      Duration(10, "s")
    )

  /** Execute la requête slick passée en paramètres
    */
  def execute[B](f: DBTables => DBIO[B]): Future[B] =
    jdbc.run(f(this.tables))

  /// Initialise la BDD.
  private def init(): Unit = {
    Await.result(
      Future.sequence(
        Seq(
          this.execute(_.airports.schema.create),
          this.execute(_.runways.schema.create),
          this.execute(_.countries.schema.create)
        )
      ),
      Duration(10, "s")
    )
  }

  init()

  /** Ferme la connexion existante a la BDD en mémoire.
    */
  def close(): Unit =
    jdbc.close()
}

/**
 * Liste les tables de la BDD
 **/
class DBTables {
  /*
   * Aéroports
   */
  val airports: TableQuery[AirportTable] = TableQuery[AirportTable]
  /*
   * Runways aka piste d'atterissage
   */
  val runways: TableQuery[RunwaysTable] = TableQuery[RunwaysTable]
  /*
   * Pays
   */
  val countries: TableQuery[CountryTable] = TableQuery[CountryTable]
}

// ==================================================================== Tables BDD ==============================================

class AirportTable(tag: Tag) extends Table[Airport](tag, "airports") {
  def id = column[Int]("id", O.PrimaryKey)
  def ident = column[String]("ident")
  def `type` = column[String]("type")
  def name = column[String]("name")
  def latitude_deg = column[Double]("latitude_deg")
  def longitude_deg = column[Double]("longitude_deg")
  def elevation_ft = column[Option[Int]]("elevation_ft")
  def continent = column[Option[String]]("continent")
  def iso_country = column[Option[String]]("iso_country")
  def iso_region = column[String]("iso_region")
  def municipality = column[Option[String]]("municipality")
  def scheduled_service = column[String]("scheduled_service")
  def gps_code = column[Option[String]]("gps_code")
  def iata_code = column[Option[String]]("iata_code")
  def local_code = column[Option[String]]("local_code")
  def home_Link = column[Option[String]]("home_link")
  def wikipedia_link = column[Option[String]]("wikipedia_link")
  def keywords = column[Option[String]]("keywords")

  def * = (
    id,
    ident,
    `type`,
    name,
    latitude_deg,
    longitude_deg,
    elevation_ft,
    continent,
    iso_country,
    iso_region,
    municipality,
    scheduled_service,
    gps_code,
    iata_code,
    local_code,
    home_Link,
    wikipedia_link,
    keywords
  ) <> (Airport.apply.tupled, Airport.unapply)
}

// Countries

class CountryTable(tag: Tag) extends Table[Country](tag, "countries") {
  def id = column[Int]("id", O.PrimaryKey)
  def code = column[Option[String]]("code")
  def name = column[String]("name")
  def continent = column[Option[String]]("continent")
  def wikipedia_link = column[Option[String]]("wikipedia_link")
  def keywords = column[Option[String]]("keywords")

  def * = (
    id,
    code,
    name,
    continent,
    wikipedia_link,
    keywords
  ) <> (Country.apply.tupled, Country.unapply)
}

// Runways

class RunwaysTable(tag: Tag) extends Table[Runway](tag, "runways") {
  def id = column[Int]("id", O.PrimaryKey)
  def airport_ref = column[Int]("airport_ref")
  def airport_ident = column[String]("airport_ident")
  def length_ft = column[Option[Int]]("length_ft")
  def width_ft = column[Option[Int]]("width_ft")
  def surface = column[Option[String]]("surface")
  def lighted = column[Int]("lighted")
  def closed = column[Int]("closed")
  def le_ident = column[Option[String]]("le_ident")
  def le_latitude_deg = column[Option[Double]]("le_latitude_deg")
  def le_longitude_deg = column[Option[Double]]("le_longitude_deg")
  def le_elevation_ft = column[Option[Int]]("le_elevation_ft")
  def le_heading_degT = column[Option[Double]]("le_heading_degT")
  def le_displaced_threshold_ft =
    column[Option[Int]]("le_displaced_threshold_ft")
  def he_ident = column[Option[String]]("he_ident")
  def he_latitude_deg = column[Option[Double]]("he_latitude_deg")
  def he_longitude_deg = column[Option[Double]]("he_longitude_deg")
  def he_elevation_ft = column[Option[Int]]("he_elevation_ft")
  def he_heading_degT = column[Option[Double]]("he_heading_degT")
  def he_displaced_threshold_ft =
    column[Option[Int]]("he_displaced_threshold_ft")

  // Map columns to the case class
  def * = (
    id,
    airport_ref,
    airport_ident,
    length_ft,
    width_ft,
    surface,
    lighted,
    closed,
    le_ident,
    le_latitude_deg,
    le_longitude_deg,
    le_elevation_ft,
    le_heading_degT,
    le_displaced_threshold_ft,
    he_ident,
    he_latitude_deg,
    he_longitude_deg,
    he_elevation_ft,
    he_heading_degT,
    he_displaced_threshold_ft
  ) <> (Runway.apply.tupled, Runway.unapply)
}
