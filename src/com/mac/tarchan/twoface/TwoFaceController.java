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

import com.mac.tarchan.twoface.book.Book;
import com.mac.tarchan.twoface.book.BookFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Pagination;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Takashi Ogura <tarchan at mac.com>
 */
public class TwoFaceController implements Initializable {

    private static final Logger log = Logger.getLogger(TwoFaceController.class.getName());
    private FileChooser fileChooser;
    private Book book;
    private int origin = 0;
    private Exception lastError;
    @FXML
    private ListView<PageItem> thumbnail;
    @FXML
    private Pagination pagination;
    @FXML
    private ToggleGroup faceGroup;
    @FXML
    private ToggleGroup originGroup;
    @FXML
    private ToggleGroup directionGroup;
    @FXML
    private RadioMenuItem twoFace;
    @FXML
    private RadioMenuItem withCover;
    @FXML
    private RadioMenuItem rightDirection;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        log.log(Level.INFO, "初期化します。: {0}", url);

        fileChooser = new FileChooser();
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF ファイル (*.pdf)", "*.pdf");
        FileChooser.ExtensionFilter zipFilter = new FileChooser.ExtensionFilter("ZIP ファイル (*.zip)", "*.zip");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("すべてのファイル (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(pdfFilter);
        fileChooser.getExtensionFilters().add(zipFilter);
        fileChooser.getExtensionFilters().add(allFilter);

        pagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer idx) {
                return createPage(idx);
            }
        });
        pagination.setPageCount(1);

        MultipleSelectionModel<PageItem> model = thumbnail.getSelectionModel();
        model.selectedItemProperty().addListener(new ChangeListener<PageItem>() {
            @Override
            public void changed(ObservableValue<? extends PageItem> property, PageItem oldValue, PageItem newValue) {
                onChanged(property, oldValue, newValue);
            }
        });
    }

    /**
     * Pagination で表示する Node を返します。
     *
     * @param index ページ番号 (0 オリジン)
     * @return Pagination で表示する Node
     */
    private Node createPage(Integer index) {
        log.log(Level.INFO, "createPage: index={0} ({1})", new Object[]{index, origin});

        HBox hbox = new HBox();

        if (book == null) {
            Label label = lastError != null ? new Label("ファイルを読み込めません。: " + lastError) : new Label("ファイルを選択してください。");
            hbox.getChildren().add(label);
            return hbox;
        }

        Image page0 = book.getImage(index + origin);
        Image page1 = book.getImage(index + origin + 1);

        ImageView view0 = new ImageView(page0);
        view0.setFitWidth(pagination.getWidth() / 2);
        view0.setPreserveRatio(true);
        view0.setSmooth(true);
        view0.setCache(true);

        ImageView view1 = new ImageView(page1);
        view1.setFitWidth(pagination.getWidth() / 2);
        view1.setPreserveRatio(true);
        view1.setSmooth(true);
        view1.setCache(true);

        hbox.getChildren().add(view1);
        hbox.getChildren().add(view0);
        return hbox;
    }

    /**
     * サムネールで選択されたページを表示します。
     *
     * @param property プロパティ
     * @param oldValue 古い値
     * @param newValue 新しい値
     */
    private void onChanged(ObservableValue<? extends PageItem> property, PageItem oldValue, PageItem newValue) {
        log.log(Level.INFO, "onChanged: {0}, {1} -> {2}", new Object[]{property, oldValue, newValue});
        if (newValue != null) {
            int page = newValue.value;
            log.log(Level.INFO, "page={0}", page);
            pagination.currentPageIndexProperty().setValue(page);
        }
    }

    /**
     * root 要素を取得します。
     *
     * @return root 要素
     */
    private Parent getRoot() {
        Parent root = pagination.getParent();
        while (root.getParent() != null) {
            log.log(Level.INFO, "parent={0}", root.getClass());
            log.log(Level.INFO, "userData={0}", root.getUserData());
            root = root.getParent();
        }
        log.log(Level.INFO, "root={0}", root.getClass());
        log.log(Level.INFO, "userData={0}", root.getUserData());
        return root;
    }

    @FXML
    private void handleOpen(ActionEvent event) {
        try {
            log.log(Level.INFO, "ファイルを選択します。");

            Parent root = getRoot();
            String param = (String) root.getUserData();
            log.log(Level.INFO, "パラメータ={0}", param);

            File file = param != null ? new File(param) : fileChooser.showOpenDialog(null);
            if (file == null) {
                return;
            }

            log.log(Level.INFO, "ファイルを開きます。: {0}", file);
            book = BookFactory.getBook(file);

            pagination.setPageCount(book.getPageCount());

            ArrayList<PageItem> pages = new ArrayList<>();
            origin = withCover.isSelected() ? 0 : 1;
            for (int i = 0; i < book.getPageCount(); i += 2) {
                pages.add(new PageItem(i, origin));
            }
            ObservableList<PageItem> names = FXCollections.observableArrayList(pages);
            thumbnail.setItems(names);
            thumbnail.getSelectionModel().select(0);
            thumbnail.requestFocus();
        } catch (IOException ex) {
            log.log(Level.SEVERE, "ファイルを読み込めません。", ex);
            lastError = ex;
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        log.log(Level.INFO, "アプリケーションを終了します。");
        Platform.exit();
    }

    @FXML
    private void handleFirst(ActionEvent event) {
        thumbnail.getSelectionModel().select(0);
    }

    @FXML
    private void handlePrev(ActionEvent event) {
        int index = thumbnail.getSelectionModel().getSelectedIndex() - 1;
        thumbnail.getSelectionModel().select(index);
    }

    @FXML
    private void handleNext(ActionEvent event) {
        int index = thumbnail.getSelectionModel().getSelectedIndex() + 1;
        thumbnail.getSelectionModel().select(index);
    }

    @FXML
    private void handleLast(ActionEvent event) {
        int index = thumbnail.getItems().size() - 1;
        thumbnail.getSelectionModel().select(index);
    }
}

/**
 * ページアイテム
 *
 * @author tarchan
 */
class PageItem {

    /**
     * ページ番号
     */
    public int value;
    /**
     * ページ名
     */
    public String name;
    /**
     * サムネール
     */
    public Image image;

    public PageItem(int page, int origin) {
        value = page;
        name = page + origin != 0 ? String.format("%,d ページ", page + origin) : "表紙";
    }

    /**
     * 文字列表現
     */
    @Override
    public String toString() {
        return name;
    }
}
