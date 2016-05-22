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
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSpinner;
import es.kauron.estraba.App;
import es.kauron.estraba.model.DataBundle;
import javafx.application.Platform;
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
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(App.GENERAL_BUNDLE.getString("app.extension"), "*.gpx"));
        file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) {
            errorLoading();
            return;
        }
        loadGPXFile(file);
    }

    public void loadGPXFile(File file) {
        buttonLoad.setVisible(false);
        labelWelcome.setVisible(false);
        spinner.setVisible(true);
        snackbar.registerSnackbarContainer(root);
        snackbar.show("Loading file", 5000);
        Thread th = new Thread(new Task<DataBundle>() {
            @Override
            protected DataBundle call() throws Exception {
                return DataBundle.loadFrom(file);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                DataBundle bundle = getValue();
                if (bundle == null) errorLoading();
                FXMLLoader loader = new FXMLLoader(
                        App.class.getResource("fxml/Dashboard.fxml"), App.GENERAL_BUNDLE);
                Parent parent;
                try {
                    parent = loader.load();
                    loader.<DashboardController>getController().load(bundle);
                    ((Stage) root.getScene().getWindow()).setScene(new Scene(parent));
                } catch (IOException e) {
                    errorLoading();
                }
            }

            @Override
            protected void failed() {
                super.failed();
                errorLoading();
            }
        });

        th.setDaemon(true);
        th.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imgLogo.setImage(new Image(App.class.getResourceAsStream("img/header.png")));
        snackbar = new JFXSnackbar();

        imgLogo.setOnMouseClicked(e -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://www.github.com/kauron/estraba"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        });

        Platform.runLater(() -> root.getScene().setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            } else {
                e.consume();
            }
        }));

        // Dropping over surface
        Platform.runLater(() -> root.getScene().setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                String filePath = null;
                for (File file : db.getFiles()) {
                    filePath = file.getAbsolutePath();
                    System.out.println(filePath);
                }
            }
            e.setDropCompleted(success);
            e.consume();
        }));
    }

    private void errorLoading() {
        buttonLoad.setVisible(true);
        labelWelcome.setVisible(true);
        spinner.setVisible(false);
        snackbar.show("Error loading file", 3000);
    }
}

