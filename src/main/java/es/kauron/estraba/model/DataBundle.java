package es.kauron.estraba.model;

import es.kauron.estraba.App;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import jgpx.model.analysis.Chunk;
import jgpx.model.analysis.TrackData;
import jgpx.model.gpx.Track;
import jgpx.model.jaxb.GpxType;
import jgpx.model.jaxb.TrackPointExtensionT;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class DataBundle {
    public static final int N = 0, S = 1, E = 2, W = 3;
    private static final double DISTANCE_EPSILON = 1E-6;
    private static final double KILOMETER_CUTOFF = 10000;
    
    public String HRAvg, HRMax, HRMin, speedAvg, speedMax, cadenceAvg, cadenceMax;
    public String date, time, activeTime, totalTime, distance, elevation, ascent, descent;
    public XYChart.Series<Double, Double> elevationSeries = new XYChart.Series<>(),
            speedSeries = new XYChart.Series<>(),
            hrSeries = new XYChart.Series<>(), 
            cadenceSeries = new XYChart.Series<>();
    public ObservableList<PieChart.Data> pieData = FXCollections.emptyObservableList();
    public ObservableList<Chunk> chunks;

    public static DataBundle loadFrom(File file) throws Exception {
        String name = file.getName();
        JAXBElement<Object> jaxbElement;
        JAXBContext jaxbContext = JAXBContext.newInstance(GpxType.class, TrackPointExtensionT.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        jaxbElement = (JAXBElement<Object>) unmarshaller.unmarshal(file);
        GpxType gpx = (GpxType) jaxbElement.getValue();

        if (gpx == null) throw new Exception();
        return new DataBundle(new TrackData(new Track(gpx.getTrk().get(0))));
    }
    
    private DataBundle(TrackData track) {
        HRAvg = track.getAverageHeartrate() + App.GENERAL_BUNDLE.getString("unit.bpm");
        HRMax = (track.getMaxHeartrate() + App.GENERAL_BUNDLE.getString("unit.bpm"));
        HRMin = track.getMinHeartRate() + App.GENERAL_BUNDLE.getString("unit.bpm");

        // speed is given as m/s
        speedAvg = String.format("%.2f", track.getAverageSpeed() * 3.6) + App.GENERAL_BUNDLE.getString("unit.kmph");
        speedMax = String.format("%.2f", track.getMaxSpeed() * 3.6) + App.GENERAL_BUNDLE.getString("unit.kmph");

        cadenceAvg = track.getAverageCadence() + App.GENERAL_BUNDLE.getString("unit.hz");
        cadenceMax = track.getMaxCadence() + App.GENERAL_BUNDLE.getString("unit.hz");

        date = track.getStartTime().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));
        time = track.getStartTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM));
        activeTime = LocalTime.MIDNIGHT.plus(track.getMovingTime()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        totalTime = App.GENERAL_BUNDLE.getString("time.of")
                + LocalTime.MIDNIGHT.plus(track.getTotalDuration()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        if (track.getTotalDistance() > KILOMETER_CUTOFF) {
            distance = String.format("%.2f", track.getTotalDistance() / 1000) + App.GENERAL_BUNDLE.getString("unit.km");
        } else {
            distance = String.format("%.2f", track.getTotalDistance()) + App.GENERAL_BUNDLE.getString("unit.m");
        }

        elevation = (int)(track.getTotalAscent() - track.getTotalDescend()) + App.GENERAL_BUNDLE.getString("unit.m");
        ascent = (int)track.getTotalAscent() + App.GENERAL_BUNDLE.getString("unit.m");
        descent = (int)track.getTotalDescend() + App.GENERAL_BUNDLE.getString("unit.m");

        // traverse the chunks
        chunks = track.getChunks();
        double currentDistance = 0.0;
        double currentHeight = chunks.get(0).getFirstPoint().getElevation();
        for (Chunk chunk : chunks) {
            currentDistance += chunk.getDistance();
            if (chunk.getDistance() < DISTANCE_EPSILON) continue;
            currentHeight += chunk.getAscent() - chunk.getDescend();

            elevationSeries.getData().add(new XYChart.Data<>(currentDistance, currentHeight));
            speedSeries.getData().add(new XYChart.Data<>(currentDistance, chunk.getSpeed()*3.6)); // m/s
            hrSeries.getData().add(new XYChart.Data<>(currentDistance, chunk.getAvgHeartRate()));
            cadenceSeries.getData().add(new XYChart.Data<>(currentDistance, chunk.getAvgCadence()));

            String zone;
            if (chunk.getAvgHeartRate() > 170) zone = App.GENERAL_BUNDLE.getString("zone.anaerobic");
            else if (chunk.getAvgHeartRate() > 150) zone = App.GENERAL_BUNDLE.getString("zone.threshold");
            else if (chunk.getAvgHeartRate() > 130) zone = App.GENERAL_BUNDLE.getString("zone.tempo");
            else if (chunk.getAvgHeartRate() > 110) zone = App.GENERAL_BUNDLE.getString("zone.endurance");
            else zone = App.GENERAL_BUNDLE.getString("zone.recovery");

            boolean pieFound = false;
            for (PieChart.Data d : pieData){
                if (d.getName().equals(zone)) {
                    pieFound = true;
                    d.setPieValue(d.getPieValue() + 1);
                }
            }
            if (!pieFound) pieData.add( new PieChart.Data(zone, 1) );
        }
    }
}
