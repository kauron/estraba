package es.kauron.estraba.controller;
import com.jfoenix.controls.JFXListView;
import com.lynden.gmapsfx.GoogleMapView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Tab;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * es.kauron.estraba (estraba)
 * Created by baudlord on 5/17/16.
 */

public class DashboardController implements Initializable {

    @FXML
    private Tab tabDashboard;

    @FXML
    private Tab tabMap;

    @FXML
    private GoogleMapView mapView;

    @FXML
    private JFXListView<?> mapSummary;

    @FXML
    private Tab tabGraph;

    @FXML
    private AreaChart<?, ?> elevationChart;

    @FXML
    private LineChart<?, ?> speedChart;

    @FXML
    private LineChart<?, ?> hrChart;

    @FXML
    private LineChart<?, ?> cadenceChart;

    @FXML
    private Tab tabSettings;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

