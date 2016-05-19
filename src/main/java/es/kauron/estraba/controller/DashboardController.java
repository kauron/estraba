package es.kauron.estraba.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.lynden.gmapsfx.GoogleMapView;
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

public class DashboardController implements Initializable {

    @FXML
    private AnchorPane root;

    @FXML
    private Tab tabDashboard;

    @FXML
    private Label labelMotivationUpper;

    @FXML
    private ImageView imgHR;

    @FXML
    private Label valueHRAvg;

    @FXML
    private Label valueHRMin;

    @FXML
    private Label valueHRMax;

    @FXML
    private ImageView imgSpeed;

    @FXML
    private Label valueSpeedAvg;

    @FXML
    private Label valueSpeedMax;

    @FXML
    private ImageView imgCadence;

    @FXML
    private Label valueCadenceAvg;

    @FXML
    private Label valueCadenceMax;

    @FXML
    private PieChart zoneChart;

    @FXML
    private Label valueDate;

    @FXML
    private Label valueTime;

    @FXML
    private Label valueActiveTime;

    @FXML
    private Label valueTotalTime;

    @FXML
    private ImageView imgDate;

    @FXML
    private Label valueDistance;

    @FXML
    private ImageView imgDistance;

    @FXML
    private Label valueElevation;

    @FXML
    private Label valueAscent;

    @FXML
    private Label valueDescent;

    @FXML
    private ImageView imgElevation;

    @FXML
    private Label labelMotivationLower;

    @FXML
    private Tab tabMap;

    @FXML
    private GoogleMapView mapView;

    @FXML
    private JFXButton elevationButton;

    @FXML
    private JFXButton speedButton;

    @FXML
    private JFXButton hrButton;

    @FXML
    private JFXButton cadenceButton;

    @FXML
    private LineChart<Double, Double> mapChart;

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

    private JFXSnackbar snackbar;
    private final double DISTANCE_EPSILON = 10;
    private final double KILOMETER_CUTOFF = 10000;

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

        valueSpeedAvg.setText(String.format("%.2f", track.getAverageSpeed())
                + App.GENERAL_BUNDLE.getString("unit.kmph"));
        valueSpeedMax.setText(String.format("%.2f", track.getMaxSpeed())
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
        // MVCArray pathArray = new MVCArray();


        // traverse the chunks
        ObservableList<Chunk> chunks = track.getChunks();
        double currentDistance = 0.0;
        double currentHeight = chunks.get(0).getFirstPoint().getElevation();
        for (Chunk chunk : chunks) {
            currentDistance += chunk.getDistance();
            if (chunk.getDistance() < DISTANCE_EPSILON) continue;
            currentHeight += chunk.getAscent() - chunk.getDescend();

            // pathArray.push(new LatLong(chunk.getLastPoint().getLatitude(), chunk.getLastPoint().getLongitude()));
            elevationChartData.getData().add(new XYChart.Data<>(currentDistance, currentHeight));
            speedChartData.getData().add(new XYChart.Data<>(currentDistance, chunk.getSpeed()));
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
        }

        // populate the charts
        elevationChart.getData().add(elevationChartData);
        speedChart.getData().add(speedChartData);
        hrChart.getData().add(hrChartData);
        cadenceChart.getData().add(cadenceChartData);

        // populate and render the map
        //GoogleMap map = mapView.createMap();
        //map.addMapShape(new Polyline(
        //        new PolylineOptions()
        //                .path(pathArray)
        //                .strokeColor("red")
        //                .strokeWeight(2))
        //);

    }

    @FXML
    private void load() throws JAXBException {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) return;

        String name = file.getName();
        JAXBContext jaxbContext = JAXBContext.newInstance(GpxType.class, TrackPointExtensionT.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        @SuppressWarnings("unchecked")
        JAXBElement<Object> jaxbElement = (JAXBElement<Object>) unmarshaller.unmarshal(file);
        GpxType gpx = (GpxType) jaxbElement.getValue();

        if (gpx != null) {
            loadTrack(new TrackData(new Track(gpx.getTrk().get(0))));
            //snackbar.show("GPX file: " + name + "successfully loaded", 3000);
        } else {
            //snackbar.show("Error loading GPX file: " + name, 3000);
        }
    }

}

