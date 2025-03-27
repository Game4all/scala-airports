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

class AppScene(title: Label, content: Node) extends Scene(1200, 800) {
  val rootPane = new StackPane()
  root = rootPane

  val backgroundImage = new Image(getClass.getResource("/airplane-image.jpg").toExternalForm)
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

  this.width.onChange { (_, _, newWidth) => adjustBackgroundSize()}
  this.height.onChange { (_, _, newHeight) => adjustBackgroundSize()}
  def adjustBackgroundSize(): Unit = {
    backgroundView.fitWidth = this.getWidth
    backgroundView.fitHeight = this.getHeight
  }
}