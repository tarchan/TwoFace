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
import java.nio.file.Files;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Book の復号化を実行するクラスです。
 *
 * @author Takashi Ogura <tarchan at mac.com>
 */
public class Books {

    private static final Logger log = Logger.getLogger(Books.class.getName());

    /**
     * インスタンスの生成を禁止します。
     */
    private Books() {
    }

    /**
     * 指定された File を復号化した Book を返します。
     * 
     * @param file 読み込み元の File
     * @return 読み込んだ内容を保持する Book または null
     * @throws IOException 読み込み中にエラーが発生した場合
     */
    public static Book read(File file) throws IOException {
        String mimetype = Files.probeContentType(file.toPath());
        log.log(Level.INFO, "mimetype: {0}", mimetype);
        
        ServiceLoader<Book> loader = ServiceLoader.load(Book.class);
        log.log(Level.INFO, "ServiceLoader: {0}", loader);
        for (Book book : loader) {
            log.log(Level.INFO, "book: {0}", book.getClass().getName());
            if (book.canDecodeInput(file)) {
                book.read(file);
                return book;
            }
        }

        return null;
    }
}
