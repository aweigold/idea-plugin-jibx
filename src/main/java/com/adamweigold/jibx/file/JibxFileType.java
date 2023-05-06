/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adamweigold.jibx.file;

import com.adamweigold.jibx.icons.JibxIcons;
import com.adamweigold.jibx.lang.JibxLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Adam J. Weigold <adam@adamweigold.com>
 */
public class JibxFileType extends LanguageFileType {

    public static final JibxFileType JIBX_FILE_TYPE = new JibxFileType();
    public static final Language JIBX_LANGUAGE = JIBX_FILE_TYPE.getLanguage();

    private JibxFileType(){
        super(new JibxLanguage());
    }

    @NotNull
    @Override
    public String getName() {
        return "JiBX";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "JiBX XML Binding Definition";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "jibx";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return JibxIcons.JIBX_16;
    }
}
