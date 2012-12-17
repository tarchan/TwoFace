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

import javafx.scene.image.Image;

/**
 *
 * @author Takashi Ogura <tarchan at mac.com>
 */
public interface Book {

    /**
     * この Book のページ数を返します。
     * 
     * @return ページ数
     */
    public int getPageCount();

    /**
     * 指定されたページ番号のイメージを返します。
     * 
     * @param page ページ番号
     * @return イメージ
     */
    public Image getImage(int page);
}
