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

package com.adamweigold.jibx.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

/**
 * @author Adam J. Weigold <adam@adamweigold.com>
 */
public class JibxSettings extends JpsElementBase<JibxSettings> {

    static final JpsElementChildRole<JibxSettings> ROLE = JpsElementChildRoleBase.create("Jibx Compiler Configuration");

    public JibxSettings(){}

    public JibxSettings(JibxSettings original) {

    }

    @NotNull
    public static JibxSettings getSettings(@NotNull JpsProject project){
        JibxSettings settings = project.getContainer().getChild(ROLE);
        return settings == null ? new JibxSettings() : settings;
    }

    @NotNull
    @Override
    public JibxSettings createCopy() {
        return new JibxSettings(this);
    }

    @Override
    public void applyChanges(@NotNull JibxSettings modified) {

    }
}
