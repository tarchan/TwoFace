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
package com.mac.tarchan.twoface.book;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * PDF のイメージを返すクラスです。
 *
 * @author Takashi Ogura <tarchan at mac.com>
 */
public class PdfBook implements Book {

    private PDFFile pdfFile;

    @Override
    public boolean canDecodeInput(File input) {
        try {
            read(input);
            return true;
        } catch (IOException ex) {
           return false;
        }
    }

    /**
     * PdfBook オブジェクトを構築します。
     * 
     * @param file ファイル
     * @throws IOException ファイルが読み込めない場合
     */
    @Override
    public void read(File file) throws IOException {
        RandomAccessFile read = new RandomAccessFile(file, "r");
        FileChannel channel = read.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        pdfFile = new PDFFile(buf);
    }

    @Override
    public int getPageCount() {
        return pdfFile.getNumPages();
    }

    @Override
    public Image getImage(int page) {
        if (page < 0 || page >= getPageCount()) {
            return null;
        }
        page++;
        PDFPage pdfPage = pdfFile.getPage(page);
        Rectangle2D rect = pdfPage.getBBox();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();
        java.awt.Image awtImage = pdfPage.getImage(width, height, rect, null, true, true);
        Image image = SwingFXUtils.toFXImage((BufferedImage) awtImage, null);
        return image;
    }
}
