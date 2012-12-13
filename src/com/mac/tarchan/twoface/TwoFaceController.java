/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author tarchan
 */
public class TwoFaceController implements Initializable {

    private final Logger log = Logger.getLogger(TwoFaceController.class.getName());
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

        MultipleSelectionModel<PageItem> model = thumbnail.getSelectionModel();
        model.selectedItemProperty().addListener(new ChangeListener<PageItem>() {
            @Override
            public void changed(ObservableValue<? extends PageItem> property, PageItem oldValue, PageItem newValue) {
                onChanged(property, oldValue, newValue);
            }
        });
    }

    private Node createPage(Integer idx) {
        System.out.println(String.format("createPage: %d ページ", idx + 1));
        HBox box = new HBox();
        if (pdfFile != null) {
//            PDFPage pdfPage = pdfFile.getPage(idx + 1);
//            Rectangle2D rect = pdfPage.getBBox();
//            int width = (int) rect.getWidth();
//            int height = (int) rect.getHeight();
//            java.awt.Image awtImage = pdfPage.getImage(width, height, rect, null, true, true);
//            page0 = SwingFXUtils.toFXImage((BufferedImage) awtImage, page0);
            Image page0 = getImage(idx - origin, null);
            Image page1 = getImage(idx - origin + 1, null);
            ImageView view0 = new ImageView(page0);
            view0.setFitHeight(pagination.getHeight());
            view0.setPreserveRatio(true);
            view0.setSmooth(true);
            view0.setCache(true);
            ImageView view1 = new ImageView(page1);
            view1.setFitHeight(pagination.getHeight());
            view1.setPreserveRatio(true);
            view1.setSmooth(true);
            view1.setCache(true);
            box.getChildren().add(view1);
            box.getChildren().add(view0);
        } else {
            Label label = new Label(String.format("%d ページ", idx));
            box.getChildren().add(label);
        }
        return box;
    }

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

    private void onChanged(ObservableValue<? extends PageItem> property, PageItem oldValue, PageItem newValue) {
        System.out.println(String.format("onChanged: %s, %s, %s", property, oldValue, newValue));
        int idx = newValue.value;
        pagination.currentPageIndexProperty().setValue(idx);
    }

    @FXML
    private void handleOpen(ActionEvent event) {
        try {
            System.out.println("open file");
            File file = fileChooser.showOpenDialog(null);
            System.out.println("file=" + file);
            RandomAccessFile read = new RandomAccessFile(file, "r");
            FileChannel channel = read.getChannel();
            ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            pdfFile = new PDFFile(buf);

            pagination.setPageCount(pdfFile.getNumPages());

            ArrayList<PageItem> pages = new ArrayList<>();
//            for (int i = 0; i < pdfFile.getNumPages(); i++) {
            for (int i = 1; i < pdfFile.getNumPages(); i += 2) {
                pages.add(new PageItem(i));
            }
            ObservableList<PageItem> names = FXCollections.observableArrayList(pages);
            thumbnail.setItems(names);
        } catch (IOException ex) {
            Logger.getLogger(TwoFaceController.class.getName()).log(Level.SEVERE, "PDF ファイルを読み込めません。", ex);
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.out.println("exit platform");
        Platform.exit();
    }

    @FXML
    private void handleFirst(ActionEvent event) {
        System.out.println("handleFirst");
        pagination.currentPageIndexProperty().setValue(0);
    }

    @FXML
    private void handlePrev(ActionEvent event) {
        System.out.println("handlePrev");
        int idx = pagination.currentPageIndexProperty().getValue();
        pagination.currentPageIndexProperty().setValue(idx - 2);
    }

    @FXML
    private void handleNext(ActionEvent event) {
        System.out.println("handleNext");
        int idx = pagination.currentPageIndexProperty().getValue();
        pagination.currentPageIndexProperty().setValue(idx + 2);
    }

    @FXML
    private void handleLast(ActionEvent event) {
        System.out.println("handleLast");
        pagination.currentPageIndexProperty().setValue(pagination.getPageCount());
    }
}

/**
 * ページアイテム
 * 
 * @author tarchan
 */
class PageItem {

    /** ページ番号 */
    public int value;

    /** ページ名 */
    public String name;

    /** サムネール */
    public Image image;

    public PageItem(int idx) {
        value = idx;
        name = String.format("%,d ページ", idx + 1);
    }

    /** 文字列表現 */
    @Override
    public String toString() {
        return name;
    }
}
