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

import java.io.File;
import java.io.IOException;
import javafx.scene.image.Image;

/**
 * Book インターフェースです。
 * 
 * @author Takashi Ogura <tarchan at mac.com>
 */
public interface Book {

    /**
     * 指定された File を復号化できるかどうか判定します。
     * 
     * @param input 読み込み元の File
     * @return 復号化できる場合は true
     */
    public boolean canDecodeInput(File input) throws IOException;
    
    /**
     * 指定された File を復号化します。
     * 
     * @param input 読み込み元の File
     * @throws IOException 読み込み中にエラーが発生した場合
     */
    public void read(File input) throws IOException;

    /**
     * 現在の Book のページ数を返します。
     *
     * @return ページ数
     */
    public int getPageCount();

    /**
     * 指定されたページ番号のイメージを返します。
     * ページ番号は 0 オリジンです。
     *
     * @param page ページ番号
     * @return イメージ または null
     */
    public Image getImage(int page);
}
