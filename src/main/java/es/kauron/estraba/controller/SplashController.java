package es.kauron.estraba.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSpinner;
import es.kauron.estraba.App;
import es.kauron.estraba.model.DataBundle;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * es.kauron.estraba.controller (estraba)
 * Created by baudlord on 5/19/16.
 */

public class SplashController implements Initializable{

    @FXML
    private AnchorPane root;

    @FXML
    private ImageView imgLogo;

    @FXML
    private JFXSpinner spinner;

    @FXML
    private Label labelWelcome;

    @FXML
    private JFXButton buttonLoad;

    private JFXSnackbar snackbar;
    private File file;

    @FXML
    private void loadGPXFile(ActionEvent event) {

        buttonLoad.setVisible(false);
        labelWelcome.setVisible(false);
        spinner.setVisible(true);
        snackbar.registerSnackbarContainer(root);
        snackbar.show("Loading file", 5000);
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(App.GENERAL_BUNDLE.getString("app.extension.filter.name"), "*.gpx"));
        file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) {
            errorLoading();
            return;
        }
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    DataBundle bundle = DataBundle.loadFrom(file);
                    FXMLLoader loader = new FXMLLoader(
                            App.class.getResource("fxml/Dashboard.fxml"), App.GENERAL_BUNDLE);
                    Parent root = loader.load();

                    Stage stage = new Stage();
                    stage.getIcons().add(new Image(App.class.getResource("img/icon.png").toString()));
                    stage.setTitle(App.GENERAL_BUNDLE.getString("app.title"));
                    stage.setResizable(false);
                    stage.setScene(new Scene(root));

                    // Begin awesomewm code
                    stage.setMinHeight(500);
                    stage.setMinWidth(800);
                    stage.setResizable(false);
                    // End awesomewm code
                    stage.show();
                    loader.<DashboardController>getController().postInit(bundle);
                    Platform.runLater(() -> ((Stage) root.getScene().getWindow()).close());
                } catch (IOException e) {
                    errorLoading();
                }
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imgLogo.setImage(new Image(App.class.getResourceAsStream("img/strava-transparent.png")));
        snackbar = new JFXSnackbar();
    }

    private void errorLoading() {
        buttonLoad.setVisible(true);
        labelWelcome.setVisible(true);
        spinner.setVisible(false);
        snackbar.show("Error loading file", 3000);
    }
}

