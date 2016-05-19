package es.kauron.estraba.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import es.kauron.estraba.App;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import jgpx.model.analysis.Chunk;
import jgpx.model.analysis.TrackData;
import jgpx.model.gpx.Track;
import jgpx.model.jaxb.GpxType;
import jgpx.model.jaxb.TrackPointExtensionT;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ResourceBundle;

/**
 * es.kauron.estraba (estraba)
 * Created by baudlord on 5/17/16.
 */

public class DashboardController implements Initializable, MapComponentInitializedListener {

    @FXML
    private AnchorPane root;

    @FXML
    private Tab tabDashboard, tabMap, tabGraph, tabSettings;

    @FXML
    private ImageView imgHR, imgSpeed, imgCadence, imgDate, imgDistance, imgElevation;

    @FXML
    private Label valueHRAvg, valueHRMin, valueHRMax, valueSpeedAvg, valueSpeedMax, valueCadenceAvg, valueCadenceMax,
            valueDate, valueTime, valueActiveTime, valueTotalTime, valueDistance, valueElevation, labelMotivationUpper,
            valueAscent, valueDescent, labelMotivatorLower;

    @FXML
    private PieChart zoneChart;

    @FXML
    private GoogleMapView mapView;
    private TrackData track;

    @FXML
    private JFXButton elevationButton, speedButton, hrButton, cadenceButton;

    @FXML
    private AreaChart<Double, Double> elevationChart;

    @FXML
    private LineChart<Double, Double> speedChart, hrChart, cadenceChart, mapChart;

    private JFXSnackbar snackbar;
    private static final double DISTANCE_EPSILON = 1E-6;
    private static final double KILOMETER_CUTOFF = 10000;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        snackbar = new JFXSnackbar();
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

    public void postinit() {
        snackbar.registerSnackbarContainer(root);
        try {load();} catch (JAXBException e) {e.printStackTrace();}
    }

    private void loadTrack(TrackData track) {
        valueHRAvg.setText(track.getAverageHeartrate()
                + App.GENERAL_BUNDLE.getString("unit.bpm"));
        valueHRMax.setText(track.getMaxHeartrate()
                + App.GENERAL_BUNDLE.getString("unit.bpm"));
        valueHRMin.setText(track.getMinHeartRate()
                + App.GENERAL_BUNDLE.getString("unit.bpm"));

        // speed is given as m/s
        valueSpeedAvg.setText(String.format("%.2f", track.getAverageSpeed() * 3.6)
                + App.GENERAL_BUNDLE.getString("unit.kmph"));
        valueSpeedMax.setText(String.format("%.2f", track.getMaxSpeed() * 3.6)
                + App.GENERAL_BUNDLE.getString("unit.kmph"));

        valueCadenceAvg.setText(track.getAverageCadence()
                + App.GENERAL_BUNDLE.getString("unit.hz"));
        valueCadenceMax.setText(track.getMaxCadence()
                + App.GENERAL_BUNDLE.getString("unit.hz"));

        valueDate.setText(track.getStartTime().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)));
        valueTime.setText(track.getStartTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)));
        valueActiveTime.setText(LocalTime.MIDNIGHT.plus(track.getMovingTime())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        valueTotalTime.setText(App.GENERAL_BUNDLE.getString("time.of")
                + LocalTime.MIDNIGHT.plus(track.getTotalDuration())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        if (track.getTotalDistance() > KILOMETER_CUTOFF) {
            valueDistance.setText(String.format("%.2f", track.getTotalDistance() / 1000)
                    + App.GENERAL_BUNDLE.getString("unit.km"));
        } else {
            valueDistance.setText(String.format("%.2f", track.getTotalDistance())
                    + App.GENERAL_BUNDLE.getString("unit.m"));
        }

        valueElevation.setText((int)(track.getTotalAscent() - track.getTotalDescend())
                + App.GENERAL_BUNDLE.getString("unit.m"));
        valueAscent.setText((int)track.getTotalAscent()
                + App.GENERAL_BUNDLE.getString("unit.m"));
        valueDescent.setText((int)track.getTotalDescend()
                + App.GENERAL_BUNDLE.getString("unit.m"));

        // create charts data
        XYChart.Series<Double, Double> elevationChartData = new XYChart.Series<>();
        XYChart.Series<Double, Double> speedChartData = new XYChart.Series<>();
        XYChart.Series<Double, Double> hrChartData = new XYChart.Series<>();
        XYChart.Series<Double, Double> cadenceChartData = new XYChart.Series<>();

        // traverse the chunks
        ObservableList<Chunk> chunks = track.getChunks();
        double currentDistance = 0.0;
        double currentHeight = chunks.get(0).getFirstPoint().getElevation();
        for (Chunk chunk : chunks) {
            currentDistance += chunk.getDistance();
            if (chunk.getDistance() < DISTANCE_EPSILON) continue;
            currentHeight += chunk.getAscent() - chunk.getDescend();

            elevationChartData.getData().add(new XYChart.Data<>(currentDistance, currentHeight));
            speedChartData.getData().add(new XYChart.Data<>(currentDistance, chunk.getSpeed()*3.6)); // m/s
            hrChartData.getData().add(new XYChart.Data<>(currentDistance, chunk.getAvgHeartRate()));
            cadenceChartData.getData().add(new XYChart.Data<>(currentDistance, chunk.getAvgCadence()));

            String zone;
            if (chunk.getAvgHeartRate() > 170) zone = App.GENERAL_BUNDLE.getString("zone.anaerobic");
            else if (chunk.getAvgHeartRate() > 150) zone = App.GENERAL_BUNDLE.getString("zone.threshold");
            else if (chunk.getAvgHeartRate() > 130) zone = App.GENERAL_BUNDLE.getString("zone.tempo");
            else if (chunk.getAvgHeartRate() > 110) zone = App.GENERAL_BUNDLE.getString("zone.endurance");
            else zone = App.GENERAL_BUNDLE.getString("zone.recovery");

            boolean pieFound = false;
            for (PieChart.Data d : zoneChart.getData()){
                if (d.getName().equals(zone)) {
                    pieFound = true;
                    d.setPieValue(d.getPieValue() + 1);
                }
            }
            if (!pieFound) zoneChart.getData().add( new PieChart.Data(zone, 1) );
            zoneChart.setStartAngle(90);
        }

        // populate the charts
        elevationChart.getData().add(elevationChartData);
        speedChart.getData().add(speedChartData);
        hrChart.getData().add(hrChartData);
        cadenceChart.getData().add(cadenceChartData);

        //initialize map
        mapView.addMapInializedListener(this);
    }

    private void load() throws JAXBException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("estraba files", "*.gpx"));
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) return;

        String name = file.getName();
        JAXBContext jaxbContext = JAXBContext.newInstance(GpxType.class, TrackPointExtensionT.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        @SuppressWarnings("unchecked")
        JAXBElement<Object> jaxbElement = (JAXBElement<Object>) unmarshaller.unmarshal(file);
        GpxType gpx = (GpxType) jaxbElement.getValue();

        if (gpx != null) {
            track = new TrackData(new Track(gpx.getTrk().get(0)));
            loadTrack(track);
            snackbar.show("GPX file: " + name + "successfully loaded", 3000);
        } else {
            snackbar.show("Error loading GPX file: " + name, 3000);
        }
    }

    @Override
    public void mapInitialized() {
        // When the JS init is done
        final int N = 0, S = 1, E = 2, W = 3;
        final double[] coord = new double[4];
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
                track.getChunks().get(0).getFirstPoint().getLatitude(),
                track.getChunks().get(0).getFirstPoint().getLongitude()
        ));
        track.getChunks().forEach(chunk -> {
            double lat = chunk.getLastPoint().getLatitude();
            double lon = chunk.getLastPoint().getLongitude();
            coord[N] = Math.max(lat, coord[N]);
            coord[S] = Math.min(lat, coord[S]);
            coord[E] = Math.max(lon, coord[E]);
            coord[W] = Math.min(lon, coord[W]);
            pathArray.push(new LatLong(lat, lon));
        });
        // Create and add the polyline using the array
        // This polyline displays instantly with no problem
        // TODO: add color with PolylineOptions.strokeColor("#ffff00") to match the color schemes of the app
        // When using that method, the line does not load properly, it needs an update to the zoom to show up.
        map.addMapShape(new Polyline(new PolylineOptions().path(pathArray)));
        // Adjust the map to the correct center and zoom
        map.fitBounds(new LatLongBounds(
                new LatLong(coord[S], coord[W]),
                new LatLong(coord[N], coord[E])
        ));
        map.setZoom(getBoundsZoomLevel(coord, mapView.getHeight(), mapView.getWidth()));
        // Print some debug info
        System.err.printf("Bound to coords: %.2fN, %.2S, %.2fE, %.2fW\n", coord[N], coord[S], coord[E], coord[W]);
        System.err.printf("Selected zoom: %d\n", map.getZoom());
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

