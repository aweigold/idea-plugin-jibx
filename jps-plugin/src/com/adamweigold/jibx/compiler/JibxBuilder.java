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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author Adam J. Weigold <adam@adamweigold.com>
 */
public class JibxBuilder extends ModuleLevelBuilder {

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
        if (compileDir == null){
            return ExitCode.NOTHING_DONE;
        }

        String compileOutput = compileDir.getAbsolutePath();
        String[] classPathArray = getClassPathArray(compileOutput, moduleChunk);

        Map<String, CompiledClass> compiledClasses = outputConsumer.getCompiledClasses();
        if (compiledClasses.isEmpty()) {
            compileContext.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.INFO, "Nothing compiled yet, exiting"));
            return ExitCode.NOTHING_DONE;
        }

        String[] bindingFileList = getBindingFileList(moduleChunk);
        if (bindingFileList.length == 0){
            return ExitCode.NOTHING_DONE;
        }
        Compile jibxCompiler = getCompilerFromSettings(jibxSettings);
        try {
            compileContext.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.INFO, "Starting JiBX Bindings..."));

            jibxCompiler.compile(classPathArray, bindingFileList);
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
        for (URL u : ((URLClassLoader)this.getClass().getClassLoader()).getURLs()) {
            classpath.add(new File(u.getFile()));
        }

        return classpath;
    }

    private String[] getClassPathArray(String compileOutput, ModuleChunk moduleChunk) {
        HashSet<String> classPathSet = new HashSet<String>();
        classPathSet.add(compileOutput);
        for (File classPathFile : getClassPath(moduleChunk)){
            classPathSet.add(classPathFile.getAbsolutePath());
        }
        return classPathSet.toArray(new String[classPathSet.size()]);
    }

    private Compile getCompilerFromSettings(final JibxSettings settings) {
                //new Compile(verbose1, verbose2, load, verify, track, over);
        return new Compile();
    }

    private String[] getBindingFileList(final ModuleChunk moduleChunk) {
        HashSet<String> bindings = new HashSet<String>();
        for (JpsModule module : moduleChunk.getModules()){
            for (String rootUrl : module.getContentRootsList().getUrls()){
                File root = JpsPathUtil.urlToFile(rootUrl);
                File jibxRoot = new File(root, "jibx");
                if (jibxRoot.exists() && jibxRoot.isDirectory()){
                    File[] files = jibxRoot.listFiles();
                    if (files != null) {
                        for (File binding : files) {
                            bindings.add(binding.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return bindings.toArray(new String[bindings.size()]);
    }

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
