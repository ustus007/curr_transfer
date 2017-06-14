package test.revolut.application

import scalafx.application.JFXApp
import scalafx.scene.layout.FlowPane
import scalafx.scene.Scene
import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.control.TextField
import scalafx.scene.control.Button
import scalafx.beans.value.ObservableValue
import scalafx.Includes._
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import scalafx.stage.FileChooser
import test.revolut.data.storage.InMemory


object ApplicationStart  extends JFXApp {
  
  val rootPane = new FlowPane
  val portLabel = new Label("Port: ")
  val portField = new TextField
  val startServerButton = new Button("Start web server")
  val stopServerButton = new Button("Stop web server")
  val saveDataButton = new Button("Save data to file")
  val loadDataButton = new Button("Load data from file")
  val mainFileChooser: FileChooser = new FileChooser()
  
  stage = new JFXApp.PrimaryStage {
    title = "Web server controlls"
    onCloseRequest = handle {
      ServerHolder.stopServer
    }
    scene = new Scene(350, 100) {
      rootPane.padding = Insets(5, 0, 5, 0)
      rootPane.vgap = 4
      rootPane.hgap = 4
      portField.setText("8082")
      stopServerButton.disable = true
      rootPane.children.addAll(portLabel,portField, startServerButton, stopServerButton, saveDataButton, loadDataButton)
      root = rootPane
      portField.text.onChange((observable: ObservableValue[String, String], oldVal: String, newVal: String) => {
        var newValue: String = portField.text.value
        if (!newValue.matches("\\d*")) {
          portField.setText(newValue.replaceAll("[^\\d]", ""));
        }
      })
      
      
      
      startServerButton.onAction = handle {
        val port = portField.text.value.toInt
        
        ServerHolder.initServer(port)
        
        portLabel.disable = true
        portField.disable = true
        startServerButton.disable = true
        stopServerButton.disable = false
        saveDataButton.disable = true
        loadDataButton.disable = true
      }
      stopServerButton.onAction = handle {
        ServerHolder.stopServer
        
        portLabel.disable = false
        portField.disable = false
        startServerButton.disable = false
        stopServerButton.disable = true
        saveDataButton.disable = false
        loadDataButton.disable = false
        
      }
      saveDataButton.onAction = handle {
        InMemory.save(mainFileChooser.showSaveDialog(stage).getAbsolutePath)
      }
      loadDataButton.onAction = handle {
        InMemory.load(mainFileChooser.showOpenDialog(stage))
      }
      
      
    }
  }
}