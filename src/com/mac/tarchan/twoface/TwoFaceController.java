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

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application.Parameters;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
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
    private PDFFile pdfFile;
//    private WritableImage page0, page1;
    private int origin = 1;
    @FXML
    private ListView<PageItem> thumbnail;
    @FXML
    private Pagination pagination;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        log.log(Level.INFO, "初期化します。: {0}", url);

        fileChooser = new FileChooser();
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF ファイル (*.pdf)", "*.pdf");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("すべてのファイル (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(pdfFilter);
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
     * @param idx ページ番号 (0 オリジン)
     * @return Pagination で表示する Node
     */
    private Node createPage(Integer idx) {
        log.log(Level.INFO, "createPage: {0} ページ", idx + 1);

        HBox hbox = new HBox();

        if (pdfFile == null) {
            Label label = new Label("ファイルを選択してください。");
            hbox.getChildren().add(label);
            return hbox;
        }

        Image page0 = getImage(idx - origin, null);
        Image page1 = getImage(idx - origin + 1, null);

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
     * 指定されたページ番号のイメージを返します。
     *
     * @param idx ページ番号
     * @param image イメージ
     * @return イメージ
     * @see SwingFXUtils#toFXImage(java.awt.image.BufferedImage,
     * javafx.scene.image.WritableImage)
     */
    private WritableImage getImage(int idx, WritableImage image) {
        idx++;
        if (idx <= 0 || idx > pdfFile.getNumPages()) {
            return null;
        }
        PDFPage pdfPage = pdfFile.getPage(idx);
        Rectangle2D rect = pdfPage.getBBox();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();
        java.awt.Image awtImage = pdfPage.getImage(width, height, rect, null, true, true);
        image = SwingFXUtils.toFXImage((BufferedImage) awtImage, image);
        return image;
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
            int idx = newValue.value;
            pagination.currentPageIndexProperty().setValue(idx);
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

            File file = param != null ? new File(param) : fileChooser.showOpenDialog(null);
            if (file == null) {
                return;
            }

            log.log(Level.INFO, "ファイルを開きます。: {0}", file);
            RandomAccessFile read = new RandomAccessFile(file, "r");
            FileChannel channel = read.getChannel();
            ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            pdfFile = new PDFFile(buf);

            pagination.setPageCount(pdfFile.getNumPages());

            ArrayList<PageItem> pages = new ArrayList<>();
            for (int i = 1; i < pdfFile.getNumPages(); i += 2) {
                pages.add(new PageItem(i));
            }
            ObservableList<PageItem> names = FXCollections.observableArrayList(pages);
            thumbnail.setItems(names);
            thumbnail.getSelectionModel().select(0);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "PDF ファイルを読み込めません。", ex);
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        log.log(Level.INFO, "アプリケーションを終了します。");
        Platform.exit();
    }

    @FXML
    private void handleFirst(ActionEvent event) {
        log.log(Level.INFO, "最初のページ: {0}", 0);
        pagination.currentPageIndexProperty().setValue(0);
    }

    @FXML
    private void handlePrev(ActionEvent event) {
        int idx = pagination.currentPageIndexProperty().getValue() - 2;
        log.log(Level.INFO, "前のページ: {0}", idx);
        pagination.currentPageIndexProperty().setValue(idx);
    }

    @FXML
    private void handleNext(ActionEvent event) {
        int idx = pagination.currentPageIndexProperty().getValue() + 2;
        log.log(Level.INFO, "次のページ: {0}", idx);
        pagination.currentPageIndexProperty().setValue(idx);
    }

    @FXML
    private void handleLast(ActionEvent event) {
        log.log(Level.INFO, "最後のページ: {0}", pagination.getPageCount());
        pagination.currentPageIndexProperty().setValue(pagination.getPageCount());
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

    public PageItem(int idx) {
        value = idx;
        name = String.format("%,d ページ", idx + 1);
    }

    /**
     * 文字列表現
     */
    @Override
    public String toString() {
        return name;
    }
}
