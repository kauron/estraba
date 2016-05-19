package es.kauron.estraba.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import es.kauron.estraba.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * es.kauron.estraba.controller (estraba)
 * Created by baudlord on 5/19/16.
 */

public class SplashController implements Initializable{

    @FXML
    private ImageView imgLogo;

    @FXML
    private JFXSpinner spinner;

    @FXML
    private Label labelWelcome;

    @FXML
    private JFXButton buttonLoad;

    @FXML
    void loadGPXFile(ActionEvent event) {

        buttonLoad.setVisible(false);
        labelWelcome.setVisible(false);
        spinner.setVisible(true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imgLogo.setImage(new Image(App.class.getResourceAsStream("img/splash.png")));
    }
}

