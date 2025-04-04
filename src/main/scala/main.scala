import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._
import scalafx.scene.text.Font
import scalafx.geometry.{Insets, Pos}
import scalafx.collections.ObservableBuffer
import scala.collection.mutable
import scalafx.Includes.jfxScene2sfx
import scala.compiletime.uninitialized
import scalafx.Includes.jfxMultipleSelectionModel2sfx
import scala.concurrent.Future
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scala.concurrent.ExecutionContext.Implicits.global
import scalafx.beans.property.StringProperty
import scala.annotation.elidable

object AirportApp extends JFXApp3 {
  private var db: DatasetDB = uninitialized
  private var homeScene: Scene = null
  private var cacheAirportData: mutable.Map[String, List[(Airport, List[Runway])]] = mutable.Map()

  def initDB(): Unit = {
    db = DatasetDB()
    val airportsFile = "airports.csv"
    val runwaysFile = "runways.csv"
    val countriesFile = "countries.csv"

    db.populate(
      readCsv(countriesFile, Country.fromCsv),
      readCsv(runwaysFile, Runway.fromCsv),
      readCsv(airportsFile, Airport.fromCsv)
    )
  }

  def closeDB(): Unit = {
    db.close()
  }

  override def start(): Unit = {
    initDB()

    stage = new JFXApp3.PrimaryStage {
      title = "Airport Explorer"
      // fullScreen = true
      // fullScreenExitHint = ""
      scene = createHomeScene()
    }

    this.homeScene = stage.scene.value
  }

  def backToHome(): Unit = {
    stage.scene = if (homeScene != null) homeScene else createHomeScene()
    // stage.fullScreen = true
  }

  def handleQueryForm(countryList: ObservableBuffer[String], selectedCodeCountry: String): Unit = {
      if (selectedCodeCountry.nonEmpty && countryList.contains(selectedCodeCountry)) {
        val selectedCountry = selectedCodeCountry.split(" - ")(1).trim
        stage.scene = new LoadingScene("Fetching country data...")
        // stage.fullScreen = true

        Future {
          if (!cacheAirportData.contains(selectedCountry)) {
            val (_, airports) = DBQueries.fetchCountryAirportsRunways(db, selectedCountry)
            cacheAirportData(selectedCountry) = airports
          }
          javafx.application.Platform.runLater(() => 
            stage.scene = createCountryScene(selectedCountry, cacheAirportData(selectedCountry))
            // stage.fullScreen = true
          )
        }
      }
      else {
        if (selectedCodeCountry.isEmpty) {
          val alert = new Alert(Alert.AlertType.Warning) {
            title = "Warning"
            headerText = "Empty search field"
            contentText = "Please enter a country code or name to search for."
          }
          alert.showAndWait()
        }
        else {
          val alert = new Alert(Alert.AlertType.Warning) {
            title = "Warning"
            headerText = "Country not found"
            contentText = "Please enter a valid country code or name to search for."
          }
          alert.showAndWait()
        }
      }
    }

  def hanfleCountryDropdown(countryDropdown: ListView[String], searchField: TextField): Unit = {
    val selected = countryDropdown.selectionModel().getSelectedItem
    if (selected != null) searchField.text = selected
    countryDropdown.visible = false
  }

  def createHomeScene(): AppScene = {
    val rootPane = new StackPane()

    // Search Box
    val searchField = new TextField {
      promptText = "Enter country code or name..."
      style = "-fx-font-size: 16px; -fx-padding: 10px; -fx-border-radius: 5px;"
      minWidth = 300
    }
    val queryButton = new Button("🔍") {
      font = new Font("Arial", 18)
      style = "-fx-background-color: #005BBB; -fx-text-fill: white; -fx-padding: 10px 20px;"
    }
    val searchBox = new HBox(10, searchField, queryButton) {
      alignment = Pos.CENTER
    }

    // Country Dropdown
    val countries = DBQueries.fetchCountryDropdown(db)
    val countryList = ObservableBuffer(countries.map { case (code, name) => s"$code - $name" }.toSeq.sorted*)
    val countryDropdown = new ListView[String](countryList) {
      visible = false
      maxHeight = 150
      style = "-fx-background-color: white; -fx-font-size: 16px; -fx-border-radius: 5px;"
    }

    // Country Dropdown Filter
    searchField.text.onChange { (_, _, newValue) =>
      val filtered = if (newValue.contains(" - ")) {
                        val Array(code, name) = newValue.split(" - ").map(_.trim)
                        countries.filter { case (c, n) =>
                        c.toLowerCase.startsWith(code.toLowerCase) && n.toLowerCase.startsWith(name.toLowerCase)
                        }
                        .map { case (c, n) => s"$c - $n" }
                        .toSeq.sorted
                      }
                      else {
                        countries
                        .filter { case (code, name) =>
                          code.toLowerCase.startsWith(newValue.toLowerCase) ||
                          name.toLowerCase.startsWith(newValue.toLowerCase)
                        }
                        .map { case (code, name) => s"$code - $name" }
                        .toSeq.sorted
                      }

      countryList.setAll(filtered*)
      countryDropdown.visible = filtered.nonEmpty
    }

    // Country Dropdown Selection
    countryDropdown.onMouseClicked = _ => hanfleCountryDropdown(countryDropdown, searchField)

    // Query Button Action
    queryButton.onAction = _ => handleQueryForm(countryList, searchField.text.value)

    // Search Dropdown Box
    val searchDropdownBox = new VBox(5, searchBox, countryDropdown) {
      alignment = Pos.CENTER
    }
    // Section "Search Airports"
    val searchLabel = new Label("Search Airports") {
      font = new Font("Arial", 24)
      style = "-fx-fill: white; -fx-font-weight: bold;"
    }
    val searchContainer = new VBox(15, searchLabel, searchDropdownBox) {
      alignment = Pos.TOP_CENTER
      padding = Insets(30)
      style = "-fx-background-color: rgba(211, 233, 255, 0.7); -fx-padding: 40px; -fx-background-radius: 10px; min-width: fit-content;"
      maxWidth = 500
    }

    // Section "Insights"
    val insightsLabel = new Label("Insights") {
      font = new Font("Arial", 28)
      style = "-fx-fill: white; -fx-font-weight: bold;"
    }
    val insightsText = new Label("Choosing Reports will generate a document with:\n\n• 10 countries with highest/lowest airports.\n\n• Runway surface types per country.\n\n• Most common runway latitude.") {
      style = "-fx-fill: white;"
      font = new Font("Arial", 18)
    }
    val reportsButton = new Button("📊 Reports") {
      font = new Font("Arial", 18)
      style = "-fx-background-color:rgb(123, 168, 134); -fx-text-fill: white;"
      onAction = _ => {
        stage.scene = new LoadingScene("Compiling report... (May take a while)")
        // stage.fullScreen = true

        Future {
          val topCountries = DBQueries.fetchTopCountries(db, 10, (col: slick.lifted.Rep[Int]) => slick.lifted.ColumnOrdered(col, slick.ast.Ordering(slick.ast.Ordering.Desc)))
          val countrySurface = DBQueries.fetchSurfaceTypesPerCountry(db)
          // order by country name
          val countrySurfaceSorted = countrySurface.sortBy(_._1.name)          
          val topLattitude = DBQueries.fetchMostCommonLatitudes(db, 10)

          javafx.application.Platform.runLater(() => 
            stage.scene = createRapportScene(topCountries, countrySurfaceSorted, topLattitude)
            // stage.fullScreen = true
          )
        }
      }
    }
    val insightsContainer = new VBox(15, insightsLabel, insightsText, reportsButton) {
      alignment = Pos.TOP_CENTER
      padding = Insets(30)
      style = "-fx-background-color: rgba(219, 237, 255, 0.7); -fx-padding: 40px; -fx-background-radius: 10px; min-width: fit-content;"
      maxWidth = 500
    }

    // Alignement horizontal des sections
    val sectionsBox = new HBox(80, searchContainer, insightsContainer) {
      alignment = Pos.CENTER
      padding = Insets(50)
    }

    // title Scene
    val homeTitle = new Label("Airport Explorer") {
      style = "-fx-fill: white; -fx-font-weight: bold; -fx-padding: 10px;"
      font = new Font("Arial", 48)
    }

    new AppScene(homeTitle, sectionsBox)
  }


  def handleAirportListView(airportListView: ListView[String], airportDetails: TextArea, runwayList: ListView[String], runwayDetails: TextArea, airports: List[(Airport, List[Runway])]): Unit = {
    airportListView.selectionModel().selectedItem.onChange { (_, _, newAirport) =>
    if (newAirport != null) {
        val airport = airports.find { case (a, _) => a.name == newAirport }.get._1
        val runways = airports.collectFirst {
          case (airport, runways) if airport.name == newAirport => runways
        }.getOrElse(Nil)

        val airportSummary = airport.summary
        airportDetails.text = if (airportSummary.isEmpty) "No details available or airport selected" else s"$airportSummary"

        // clear runway details
        runwayDetails.text = "No runway selected"
        if (runways.nonEmpty) runwayList.items = ObservableBuffer(runways.map(_.toString)*)
        else runwayList.items = ObservableBuffer("No runway available")
      }
    }
  }

  def handleRunwayListView(runwayListView: ListView[String], runwayDetails: TextArea, airports: List[(Airport, List[Runway])]): Unit = {
    runwayListView.selectionModel().selectedItem.onChange { (_, _, newRunway) =>
      if (newRunway != null) {
        val runway = airports
          .flatMap { case (_, runways) => runways }
          .find(_.toString == newRunway)
          .get

        val runwaySummary = runway.summary
        runwayDetails.text = s"$runwaySummary"
      }
    }
  }

  def createCountryScene(selectedCountry: String, airports: List[(Airport, List[Runway])]): AppScene = {
    val rootPane = new StackPane()

    // Create airport list and label
    val airportLabel = new Label("Airports") {
      font = new Font("Arial", 24)
      style = "-fx-text-fill: white; -fx-font-weight: bold;"
    }
    val airportList = ObservableBuffer(airports.map(_._1.name).filter(_.nonEmpty)*).sortInPlace()
    val airportListView = new ListView[String](airportList) {
      style = """
        -fx-font-size: 18px;
        -fx-background-radius: 15px;
        -fx-background-color: #87CEEB;
        -fx-border-color: #4682B4;
        -fx-border-width: 2px;
        -fx-border-radius: 15px;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-padding: 10px;
        -fx-selection-bar: #00BFFF;
      """
      maxHeight = 350
      prefWidth = 400
      maxWidth = 400
    }
    airportListView.setFixedCellSize(40)
    airportListView.setPrefHeight(10 * airportListView.getFixedCellSize + 2)

    // Create runways list and label
    val runwayLabel = new Label("Runways") {
      font = new Font("Arial", 24)
      style = "-fx-text-fill: white; -fx-font-weight: bold;"
    }

    val runwayList = new ListView[String]() {
      style = """
        -fx-font-size: 18px;
        -fx-background-radius: 15px;
        -fx-background-color: #87CEEB;
        -fx-border-color: #4682B4;
        -fx-border-width: 2px;
        -fx-border-radius: 15px;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-padding: 10px;
        -fx-selection-bar: #00BFFF;
      """
      maxHeight = 350
      prefWidth = 400
      maxWidth = 400
    }
    runwayList.setFixedCellSize(40)
    runwayList.setPrefHeight(10 * runwayList.getFixedCellSize + 2)

    // Airport details and runway details
    val airportDetails = new TextArea {
      editable = false
      wrapText = true
      style = """
        -fx-font-size: 14px;
        -fx-font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        -fx-background-radius: 15px;
        -fx-background-color: rgba(255, 255, 255, 0.8);  /* Fond plus clair */
        -fx-border-color: #5f6368;  /* Bordure subtile */
        -fx-border-width: 1px;
        -fx-border-radius: 15px;
        -fx-text-fill: #333333;  /* Texte sombre pour la lisibilité */
        -fx-padding: 15px;
        -fx-font-weight: normal;
        -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0, 0, 2); /* Ombre douce */
      """
      maxHeight = 250
      prefWidth = 400
      wrapText = true
    }
    val runwayDetails = new TextArea {
      editable = false
      wrapText = true
      style = """
        -fx-font-size: 14px;
        -fx-font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        -fx-background-radius: 15px;
        -fx-background-color: rgba(255, 255, 255, 0.8);  /* Fond plus clair */
        -fx-border-color: #5f6368;  /* Bordure subtile */
        -fx-border-width: 1px;
        -fx-border-radius: 15px;
        -fx-text-fill: #333333;  /* Texte sombre pour la lisibilité */
        -fx-padding: 15px;
        -fx-font-weight: normal;
        -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0, 0, 2); /* Ombre douce */
      """
      maxHeight = 250
      prefWidth = 400
      wrapText = true
    }

    val airportDetailsPane = new TitledPane {
      text = "Details"
      expanded = false
      content = new VBox(airportDetails) {
        padding = Insets(10)
      }
      prefWidth = 400
      maxHeight = 300
      style = """
        -fx-background-color: #f0f0f0;  /* Fond clair pour la TitledPane */
        -fx-border-color: #dcdcdc;  /* Bordure claire */
        -fx-border-width: 1px;
        -fx-border-radius: 15px;
      """
    }
    val runwayDetailsPane = new TitledPane {
      text = "Runway Details"
      expanded = false
      content = new VBox(runwayDetails) {
        padding = Insets(10)
      }
      prefWidth = 400
      maxHeight = 300
      style = """
        -fx-background-color: #f0f0f0;  /* Fond clair pour la TitledPane */
        -fx-border-color: #dcdcdc;  /* Bordure claire */
        -fx-border-width: 1px;
        -fx-border-radius: 15px;
      """
    }

    handleAirportListView(airportListView, airportDetails, runwayList, runwayDetails, airports)
    handleRunwayListView(runwayList, runwayDetails, airports)

    val backButton = new Button("⬅ Back") {
      font = new Font("Arial", 18)
      style = """
        -fx-background-color: #ff5555; 
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-padding: 10px 20px;
        -fx-background-radius: 10px;
      """
      onAction = _ => backToHome()
    }

    val leftPane = new VBox(15, airportLabel, airportListView, airportDetailsPane) {
      alignment = Pos.CENTER
      padding = Insets(10)
      style = """
        -fx-background-color: rgba(0, 0, 0, 0.7);
        -fx-background-radius: 20px;
        -fx-border-color: white;
        -fx-border-width: 2px;
        -fx-border-radius: 20px;
      """
      maxWidth = 450
      prefWidth = 450
      maxHeight = 600
      prefHeight = 600
    }

    val RightPane = new VBox(15, runwayLabel, runwayList, runwayDetailsPane) {
      alignment = Pos.CENTER
      padding = Insets(10)
      style = """
        -fx-background-color: rgba(0, 0, 0, 0.7);
        -fx-background-radius: 20px;
        -fx-border-color: white;
        -fx-border-width: 2px;
        -fx-border-radius: 20px;
      """
      maxWidth = 450
      prefWidth = 450
      maxHeight = 600
      prefHeight = 600
    }

    val mainContainer = new HBox(100, leftPane, RightPane) {
      alignment = Pos.CENTER
      padding = Insets(30)
    }

    val mainLayout = new VBox(100, mainContainer, backButton) {
      alignment = Pos.CENTER
    }

    val title = new Label(s"Airports in ${selectedCountry.toUpperCase()}") {
      font = new Font("Arial", 50)
      style = "-fx-text-fill: white; -fx-font-weight: bold;"
      padding = Insets(20)
    }

    new AppScene(title, mainLayout)
  }

  def createRapportScene(
    topCountries: List[(Country, Int)], 
    countrySurface: List[(Country, List[Option[String]])], 
    topLatitude: List[Option[String]]
  ): AppScene = {
    
    val dateToday = java.time.LocalDate.now().toString
    val title = new Label(f"Reports of All Airports, updated on $dateToday") {
      font = new Font("Arial", 36)
      style = "-fx-text-fill: white; -fx-font-weight: bold;"
    }
    
    val topCountriesTitle = new Label("🏆 Top 10 Countries by Airports") {
      font = new Font("Arial", 24)
      style = "-fx-text-fill: black; -fx-font-weight: bold;"
    }
    
    val topCountriesList = new VBox(
      topCountries.map { case (country, count) => 
        new Label(s"${country.name}: $count airports") {
          font = new Font("Arial", 18)
          style = "-fx-text-fill: black;"
        }
      }*
    )

    val topCountriesContainer = new VBox(10, topCountriesTitle, topCountriesList) {
      padding = Insets(10)
      alignment = Pos.CENTER
      style = """-fx-background-color: #87CEEB;
                -fx-border-radius: 10px;
                -fx-background-radius: 10px;"""
    }

    val separator = new Separator()

    val latitudeTitle = new Label("📍 Top 10 Common Runway Latitudes") {
      font = new Font("Arial", 24)
      style = "-fx-text-fill: black; -fx-font-weight: bold;"
    }

    val latitudeList = new VBox(
      topLatitude.flatten.map(lat =>
        val latRank = topLatitude.flatten.indexOf(lat) + 1
        val textLat = if (lat.contains("H")) lat + " (Helipad)" else lat + "°"
        new Label(s"Rank $latRank: $textLat") {
          font = new Font("Arial", 18)
          style = "-fx-text-fill: black;"
        }
      )*
    )

    val latitudeContainer = new VBox(10, latitudeTitle, latitudeList) {
      padding = Insets(10)
      alignment = Pos.CENTER
      style = "-fx-background-color: #4682B4; -fx-border-radius: 10px; -fx-background-radius: 10px;"
    }

    val leftContainer = new VBox(20, topCountriesContainer, separator, latitudeContainer) {
      padding = Insets(20)
      alignment = Pos.TOP_CENTER
    }


    val surfaceTitle = new Label("🌍 Runway Surface Types per Country") {
      font = new Font("Arial", 24)
      style = "-fx-text-fill: black; -fx-font-weight: bold;"
    }

    val countriesSurface = countrySurface.map { case (country, surfaces) =>
      val surfaceTypes = surfaces.flatten.distinct.mkString(", ")
      s"${country.name}: $surfaceTypes"
    }

    // Create a listview for the country surface types that we can scroll
    val countrySurfaceList = new ListView[String](ObservableBuffer(countriesSurface*)) {
      style = """
        -fx-font-size: 18px;
        -fx-background-radius: 15px;
        -fx-background-color: #87CEEB;
        -fx-border-color: #4682B4;
        -fx-border-width: 2px;
        -fx-border-radius: 15px;
        -fx-text-fill: black;
        -fx-font-weight: bold;
        -fx-padding: 10px;
        -fx-selection-bar: #00BFFF;
      """
      maxHeight = 350
      prefWidth = 400
      maxWidth = 400
    }
    countrySurfaceList.setFixedCellSize(40)
    countrySurfaceList.setPrefHeight(10 * countrySurfaceList.getFixedCellSize + 2)

    countrySurfaceList.visible = true
    countrySurfaceList.managed = true

    val rightContainer = new VBox(10, surfaceTitle, countrySurfaceList) {
      padding = Insets(20)
      alignment = Pos.CENTER // Centre les éléments à l'intérieur du conteneur
      style = """
        -fx-background-color: #4682B4; /* Bleu */
        -fx-padding: 20px;
        -fx-border-radius: 10px;
        -fx-background-radius: 10px;
        -fx-text-fill: white;
      """
      maxWidth = 450
      prefWidth = 450
      maxHeight = 550
      prefHeight = 550
    }

    HBox.setHgrow(rightContainer, Priority.NEVER)
    val mainContainer = new HBox(40, leftContainer, rightContainer) {
      padding = Insets(20)
      alignment = Pos.CENTER
    }

    // Create the back button
    val backButton = new Button("⬅ Back") {
      font = new Font("Arial", 18)
      style = """
        -fx-background-color: #ff5555; 
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-padding: 10px 20px;
        -fx-background-radius: 10px;
      """
      onAction = _ => backToHome()
    }

    val mainLayout = new VBox(10, mainContainer, backButton) {
      alignment = Pos.CENTER
    }

    new AppScene(title, mainLayout)
  }


  override def stopApp(): Unit = {
    closeDB()
  }
}