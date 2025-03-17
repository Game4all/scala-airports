import scala.Option

// Fonctions utilisées pour parse des valeurs optionnelles
private def parseOptionalInt(value: String): Option[Int] =
  if (value.isEmpty) None else Some(value.toInt)

private def parseOptionalString(value: String): Option[String] =
  if (value.isEmpty) None else Some(value)

private def parseOptionalDouble(value: String): Option[Double] =
  if (value.isEmpty) None else Some(value.toDouble)

// Représente un aéroport
case class Airport(
    id: Int,
    ident: String,
    `type`: String,
    name: String,
    latitude_deg: Double,
    longitude_deg: Double,
    elevation_ft: Option[Int],
    continent: Option[String],
    iso_country: Option[String],
    iso_region: String,
    municipality: Option[String],
    scheduled_service: String,
    gps_code: Option[String],
    iata_code: Option[String],
    local_code: Option[String],
    home_link: Option[String],
    wikipedia_link: Option[String],
    keywords: Option[String]
)

object Airport {
  def fromCsv(line: List[String]): Airport = {

    Airport(
      id = line(0).toInt,
      ident = line(1),
      `type` = line(2),
      name = line(3),
      latitude_deg = line(4).toDouble,
      longitude_deg = line(5).toDouble,
      elevation_ft = parseOptionalInt(line(6)),
      continent = parseOptionalString(line(7)),
      iso_country = parseOptionalString(line(8)),
      iso_region = line(9),
      municipality = parseOptionalString(line(10)),
      scheduled_service = line(11),
      gps_code = parseOptionalString(line(12)),
      iata_code = parseOptionalString(line(13)),
      local_code = parseOptionalString(line(14)),
      home_link = parseOptionalString(line(15)),
      wikipedia_link = parseOptionalString(line(16)),
      keywords = parseOptionalString(line(17))
    )
  }
}

// ====================================================== Pays ====================================================

// Représente un pays.
case class Country(
    id: Int,
    code: Option[String],
    name: String,
    continent: Option[String],
    wikipedia_link: Option[String],
    keywords: Option[String]
)

object Country {
  def fromCsv(line: List[String]): Country = {
    Country(
      id = line(0).toInt,
      code = parseOptionalString(line(1)),
      name = line(2).replace("\"", ""),
      continent = parseOptionalString(line(3)),
      wikipedia_link = parseOptionalString(line(4)),
      keywords = parseOptionalString(line(5))
    )
  }
}

// =========================================================== Piste d'atterissage ========================================

// Représente une piste d'atterissage
case class Runway(
    id: Int,
    airport_ref: Int,
    airport_ident: String,
    length_ft: Option[Int],
    width_ft: Option[Int],
    surface: Option[String],
    lighted: Int,
    closed: Int,
    le_ident: Option[String],
    le_latitude_deg: Option[Double],
    le_longitude_deg: Option[Double],
    le_elevation_ft: Option[Int],
    le_heading_degT: Option[Double],
    le_displaced_threshold_ft: Option[Int],
    he_ident: Option[String],
    he_latitude_deg: Option[Double],
    he_longitude_deg: Option[Double],
    he_elevation_ft: Option[Int],
    he_heading_degT: Option[Double],
    he_displaced_threshold_ft: Option[Int]
)

object Runway {
  def fromCsv(line: List[String]): Runway = {
    Runway(
      id = line(0).toInt,
      airport_ref = line(1).toInt,
      airport_ident = line(2),
      length_ft = parseOptionalInt(line(3)),
      width_ft = parseOptionalInt(line(4)),
      surface = parseOptionalString(line(5)),
      lighted = line(6).toInt,
      closed = line(7).toInt,
      le_ident = parseOptionalString(line(8)),
      le_latitude_deg = parseOptionalDouble(line(9)),
      le_longitude_deg = parseOptionalDouble(line(10)),
      le_elevation_ft = parseOptionalInt(line(11)),
      le_heading_degT = parseOptionalDouble(line(12)),
      le_displaced_threshold_ft = parseOptionalInt(line(13)),
      he_ident = parseOptionalString(line(14)),
      he_latitude_deg = parseOptionalDouble(line(15)),
      he_longitude_deg = parseOptionalDouble(line(16)),
      he_elevation_ft = parseOptionalInt(line(17)),
      he_heading_degT = parseOptionalDouble(line(18)),
      he_displaced_threshold_ft = parseOptionalInt(line(19))
    )
  }
}
