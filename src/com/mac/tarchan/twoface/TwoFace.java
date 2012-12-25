/*
 * Copyright 2012 Takashi Ogura <tarchan at mac.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mac.tarchan.twoface;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX Application class
 *
 * @author Takashi Ogura <tarchan at mac.com>
 */
public class TwoFace extends Application {

    /**
     * TwoFace アプリケーションを開始します。
     *
     * @param stage Stage オブジェクト
     * @throws Exception FXML ファイルが読み込めない場合
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("TwoFace.fxml"));
        Parent root = (Parent) fxml.load();
        TwoFaceController controller = fxml.getController();

        // コマンドライン引数をユーザーデータに設定します。
        if (getParameters().getNamed().containsKey("file")) {
            root.setUserData(getParameters().getNamed().get("file"));
        }

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.titleProperty().bind(controller.titleBinding);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
