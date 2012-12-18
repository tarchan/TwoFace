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

/**
 * Book を返すファクトリークラスです。
 *
 * @author Takashi Ogura <tarchan at mac.com>
 */
public class BookFactory {

    /**
     * インスタンスの生成を禁止します。
     */
    private BookFactory() {
    }

    /**
     * 指定されたファイルに対応する Book オブジェクトを返します。
     * 
     * @param file ファイル
     * @return Book オブジェクト
     * @throws IOException ファイルの読み込みでエラーが発生した場合
     * @throws UnsupportedOperationException 未対応のファイルが指定された場合
     */
    public static Book getBook(File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".pdf")) {
            return new PdfBook(file);
        } else if (file.getName().toLowerCase().endsWith(".zip")) {
            return new ZipBook(file);
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
