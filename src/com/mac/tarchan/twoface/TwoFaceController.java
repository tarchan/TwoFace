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
import com.mac.tarchan.twoface.book.Books;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Takashi Ogura <tarchan at mac.com>
 */
public class TwoFaceController implements Initializable {

    private static final Logger log = Logger.getLogger(TwoFaceController.class.getName());
    private FileChooser fileChooser = new FileChooser();;
    private Book book;
    private ObjectProperty<Book> bookProperty = new SimpleObjectProperty<>(this, "book");
    private int origin = 0;
    private Exception lastError;
    private BooleanProperty faceProperty = new SimpleBooleanProperty(this, "face", true) {
        @Override
        protected void invalidated() {
            log.log(Level.INFO, "faceProperty: {0}", get());
            updateIndex();
//            pagination.requestLayout();
        }
    };
    private BooleanProperty coverProperty = new SimpleBooleanProperty(this, "cover", true) {
        @Override
        protected void invalidated() {
            log.log(Level.INFO, "coverProperty: {0}", get());
            updateIndex();
//            pagination.requestLayout();
            currentPane.getChildren().removeAll(currentPane.getChildren());
            currentPane.getChildren().addAll(createChildren(pagination.getCurrentPageIndex()));
        }
    };
    private BooleanProperty rightProperty = new SimpleBooleanProperty(this, "right", true) {
        @Override
        protected void invalidated() {
            log.log(Level.INFO, "coverProperty: {0}", get());
            if (currentPane.getChildren().size() == 2) {
                final Node[] children = new Node[2];
                currentPane.getChildren().toArray(children);

                // FIXME TranslateTransitionの影響をリセットする
//                log.log(Level.INFO, "getLayoutX: {0}, {1}", new Object[]{children[0].getLayoutX(), children[1].getLayoutX()});
//                final TranslateTransition tt0 = new TranslateTransition(Duration.millis(2000), children[0]);
////                tt0.setFromX(children[0].getLayoutX());
//                tt0.setByX(children[1].getLayoutX() - children[0].getLayoutX());
//                final TranslateTransition tt1 = new TranslateTransition(Duration.millis(2000), children[1]);
//                tt1.setByX(children[0].getLayoutX() - children[1].getLayoutX());
////                tt1.setToX(children[0].getLayoutX());
////                tt0.play();
////                tt1.play();
//                ParallelTransition parallel = new ParallelTransition(tt0, tt1);
//                parallel.setOnFinished(new EventHandler<ActionEvent>() {
//                    @Override
//                    public void handle(ActionEvent t) {
//                        currentPane.getChildren().removeAll(currentPane.getChildren());
//                        log.log(Level.INFO, "setOnFinished: {0}, {1}", new Object[]{children[0].getLayoutX(), children[1].getLayoutX()});
////                        double x0 = children[0].getLayoutX();
////                        children[0].setLayoutX(children[1].getLayoutX());
////                        children[1].setLayoutX(x0);
////                        tt0.setByX(children[1].getLayoutX());
////                        tt1.setByX(x0);
//                        currentPane.getChildren().addAll(children[1], children[0]);
//                    }
//                });
//                parallel.play();

                currentPane.getChildren().removeAll(currentPane.getChildren());
                currentPane.getChildren().addAll(children[1], children[0]);
            }
        }
    };
    private ObjectProperty<File> fileProperty = new SimpleObjectProperty<>(this, "file");
    private GetBookService bookService = new GetBookService();
    public StringBinding titleBinding;
    Pane currentPane;
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
    @FXML
    private ToggleGroup faceGroup;
    @FXML
    private ToggleGroup originGroup;
    @FXML
    private ToggleGroup directionGroup;
    @FXML
    private TitledPane x1;
    @FXML
    private Region veil;
    @FXML
    private ProgressIndicator loading;

    /**
     * コントローラを初期化します。
     *
     * @param url root 要素の URL
     * @param rb ローカライズリソース
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        log.log(Level.INFO, "初期化します。: {0}", url);
        log.log(Level.INFO, "javafx.version: {0}", System.getProperty("javafx.version"));

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

        titleBinding = new StringBinding() {
            {
                super.bind(fileProperty, thumbnail.getSelectionModel().selectedItemProperty());
            }

            @Override
            protected String computeValue() {
                log.log(Level.INFO, "titleBinding: {0} ({1})", new Object[]{fileProperty, thumbnail.getSelectionModel().selectedItemProperty()});
                return fileProperty.isNull().get() ? "TwoFace" : String.format("(%s) %s - TwoFace", thumbnail.getSelectionModel().selectedItemProperty().get(), fileProperty.get().getName());
            }
        };

        thumbnail.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PageItem>() {
            @Override
            public void changed(ObservableValue<? extends PageItem> self, PageItem oldValue, PageItem newValue) {
                setCurrentPage(newValue);
            }
        });

        bookProperty.bind(bookService.valueProperty());
        bookService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                book = bookProperty.get();

                updateIndex();
                thumbnail.getSelectionModel().select(0);
                thumbnail.requestFocus();

                int pageCount = book.getPageCount() / 2 * 2 + 1;
                log.log(Level.INFO, "pagination: {0} ({1})", new Object[]{book.getPageCount(), pageCount});
                pagination.setPageCount(pageCount);
            }
        });
        veil.visibleProperty().bind(bookService.runningProperty());
        loading.visibleProperty().bind(bookService.runningProperty());
        loading.progressProperty().bind(bookService.progressProperty());

        twoFaceMenu.selectedProperty().bindBidirectional(faceProperty);
        withCoverMenu.selectedProperty().bindBidirectional(coverProperty);
        rightDirectionMenu.selectedProperty().bindBidirectional(rightProperty);
    }

    /**
     * インデックスを設定します。
     */
    private void updateIndex() {
        log.log(Level.INFO, "updateIndex: {0}", coverProperty.get());
        if (book == null) {
            return;
        }

        origin = coverProperty.get() ? 0 : 1;
        int index = thumbnail.getSelectionModel().getSelectedIndex();
        ArrayList<PageItem> pages = new ArrayList<>();

        if (faceProperty.get()) {
            int pageCount = (book.getPageCount() - origin) / 2 + 1;
            log.log(Level.INFO, "thumbnail: {0} ({1})", new Object[]{book.getPageCount(), pageCount});

            for (int i = 0; i < pageCount; i++) {
                pages.add(new PageItem(i * 2, origin));
            }
        } else {
            int pageCount = book.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                pages.add(new PageItem(i, origin));
            }
        }

        ObservableList<PageItem> names = FXCollections.observableArrayList(pages);
        thumbnail.setItems(names);
        thumbnail.getSelectionModel().select(index);
        thumbnail.requestFocus();
        pagination.requestLayout();
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
            pagination.currentPageIndexProperty().set(index);
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
        hbox.setAlignment(Pos.TOP_CENTER);
        hbox.getChildren().addAll(createChildren(index));
        currentPane = hbox;
        return hbox;
    }

    private List<Node> createChildren(int index) {
        List<Node> children = new ArrayList<>();

        if (book == null) {
            Label label = new Label(lastError != null ? "ファイルを読み込めません。: " + lastError : "ファイルを選択してください。");
            children.add(label);
            return children;
        }

        if (faceProperty.get()) {
            Image leftImage = book.getImage(getPageIndex(index, rightProperty.not().get()));
            Image rightImage = book.getImage(getPageIndex(index, rightProperty.get()));
            log.log(Level.INFO, "createContent: left={0}, right={1}", new Object[]{leftImage, rightImage});

            if (leftImage != null) {
                Color color = leftImage.getPixelReader().getColor(0, 0);
                log.log(Level.INFO, "color={0}", color);
                // TODO 背景色をクロスフェード
                children.add(wrapView(leftImage));
            }
            if (rightImage != null) {
                children.add(wrapView(rightImage));
            }
        } else {
            Image image = book.getImage(index);
            children.add(wrapView(image));
        }

        return children;
    }

    private int getPageIndex(int index, boolean right) {
        if (right) {
            return index + origin - 1;
        } else {
            return index + origin;
        }
    }

    private ImageView wrapView(Image image) {
        ImageView view = new ImageView(image);
        view.getStyleClass().add("imageview");
        view.fitWidthProperty().bind(pagination.widthProperty().divide(2));
        view.fitHeightProperty().bind(pagination.heightProperty());
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setCache(true);
        Reflection reflection = new Reflection();
        view.setEffect(reflection);
        return view;
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
            if (file != null) {
                setFile(file);
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, "ファイルを読み込めません。", ex);
            lastError = ex;
            // TODO エラーダイアログを表示する
        }
    }

    private void setFile(File file) throws IOException {
        log.log(Level.INFO, "ファイルを開きます。: {0}", file);
//        book = Books.read(file);
        fileProperty.set(file);
        bookService.restart();

//        updateIndex();
//        thumbnail.getSelectionModel().select(0);
//        thumbnail.requestFocus();
//
//        int pageCount = book.getPageCount() / 2 * 2 + 1;
//        log.log(Level.INFO, "pagination: {0} ({1})", new Object[]{book.getPageCount(), pageCount});
//        pagination.setPageCount(pageCount);
    }

    class GetBookService extends Service<Book> {

        private File file;

        @Override
        protected Task<Book> createTask() {
            file = fileProperty.get();
            return new GetBookTask(file);
        }
    }

    class GetBookTask extends Task<Book> {

        private File file;

        GetBookTask(File file) {
            this.file = file;
        }

        @Override
        protected Book call() throws Exception {
            Book book = Books.read(file);
            return book;
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
        int index = thumbnail.getSelectionModel().getSelectedIndex();
        if (index <= 0) {
            return;
        }
        thumbnail.getSelectionModel().select(index - 1);
    }

    @FXML
    private void handleNext(ActionEvent event) {
        int index = thumbnail.getSelectionModel().getSelectedIndex();
        thumbnail.getSelectionModel().select(index + 1);
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

    private void handleSwipeDown(SwipeEvent event) {
        log.log(Level.INFO, "handleSwipeDown: {0}", event);
    }

    @FXML
    private void handleScrollStarted(ScrollEvent event) {
        log.log(Level.INFO, "handleScrollStarted: {0} [{1},{2}]", new Object[]{event.getEventType().getName(), event.getDeltaX(), event.getDeltaY()});
        if (event.getDeltaY() == 0) {
            if (event.getDeltaX() < 0) {
                handlePrev(null);
            } else {
                handleNext(null);
            }
        }
        if (event.getDeltaX() == 0) {
            if (event.getDeltaY() < 0) {
                handleOpen(null);
            }
        }
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
