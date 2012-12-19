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
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.Pagination;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.SwipeEvent;
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
    private boolean face = true;
    private BooleanProperty faceProprty = new SimpleBooleanProperty(face) {
        @Override
        public boolean get() {
            log.log(Level.INFO, "faceProprty.get:");
            return face;
        }

        @Override
        public void set(boolean value) {
            log.log(Level.INFO, "faceProprty.set: {0}", value);
            face = value;
            // TODO set face
        }
    };
    private boolean cover = true;
    private BooleanProperty coverProprty = new SimpleBooleanProperty(cover) {
        @Override
        public boolean get() {
            log.log(Level.INFO, "coverProprty.get:");
            return cover;
        }

        @Override
        public void set(boolean value) {
            log.log(Level.INFO, "coverProprty.set: {0}", value);
            cover = value;
            setCover(value);
        }
    };
    private boolean right = true;
    private BooleanProperty rightProprty = new SimpleBooleanProperty(right) {
        @Override
        public boolean get() {
            log.log(Level.INFO, "rightProprty.get:");
            return right;
        }

        @Override
        public void set(boolean value) {
            log.log(Level.INFO, "rightProprty.set: {0}", value);
            right = value;
            // TODO set right
        }
    };
    private DoubleBinding viewHeight;
    @FXML
    private ListView<PageItem> thumbnail;
    @FXML
    private Pagination pagination;
    @FXML
    private RadioMenuItem twoFaceMenu;
    @FXML
    private RadioMenuItem withCoverMenu;
    @FXML
    private RadioMenuItem rightDirectionMenu;

    /**
     * コントローラを初期化します。
     *
     * @param url root 要素の URL
     * @param rb ローカライズリソース
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
            public Node call(Integer index) {
                return createPage(index);
            }
        });
        pagination.setPageCount(1);

        viewHeight = new DoubleBinding() {
            {
                super.bind(pagination.widthProperty(), pagination.heightProperty());
            }

            @Override
            protected double computeValue() {
                // TODO ウインドウ最大化のとき再計算されない
//                double gap = pagination.heightProperty().get() - pagination.getBaselineOffset();
                double gap = 61;
                return pagination.heightProperty().get() - gap;
            }
        };

        thumbnail.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PageItem>() {
            @Override
            public void changed(ObservableValue<? extends PageItem> self, PageItem oldValue, PageItem newValue) {
                setCurrentPage(newValue);
            }
        });

        twoFaceMenu.selectedProperty().bindBidirectional(faceProprty);
        withCoverMenu.selectedProperty().bindBidirectional(coverProprty);
        rightDirectionMenu.selectedProperty().bindBidirectional(rightProprty);
    }

    /**
     * インデックスを設定します。
     *
     * @param withCover 表紙ありの場合は true
     */
    private void setCover(boolean withCover) {
        log.log(Level.INFO, "setCover: {0}", withCover);
        if (book == null) {
            return;
        }

        origin = withCover ? 0 : 1;
        int index = thumbnail.getSelectionModel().getSelectedIndex();
        int pageCount = (book.getPageCount() - origin) / 2 + 1;
        log.log(Level.INFO, "thumbnail: {0} ({1})", new Object[]{book.getPageCount(), pageCount});

        ArrayList<PageItem> pages = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            pages.add(new PageItem(i * 2, origin));
        }
        ObservableList<PageItem> names = FXCollections.observableArrayList(pages);
        thumbnail.setItems(names);
        thumbnail.getSelectionModel().select(index);
        thumbnail.requestFocus();
        pagination.layout();
    }

    /**
     * サムネールで選択されたページを表示します。
     *
     * @param page ページ
     */
    private void setCurrentPage(PageItem page) {
        log.log(Level.INFO, "selectPage: {0}", page);
        if (page != null) {
            int index = page.value;
            log.log(Level.INFO, "index={0}", index);
            pagination.currentPageIndexProperty().setValue(index);
        }
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
            Label label = new Label(lastError != null ? "ファイルを読み込めません。: " + lastError : "ファイルを選択してください。");
            hbox.getChildren().add(label);
            return hbox;
        }

        Image page0 = book.getImage(index + origin - 1);
        Image page1 = book.getImage(index + origin);

        ImageView view0 = new ImageView(page0);
        view0.fitHeightProperty().bind(viewHeight);
        view0.setPreserveRatio(true);
        view0.setSmooth(true);
        view0.setCache(true);

        ImageView view1 = new ImageView(page1);
        view1.fitHeightProperty().bind(viewHeight);
        view1.setPreserveRatio(true);
        view1.setSmooth(true);
        view1.setCache(true);

        hbox.getChildren().add(view1);
        hbox.getChildren().add(view0);
        return hbox;
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

            setCover(coverProprty.get());
            thumbnail.getSelectionModel().select(0);
            thumbnail.requestFocus();

            int pageCount = book.getPageCount() / 2 * 2 + 1;
            log.log(Level.INFO, "pagination: {0} ({1})", new Object[]{book.getPageCount(), pageCount});
            pagination.setPageCount(pageCount);
            pagination.layout();
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
    private void handleCopy(ActionEvent event) {
        log.log(Level.INFO, "コピー");
        PageItem page = thumbnail.getSelectionModel().getSelectedItem();
        if (page == null) {
            return;
        }

        String text = page.name;
        Image image = book.getImage(page.value + origin);

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        if (text != null) {
            content.putString(text);
        }
        if (image != null) {
            content.putImage(image);
        }
        clipboard.setContent(content);
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

    @FXML
    private void handleSwipeLeft(SwipeEvent event) {
        log.log(Level.INFO, "handleSwipeLeft: {0}", event);
    }

    @FXML
    private void handleSwipeRight(SwipeEvent event) {
        log.log(Level.INFO, "handleSwipeRight: {0}", event);
    }

    @FXML
    private void handleSwipeUp(SwipeEvent event) {
        log.log(Level.INFO, "handleSwipeUp: {0}", event);
    }

    @FXML
    private void handleSwipeDown(SwipeEvent event) {
        log.log(Level.INFO, "handleSwipeDown: {0}", event);
    }

    /**
     * ページアイテム
     *
     * @author tarchan
     */
    public static class PageItem {

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

        /**
         * ページアイテムを構築します。
         *
         * @param page ページ番号
         * @param origin ページ基点
         */
        /**
         *
         * @param page
         * @param origin
         */
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
}
