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

package com.adamweigold.jibx.compiler;

import com.adamweigold.jibx.settings.JibxSettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.ProjectPaths;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.incremental.*;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.util.JpsPathUtil;
import org.jibx.binding.Compile;
import org.jibx.runtime.JiBXException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Adam J. Weigold <adam@adamweigold.com>
 */
public class JibxBuilder extends ModuleLevelBuilder {

    private static final Logger logger = Logger.getInstance(JibxBuilder.class);

    public JibxBuilder() {
        super(BuilderCategory.CLASS_POST_PROCESSOR);
    }

    @Override
    public ExitCode build(CompileContext compileContext,
                          ModuleChunk moduleChunk,
                          DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder,
                          OutputConsumer outputConsumer) throws ProjectBuildException, IOException {
        JibxSettings jibxSettings = JibxSettings.getSettings(compileContext.getProjectDescriptor().getProject());
        ModuleBuildTarget moduleBuildTarget = moduleChunk.representativeTarget();
        File compileDir = moduleBuildTarget.getOutputDir();
        if (compileDir == null) {
            return ExitCode.NOTHING_DONE;
        }


        Map<String, CompiledClass> compiledClasses = outputConsumer.getCompiledClasses();
        if (compiledClasses.isEmpty()) {
            compileContext.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.INFO, "Nothing compiled yet, exiting"));
            return ExitCode.NOTHING_DONE;
        }

        String[] bindingFileList = getBindingFileList(moduleChunk);
        if (bindingFileList.length == 0) {
            return ExitCode.NOTHING_DONE;
        }
        logger.info("Found binding files: " + Arrays.toString(bindingFileList));


        String compileOutput = compileDir.getAbsolutePath();
        String[] classPathArray = getClassPathArray(compileOutput, moduleChunk);
        Compile jibxCompiler = getCompilerFromSettings(jibxSettings);
        try {
            compileContext.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.INFO, "Starting JiBX Bindings" + Arrays.toString(bindingFileList) + "..."));
            jibxCompiler.compile(classPathArray, bindingFileList);
            compileContext.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.INFO, "Finished JiBX Bindings"));
        } catch (JiBXException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            compileContext.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.ERROR, sw.toString()));
            return ExitCode.ABORT;
        }
        return ExitCode.OK;
    }

    private Collection<File> getClassPath(ModuleChunk moduleChunk) {
        Collection<File> classpath = new HashSet<File>();

        classpath.addAll(ProjectPaths.getCompilationClasspathFiles(moduleChunk, false, false, false));

        // Add the current classpath to get the compiler jars
        for (URL u : ((URLClassLoader) this.getClass().getClassLoader()).getURLs()) {
            classpath.add(new File(u.getFile()));
        }

        return classpath;
    }

    private String[] getClassPathArray(String compileOutput, ModuleChunk moduleChunk) {
        HashSet<String> classPathSet = new HashSet<String>();
        classPathSet.add(compileOutput);
        for (File classPathFile : getClassPath(moduleChunk)) {
            classPathSet.add(classPathFile.getAbsolutePath());
        }
        return classPathSet.toArray(new String[0]);
    }

    private Compile getCompilerFromSettings(final JibxSettings settings) {
        //new Compile(verbose1, verbose2, load, verify, track, over);
        return new Compile();
    }

    private String[] getBindingFileList(final ModuleChunk moduleChunk) {
        Set<String> bindings = new HashSet<>();
        for (JpsModule module : moduleChunk.getModules()) {
            for (String rootUrl : module.getContentRootsList().getUrls()) {
                try {
                    File root = JpsPathUtil.urlToFile(rootUrl);
                    Path moduleFolderPath = root.toPath();
                    checkForBindingsInJibxFolder(bindings, moduleFolderPath);
                    checkForBindingsInPom(bindings, moduleFolderPath);
                } catch (IOException | XmlPullParserException ex) {
                    ex.printStackTrace();
                    throw new IllegalStateException(ex);
                }
            }
        }
        return bindings.toArray(new String[0]);
    }

    private static void checkForBindingsInPom(Set<String> bindings, Path path) throws IOException, XmlPullParserException {
        Path pom = path.resolve("pom.xml");
        if(Files.exists(pom)){
            logger.info("Found pom.xml, checking for information about jibx binding files...");
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(Files.newBufferedReader(pom));
            Build build = model.getBuild();
            if (build != null) {
                logger.info("Searching for jibx plugin in pom...");
                Plugin plugin = build.getPluginsAsMap().get("org.jibx:jibx-maven-plugin");
                Optional<PluginExecution> pluginExecution = plugin.getExecutions().stream()
                        .filter(execution -> execution.getGoals().contains("bind"))
                        .findFirst();
                if (pluginExecution.isPresent()) {
                    PluginExecution bindPluginExecution = pluginExecution.get();
                    Xpp3Dom configuration = (Xpp3Dom) bindPluginExecution.getConfiguration();
                    Xpp3Dom schemaBindingDirectory = configuration.getChild("schemaBindingDirectory");
                    String schemaBindingDirectoryValue = schemaBindingDirectory.getValue();
                    logger.info("Checking binding directory...");
                    schemaBindingDirectoryValue = schemaBindingDirectoryValue.replace("${project.basedir}","");
                    schemaBindingDirectoryValue = schemaBindingDirectoryValue.replace("${project.build.directory}","target");
                    logger.info("Searching for binding files in directory " + schemaBindingDirectoryValue);
                    Path schemaBindingDirectoryPath = path.resolve(schemaBindingDirectoryValue);
                    addFilesInDirectoryToBindings(bindings, schemaBindingDirectoryPath);
                }
            }
        }
    }

    private static void checkForBindingsInJibxFolder(Set<String> bindings, Path path) {
        Path jibx = path.resolve("jibx");
        if (Files.exists(jibx)) {
            logger.info("Found jibx folder, adding binding files...");
            addFilesInDirectoryToBindings(bindings, jibx);
        }
    }

    private static void addFilesInDirectoryToBindings(Set<String> bindings, Path dir) {
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try(Stream<Path> list = Files.list(dir)){
                list
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".xml"))
                        .map(Path::toAbsolutePath)
                        .map(Path::toString)
                        .forEach(bindings::add);
            }catch(IOException ex){
                ex.printStackTrace();
                throw new IllegalStateException(ex);
            }
        }
    }

    @NotNull
    @Override
    public List<String> getCompilableFileExtensions() {
        return Arrays.asList("java", "jibx");
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Jibx Compiler";
    }

}
