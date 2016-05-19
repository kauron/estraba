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
 */

package es.kauron.estraba;

import es.kauron.estraba.controller.DashboardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * @author baudlord
 */
public class App extends Application {
    public final static ResourceBundle GENERAL_BUNDLE = getBundle("general");
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("fxml/Dashboard.fxml"), GENERAL_BUNDLE);
        Parent root = loader.load();

        stage.getIcons().add(new Image(App.class.getResource("img/icon.png").toString()));
        stage.setTitle(GENERAL_BUNDLE.getString("app.title"));
        stage.setResizable(false);
        stage.setScene(new Scene(root));

        stage.show();
        loader.<DashboardController>getController().postinit();
    }
}
