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

package com.adamweigold.jibx.icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Adam J. Weigold <adam@adamweigold.com>
 */
public interface JibxIcons {
    Icon JIBX_64 = IconLoader.getIcon("/com/adamweigold/jibx/icons/jibx_med_64.png", ClassLoader.getSystemClassLoader());
    Icon JIBX_16 = IconLoader.getIcon("/com/adamweigold/jibx/icons/jibx_med_16.png", ClassLoader.getSystemClassLoader());
}
