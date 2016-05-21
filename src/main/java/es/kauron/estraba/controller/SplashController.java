package es.kauron.estraba.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSpinner;
import es.kauron.estraba.App;
import es.kauron.estraba.model.DataBundle;
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
    private void loadGPXFile(ActionEvent event) throws Exception {
        buttonLoad.setVisible(false);
        labelWelcome.setVisible(false);
        spinner.setVisible(true);
        snackbar.registerSnackbarContainer(root);
        snackbar.show("Loading file", 5000);
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(App.GENERAL_BUNDLE.getString("app.extension"), "*.gpx"));
        file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) {
            errorLoading();
            return;
        }

        Task<DataBundle> t = new Task<DataBundle>() {
            @Override
            protected DataBundle call() throws Exception {
                DataBundle db = DataBundle.loadFrom(file);
                System.out.println("done");
                updateProgress(1, 1);
                return db;
            }
        };

       t.setOnSucceeded(ev -> {
            DataBundle bundle = t.getValue();
            if (bundle == null) errorLoading();
            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource("fxml/Dashboard.fxml"), App.GENERAL_BUNDLE);
            Parent parent = null;
            try {
                parent = loader.load();
            } catch (IOException e) {
                errorLoading();
            }
            loader.<DashboardController>getController().postInit(bundle);
            ((Stage) root.getScene().getWindow()).setScene(new Scene(parent));
        });

        Thread th = new Thread(t);
        th.setDaemon(true);
        th.start();

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

