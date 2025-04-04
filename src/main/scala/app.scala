import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.image.ImageView
import scalafx.scene.layout.StackPane
import scalafx.scene.control.Label
import scalafx.scene.text.Font
import scalafx.scene.image.Image
import scala.languageFeature.postfixOps
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.VBox
import scalafx.scene.layout.HBox
import javafx.scene.Node
import scalafx.scene.layout.Priority
import scalafx.scene.layout.Region
import scalafx.scene.input.KeyCode.I
import scalafx.scene.control.ProgressIndicator
import scalafx.animation.FadeTransition
import scalafx.util.Duration

private val appWidth = 1536
private val appHeight = 768

class AppScene(title: Label, content: Node) extends Scene(appWidth, appHeight) {
  val rootPane = new StackPane()
  root = rootPane

  val backgroundImage = new Image(
    getClass.getResource("/airplane-image.jpg").toExternalForm
  )
  val backgroundView = new ImageView(backgroundImage) {
    preserveRatio = false
  }

  val copyright = new Label("© 2025 Airport Explorer") {
    style = "-fx-fill: white; -fx-font-weight: bold; -fx-padding: 10px;"
    font = new Font("Arial", 24)
  }

  val authors = new Label("Authors: Thomas Balsalobre, Lucas Arriesse") {
    style = "-fx-fill: white; -fx-font-weight: bold; -fx-padding: 10px;"
    font = new Font("Arial", 24)
  }

  val footer = new HBox(5) {
    children.addAll(authors, copyright)
    alignment = Pos.BOTTOM_CENTER
  }

  val mainContainer = new VBox(20) {
    padding = Insets(10)
    children.addAll(title, content, footer)
    alignment = Pos.Center
    VBox.setVgrow(this, Priority.ALWAYS)
    prefHeight = Region.USE_COMPUTED_SIZE
  }

  rootPane.children.addAll(backgroundView, mainContainer)

  this.width.onChange { (_, _, newWidth) => adjustBackgroundSize() }
  this.height.onChange { (_, _, newHeight) => adjustBackgroundSize() }
  def adjustBackgroundSize(): Unit = {
    backgroundView.fitWidth = this.getWidth
    backgroundView.fitHeight = this.getHeight
  }
}

class LoadingScene extends Scene(appWidth, appHeight) {
  private val rootPane = new StackPane()
  root = rootPane

  def this(loadingText: String) = {
    this()
    waitingLabel.text = loadingText
  }

  private val backgroundImage = new Image(
    getClass.getResource("/airplane-image.jpg").toExternalForm
  )
  private val backgroundView = new ImageView(backgroundImage) {
    preserveRatio = false
  }

  private val waitingLabel = new Label("Loading...") {
    font = new Font("Arial", 24)
    style = "-fx-text-fill: white; -fx-font-weight: bold;"
  }

  private val fadeAnimation = new FadeTransition(Duration(1200), waitingLabel) {
    fromValue = 1.0
    toValue = 0.3
    cycleCount = FadeTransition.Indefinite
    autoReverse = true
  }
  fadeAnimation.play()

  private val progressIndicator = new ProgressIndicator {
    style = "-fx-progress-color: blue;"
    prefWidth = 80
    prefHeight = 80
  }

  private val waitingBox = new VBox(20, waitingLabel, progressIndicator) {
    alignment = Pos.CENTER
    padding = Insets(20)
    style =
      "-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10;"
  }

  private val mainContainer = new StackPane() {
    children.addAll(backgroundView, waitingBox)
  }

  rootPane.children.add(mainContainer)
  width.onChange((_, _, newWidth) => adjustBackgroundSize())
  height.onChange((_, _, newHeight) => adjustBackgroundSize())

  private def adjustBackgroundSize(): Unit = {
    backgroundView.fitWidth = width()
    backgroundView.fitHeight = height()
  }
}
