/*
 * Copyright 2012 tarchan.
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;

/**
 * Zip のイメージを返すクラスです。
 * 
 * @author @author Takashi Ogura <tarchan at mac.com>
 */
public class ZipBook implements Book {

    private ZipFile zipFile;
    private List<ZipEntry> zipList;

    @Override
    public boolean canDecodeInput(File input) throws IOException {
        try {
            new ZipFile(input);
            return true;
        } catch (IOException ex) {
           return false;
        }
    }

    @Override
    public void read(File input) throws IOException {
        zipFile = new ZipFile(input);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        zipList = new ArrayList<>();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            zipList.add(e);
        }
    }

    @Override
    public int getPageCount() {
        return zipList.size();
    }

    @Override
    public Image getImage(int page) {
        if (page < 0 || page >= getPageCount()) {
            return null;
        }

        try (InputStream fin = zipFile.getInputStream(zipList.get(page))) {
            BufferedImage awtImage = ImageIO.read(fin);
            Image image = SwingFXUtils.toFXImage(awtImage, null);
            return image;
        } catch (IOException ex) {
            Logger.getLogger(ZipBook.class.getName()).log(Level.SEVERE, "ZIP ファイルを読み込めません。", ex);
            throw new RuntimeException("ZIP ファイルを読み込めません。", ex);
        }
    }
}
