package com.adamweigold.jibx.compiler;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

class JibxBuilderTest {


    @Test
    void shouldDeliverNullWhenNoJibxPluginPresent() throws XmlPullParserException, IOException {
        Set<String> bindings = new HashSet<>();
        Path path = Paths.get("src/test/resources/noPlugin");
        JibxBuilder.checkForBindingsInPom(bindings, path);
    }

    @Test
    void shouldDeliverOneEntryWhenJibxPluginPresentWithConfiguration() throws XmlPullParserException, IOException {
        Set<String> bindings = new HashSet<>();
        Path path = Paths.get("src/test/resources/pluginConfig");
        JibxBuilder.checkForBindingsInPom(bindings, path);
    }

    @Test
    void shouldDeliverOneEntryWhenJibxPluginPresentWithExecutionConfiguration() throws XmlPullParserException, IOException {
        Set<String> bindings = new HashSet<>();
        Path path = Paths.get("src/test/resources/executionConfig");
        JibxBuilder.checkForBindingsInPom(bindings, path);
    }
}