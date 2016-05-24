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
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


public class DataBundle {
    private static final double DISTANCE_EPSILON = 1E-6;
    private static final double KILOMETER_CUTOFF = 10000;
    
    public String HRAvg, HRMax, HRMin, speedAvg, speedMax, cadenceAvg, cadenceMax;
    public String date, time, activeTime, totalTime, distance, elevation, ascent, descent;
    public XYChart.Series<Double, Double> elevationSeries, speedSeries, hrSeries, cadenceSeries;
    public XYChart.Series<Long, Double> elevationTSeries, speedTSeries, hrTSeries, cadenceTSeries;
    public ObservableList<PieChart.Data> pieData;
    public ObservableList<Chunk> chunks;

    private DataBundle(TrackData track, int maxHR) {

        HRAvg = track.getAverageHeartrate() + App.GENERAL_BUNDLE.getString("unit.bpm");
        HRMax = track.getMaxHeartrate() + App.GENERAL_BUNDLE.getString("unit.bpm");
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
        ascent = "+ " + (int) track.getTotalAscent() + App.GENERAL_BUNDLE.getString("unit.m");
        descent = "- " + (int) track.getTotalDescend() + App.GENERAL_BUNDLE.getString("unit.m");

        // traverse the chunks
        chunks = track.getChunks();
        double currentDistance = 0.0;
        Duration currentTime = Duration.ZERO;
        double currentHeight = chunks.get(0).getFirstPoint().getElevation();

        elevationSeries = new XYChart.Series<>();
        elevationTSeries = new XYChart.Series<>();
        cadenceSeries = new XYChart.Series<>();
        cadenceTSeries = new XYChart.Series<>();
        hrSeries = new XYChart.Series<>();
        hrTSeries = new XYChart.Series<>();
        speedSeries = new XYChart.Series<>();
        speedTSeries = new XYChart.Series<>();
        pieData = FXCollections.observableArrayList();

        for (Chunk chunk : chunks) {
            currentDistance += chunk.getDistance();
            currentTime = currentTime.plus(chunk.getMovingTime());
            if (chunk.getDistance() < DISTANCE_EPSILON ||
                    chunk.getMovingTime().getSeconds() < 1) continue;
            currentHeight += chunk.getAscent() - chunk.getDescend();

            elevationSeries.getData().add(new XYChart.Data<>(currentDistance, currentHeight));
            elevationTSeries.getData().add(new XYChart.Data<>(currentTime.toMinutes(), currentHeight));
            speedSeries.getData().add(new XYChart.Data<>(currentDistance, chunk.getSpeed()*3.6)); // m/s
            speedTSeries.getData().add(new XYChart.Data<>(currentTime.toMinutes(), chunk.getSpeed() * 3.6)); // m/s
            hrSeries.getData().add(new XYChart.Data<>(currentDistance, chunk.getAvgHeartRate()));
            hrTSeries.getData().add(new XYChart.Data<>(currentTime.toMinutes(), chunk.getAvgHeartRate()));
            cadenceSeries.getData().add(new XYChart.Data<>(currentDistance, chunk.getAvgCadence()));
            cadenceTSeries.getData().add(new XYChart.Data<>(currentTime.toMinutes(), chunk.getAvgCadence()));

            String zone;
            if (chunk.getAvgHeartRate() > maxHR * .9) zone = App.GENERAL_BUNDLE.getString("zone.anaerobic");
            else if (chunk.getAvgHeartRate() > maxHR * .8) zone = App.GENERAL_BUNDLE.getString("zone.threshold");
            else if (chunk.getAvgHeartRate() > maxHR * .7) zone = App.GENERAL_BUNDLE.getString("zone.tempo");
            else if (chunk.getAvgHeartRate() > maxHR * .6) zone = App.GENERAL_BUNDLE.getString("zone.endurance");
            else zone = App.GENERAL_BUNDLE.getString("zone.recovery");

            boolean pieFound = false;
            for (PieChart.Data d : pieData){
                if (d.getName().equals(zone)) {
                    pieFound = true;
                    d.setPieValue(d.getPieValue() + 1);
                    break;
                }
            }
            if (!pieFound) pieData.add( new PieChart.Data(zone, 1) );
        }
    }

    public static DataBundle loadFrom(File file, int maxHR) throws Exception {
        JAXBElement<Object> jaxbElement;
        JAXBContext jaxbContext = JAXBContext.newInstance(GpxType.class, TrackPointExtensionT.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        jaxbElement = (JAXBElement<Object>) unmarshaller.unmarshal(file);
        GpxType gpx = (GpxType) jaxbElement.getValue();

        if (gpx == null) throw new Exception();
        return new DataBundle(new TrackData(new Track(gpx.getTrk().get(0))), maxHR);
    }
}
