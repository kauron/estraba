package es.kauron.estraba.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTabPane;
import com.lynden.gmapsfx.GoogleMapView;
import es.kauron.estraba.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * es.kauron.estraba (estraba)
 * Created by baudlord on 5/17/16.
 */

public class DashboardController implements Initializable {

    @FXML
    private JFXTabPane root;

    @FXML
    private Tab tabDashboard;

    @FXML
    private Tab tabMap;

    @FXML
    private GoogleMapView mapView;

    @FXML
    private JFXListView<?> mapSummary;

    @FXML
    private JFXButton elevationButton;

    @FXML
    private JFXButton speedButton;

    @FXML
    private JFXButton hrButton;

    @FXML
    private JFXButton cadenceButton;

    @FXML
    private Tab tabGraph;

    @FXML
    private AreaChart<Double, Double> elevationChart;

    @FXML
    private LineChart<Double, Double> speedChart;

    @FXML
    private LineChart<Double, Double> hrChart;

    @FXML
    private LineChart<Double, Double> cadenceChart;

    @FXML
    private Tab tabSettings;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        elevationButton.setGraphic(new ImageView(new Image(App.class.getResourceAsStream("img/elevation.png"))));
        speedButton.setGraphic(new ImageView(new Image(App.class.getResourceAsStream("img/speed.png"))));
        hrButton.setGraphic(new ImageView(new Image(App.class.getResourceAsStream("img/hr.png"))));
        cadenceButton.setGraphic(new ImageView(new Image(App.class.getResourceAsStream("img/cadence.png"))));

    }
}

