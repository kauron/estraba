/*
 * Copyright (c) 2016 Jesús "baudlord" Vélez Palacios, Carlos "kauron" Santiago Galindo Jiménez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return.
 *
 */

package es.kauron.estraba.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import es.kauron.estraba.App;
import es.kauron.estraba.model.DataBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jgpx.model.analysis.Chunk;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * es.kauron.estraba (estraba)
 * Created by baudlord on 5/17/16.
 */

public class DashboardController implements Initializable, MapComponentInitializedListener {

    final int N = 0, S = 1, E = 2, W = 3;
    final double[] coord = new double[4];
    @FXML
    private AnchorPane root;
    @FXML
    private Tab tabDashboard, tabMap, tabGraph;

    @FXML
    private ImageView imgHR, imgSpeed, imgCadence, imgDate, imgDistance, imgElevation;
    @FXML
    private Label valueHRAvg, valueHRMin, valueHRMax, valueSpeedAvg, valueSpeedMax, valueCadenceAvg, valueCadenceMax,
            valueDate, valueTime, valueActiveTime, valueTotalTime, valueDistance, valueElevation, labelMotivationUpper,
            valueAscent, valueDescent, labelMotivatorLower;
    @FXML
    private JFXSpinner mapSpinner;
    @FXML
    private PieChart zoneChart;
    @FXML
    private GoogleMapView mapView;
    private ObservableList<Chunk> chunks;
    @FXML
    private JFXButton elevationButton, speedButton, hrButton, cadenceButton;

    @FXML
    private AreaChart<Double, Double> elevationChart;

    @FXML
    private AreaChart<Long, Double> elevationTChart;

    @FXML
    private LineChart<Double, Double> speedChart, hrChart, cadenceChart, mapChart;

    @FXML
    private LineChart<Long, Double> speedTChart, hrTChart, cadenceTChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mapView.setVisible(false);
        // populate map icons
        ((ImageView)elevationButton.getGraphic()).setImage(new Image(App.class.getResourceAsStream("img/elevation.png")));
        ((ImageView)speedButton.getGraphic()).setImage(new Image(App.class.getResourceAsStream("img/speed.png")));
        ((ImageView)hrButton.getGraphic()).setImage(new Image(App.class.getResourceAsStream("img/hr.png")));
        ((ImageView)cadenceButton.getGraphic()).setImage(new Image(App.class.getResourceAsStream("img/cadence.png")));

        // set icons of dashboard
        imgHR.setImage(new Image(App.class.getResourceAsStream("img/hr.png")));
        imgSpeed.setImage(new Image(App.class.getResourceAsStream("img/speed.png")));
        imgCadence.setImage(new Image(App.class.getResourceAsStream("img/cadence.png")));
        imgDate.setImage(new Image(App.class.getResourceAsStream("img/date.png")));
        imgDistance.setImage(new Image(App.class.getResourceAsStream("img/distance.png")));
        imgElevation.setImage(new Image(App.class.getResourceAsStream("img/elevation.png")));


    }

    @FXML
    private void toggleChart(MouseEvent e) {
        System.out.println("hi");
        for (Node n : ((Node) e.getSource()).getParent().getChildrenUnmodifiable())
            n.setVisible(!n.isVisible());
    }

    @FXML
    private void loadFile() {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("fxml/Splash.fxml"), App.GENERAL_BUNDLE);
        Parent parent;
        try {
            parent = loader.load();
            ((Stage) root.getScene().getWindow()).setScene(new Scene(parent));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onMapButton(ActionEvent event){
        switch (((JFXButton)event.getSource()).getId()) {
            case "elevationButton":
                mapChart.setData(elevationChart.getData());
                break;
            case "speedButton":
                mapChart.setData(speedChart.getData());
                break;
            case "hrButton":
                mapChart.setData(hrChart.getData());
                break;
            case "cadenceButton":
                mapChart.setData(cadenceChart.getData());
                break;
        }
    }

    private String randomMotivation() {
        return App.GENERAL_BUNDLE.getString("label.welcome");
    }

    void load(DataBundle bundle) {
        valueHRAvg.setText(bundle.HRAvg);
        valueHRMax.setText(bundle.HRMax);
        valueHRMin.setText(bundle.HRMin);
        valueSpeedAvg.setText(bundle.speedAvg);
        valueSpeedMax.setText(bundle.speedMax);
        valueCadenceAvg.setText(bundle.cadenceAvg);
        valueCadenceMax.setText(bundle.cadenceMax);
        valueDate.setText(bundle.date);
        valueTime.setText(bundle.time);
        valueActiveTime.setText(bundle.activeTime);
        valueTotalTime.setText(bundle.totalTime);
        valueDistance.setText(bundle.distance);
        valueElevation.setText(bundle.elevation);
        valueAscent.setText(bundle.ascent);
        valueDescent.setText(bundle.descent);
        zoneChart.setData(bundle.pieData);

        // populate the charts
        elevationChart.getData().add(bundle.elevationSeries);
        elevationTChart.getData().add(bundle.elevationTSeries);
        speedChart.getData().add(bundle.speedSeries);
        speedTChart.getData().add(bundle.speedTSeries);
        hrChart.getData().add(bundle.hrSeries);
        hrTChart.getData().add(bundle.hrTSeries);
        cadenceChart.getData().add(bundle.cadenceSeries);
        cadenceTChart.getData().add(bundle.cadenceTSeries);

        //initialize map
        chunks = bundle.chunks;
        mapView.addMapInializedListener(this);
    }

    @Override
    public void mapInitialized() {
        // When the JS init is done
        coord[0] = Double.MIN_VALUE;
        coord[1] = Double.MAX_VALUE;
        coord[2] = Double.MIN_VALUE;
        coord[3] = Double.MAX_VALUE;

        // Create a new map with the appropriate options
        GoogleMap map = mapView.createMap(new MapOptions()
                .center(new LatLong(0.0, 0.0))
                .mapType(MapTypeIdEnum.TERRAIN)
                .overviewMapControl(true)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(true)
                .streetViewControl(false)
                .zoomControl(true)
                .zoom(0)
        );

        // Prepare an array with LatLong objects
        MVCArray pathArray = new MVCArray();
        pathArray.push(new LatLong( // first step of the route
                chunks.get(0).getFirstPoint().getLatitude(),
                chunks.get(0).getFirstPoint().getLongitude()
        ));
        chunks.forEach(chunk -> {
            double lat = chunk.getLastPoint().getLatitude();
            double lon = chunk.getLastPoint().getLongitude();
            coord[N] = Math.max(lat, coord[N]);
            coord[S] = Math.min(lat, coord[S]);
            coord[E] = Math.max(lon, coord[E]);
            coord[W] = Math.min(lon, coord[W]);
            pathArray.push(new LatLong(lat, lon));
        });
        map.addMarker(new Marker(new MarkerOptions()
                .position(new LatLong(
                        chunks.get(0).getFirstPoint().getLatitude(),
                        chunks.get(0).getFirstPoint().getLongitude()))
                .title("label.begin")));
        map.addMapShape(new Polyline(new PolylineOptions().path(pathArray).strokeColor("#fc4c02")));
        map.addMarker(new Marker(new MarkerOptions()
                .position(new LatLong(
                        chunks.get(chunks.size() - 1).getLastPoint().getLatitude(),
                        chunks.get(chunks.size() - 1).getLastPoint().getLongitude()))
                .title("label.end")));
        // Adjust the map to the correct center and zoom
        mapView.setVisible(true);
        mapSpinner.setVisible(false);

        mapView.heightProperty().addListener(e -> centerMap());
        mapView.widthProperty().addListener(e -> centerMap());
        centerMap();
    }

    private void centerMap() {
        mapView.getMap().setZoom(getBoundsZoomLevel(coord, mapView.getHeight(), mapView.getWidth()));
        mapView.getMap().fitBounds(new LatLongBounds(
                new LatLong(coord[S], coord[W]),
                new LatLong(coord[N], coord[E])
        ));
    }

    /**
     * Method to compute properly the amount of zoom in a GMap
     * Many thanks to <a href=http://stackoverflow.com/a/13274361>http://stackoverflow.com/a/13274361</a>
     * @param bound Array with coordinates bounds [N, S, E, W]
     * @return Zoom from 0 to 21
     */
    private int getBoundsZoomLevel(double[] bound, double mapHeight, double mapWidth) {
        double latFraction = (latRad(bound[0]) - latRad(bound[1])) / Math.PI;

        double lngDiff = bound[2] - bound[3];
        double lngFraction = ((lngDiff < 0) ? (lngDiff + 360) : lngDiff) / 360;

        double latZoom = zoom(mapHeight, 256, latFraction);
        double lngZoom = zoom(mapWidth, 256, lngFraction);

        return (int) Math.min(Math.min(latZoom, lngZoom), 21);
    }

    private double zoom(double mapPx, int worldPx, double fraction) {
        return Math.floor(Math.log(mapPx / worldPx / fraction) / Math.log(2));
    }

    private double latRad(double lat) {
        double sin = Math.sin(lat * Math.PI / 180);
        double radX2 = Math.log((1 + sin) / (1 - sin)) / 2;
        return Math.max(Math.min(radX2, Math.PI), -Math.PI) / 2;
    }
}

