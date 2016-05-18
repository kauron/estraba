package es.kauron.estraba.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSnackbar;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MVCArray;
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
    private JFXListView<?> listLeft;

    @FXML
    private PieChart zoneChart;

    @FXML
    private JFXListView<?> listRight;

    @FXML
    private Label motivationLabel;

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
    private AreaChart<Number, Number> elevationChart;

    @FXML
    private LineChart<Number, Number> speedChart;

    @FXML
    private LineChart<Number, Number> hrChart;

    @FXML
    private LineChart<Number, Number> cadenceChart;

    @FXML
    private Tab tabSettings;

    private TrackData trackData;
    private JFXSnackbar snackbar;
    private final double DISTANCE_EPSILON = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // populate map icons
        ((ImageView)elevationButton.getGraphic()).setImage(new Image(App.class.getResourceAsStream("img/elevation.png")));
        ((ImageView)speedButton.getGraphic()).setImage(new Image(App.class.getResourceAsStream("img/speed.png")));
        ((ImageView)hrButton.getGraphic()).setImage(new Image(App.class.getResourceAsStream("img/hr.png")));
        ((ImageView)cadenceButton.getGraphic()).setImage(new Image(App.class.getResourceAsStream("img/cadence.png")));


    }

    @FXML
    private void onMapButton(ActionEvent event){
        System.out.println(((JFXButton)event.getSource()).getId());
    }

    public void postinit() {
        snackbar = new JFXSnackbar();
        snackbar.registerSnackbarContainer(root);
        try {load();} catch (JAXBException e) {e.printStackTrace();}
    }

    private void loadTrack(TrackData track) {
        // populate dashboard
        trackData.getStartTime();
        trackData.getTotalDuration();
        trackData.getMovingTime();
        trackData.getTotalDistance();
        trackData.getTotalAscent();
        trackData.getTotalDescend();
        trackData.getMaxSpeed();
        trackData.getAverageSpeed();
        trackData.getMaxHeartrate();
        trackData.getMinHeartRate();
        trackData.getAverageHeartrate();
        trackData.getMaxCadence();
        trackData.getAverageCadence();

        // create charts data
        XYChart.Series<Number, Number> elevationChartData = new XYChart.Series<>();
        XYChart.Series<Number, Number> speedChartData = new XYChart.Series<>();
        XYChart.Series<Number, Number> hrChartData = new XYChart.Series<>();
        XYChart.Series<Number, Number> cadenceChartData = new XYChart.Series<>();
        MVCArray pathArray = new MVCArray();

        ObservableList<Chunk> chunks = trackData.getChunks();
        double currentDistance = 0.0;
        for (Chunk chunk : chunks) {
            currentDistance += chunk.getDistance();
            if (chunk.getDistance() < DISTANCE_EPSILON) continue;

            pathArray.push(new LatLong(chunk.getLastPoint().getLatitude(), chunk.getLastPoint().getLongitude()));
            elevationChartData.getData().add(new XYChart.Data<>(currentDistance, chunk.getAscent()));
            speedChartData.getData().add(new XYChart.Data<>(currentDistance, chunk.getSpeed()));
            hrChartData.getData().add(new XYChart.Data<>(currentDistance, chunk.getAvgHeartRate()));
            cadenceChartData.getData().add(new XYChart.Data<>(currentDistance, chunk.getAvgCadence()));
        }

        // populate the charts
        elevationChart.getData().add(elevationChartData);
        speedChart.getData().add(speedChartData);
        hrChart.getData().add(hrChartData);
        cadenceChart.getData().add(cadenceChartData);

        // populate and render the map
        GoogleMap map = mapView.createMap();
        map.addMapShape(new Polyline(
                new PolylineOptions()
                        .path(pathArray)
                        .strokeColor("red")
                        .strokeWeight(2))
        );

    }

    @FXML
    private void load() throws JAXBException {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) return;

        String name = file.getName();
        JAXBContext jaxbContext = JAXBContext.newInstance(GpxType.class, TrackPointExtensionT.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<Object> jaxbElement = (JAXBElement<Object>) unmarshaller.unmarshal(file);
        GpxType gpx = (GpxType) jaxbElement.getValue();

        if (gpx != null) {
            loadTrack(new TrackData(new Track(gpx.getTrk().get(0))));
            snackbar.show("GPX file: " + name + "successfully loaded", 3000);
        } else {
            snackbar.show("Error loading GPX file: " + name, 3000);
        }
    }

}

