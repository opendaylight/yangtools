/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.opendaylight.yangtools.yang.parser.repo.URLSchemaContextResolver;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;
import org.opendaylight.yangtools.yang2sources.plugin.ConfigArg.CodeGeneratorArg;
import org.opendaylight.yangtools.yang2sources.plugin.Util.ContextHolder;
import org.opendaylight.yangtools.yang2sources.plugin.Util.YangsInZipsResult;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.opendaylight.yangtools.yang2sources.spi.BuildContextAware;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

class YangToSourcesProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(YangToSourcesProcessor.class);

    static final String LOG_PREFIX = "yang-to-sources:";
    static final String META_INF_YANG_STRING = "META-INF" + File.separator + "yang";
    static final String META_INF_YANG_STRING_JAR = "META-INF" + "/" + "yang";

    private final File yangFilesRootDir;
    private final File[] excludedFiles;
    private final List<CodeGeneratorArg> codeGenerators;
    private final MavenProject project;
    private final boolean inspectDependencies;
    private final BuildContext buildContext;
    private final YangProvider yangProvider;
    private final URLSchemaContextResolver resolver;

    @VisibleForTesting
    YangToSourcesProcessor(File yangFilesRootDir, File[] excludedFiles, List<CodeGeneratorArg> codeGenerators,
            MavenProject project, boolean inspectDependencies, YangProvider yangProvider) {
        this(new DefaultBuildContext(), yangFilesRootDir, excludedFiles, codeGenerators, project, inspectDependencies, yangProvider);
    }

    private YangToSourcesProcessor(BuildContext buildContext, File yangFilesRootDir, File[] excludedFiles,
            List<CodeGeneratorArg> codeGenerators, MavenProject project, boolean inspectDependencies, YangProvider yangProvider) {
        this.buildContext = Util.checkNotNull(buildContext, "buildContext");
        this.yangFilesRootDir = Util.checkNotNull(yangFilesRootDir, "yangFilesRootDir");
        this.excludedFiles = new File[excludedFiles.length];
        int i = 0;
        for (File file : excludedFiles) {
            this.excludedFiles[i++] = new File(file.getPath());
        }
        this.codeGenerators = Collections.unmodifiableList(Util.checkNotNull(codeGenerators, "codeGenerators"));
        this.project = Util.checkNotNull(project, "project");
        this.inspectDependencies = inspectDependencies;
        this.yangProvider = yangProvider;
        this.resolver = URLSchemaContextResolver.create("maven-plugin");
    }

    YangToSourcesProcessor(BuildContext buildContext, File yangFilesRootDir, File[] excludedFiles, List<CodeGeneratorArg> codeGenerators,
            MavenProject project, boolean inspectDependencies) {
        this(yangFilesRootDir, excludedFiles, codeGenerators, project, inspectDependencies, new YangProvider());
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        ContextHolder context = processYang();
        if (context != null) {
            generateSources(context);
            yangProvider.addYangsToMetaInf(project, yangFilesRootDir, excludedFiles);
        }
    }

    private ContextHolder processYang() throws MojoExecutionException {
        YangParserImpl parser = new YangParserImpl();
        List<Closeable> closeables = new ArrayList<>();
        LOG.info(Util.message("Inspecting %s", LOG_PREFIX, yangFilesRootDir));
        try {
            /*
             * Collect all files which affect YANG context. This includes all
             * files in current project and optionally any jars/files in the
             * dependencies.
             */
            final Collection<File> yangFilesInProject = Util.listFiles(yangFilesRootDir, excludedFiles);


            final Collection<File> allFiles = new ArrayList<>(yangFilesInProject);
            if (inspectDependencies) {
                allFiles.addAll(Util.findYangFilesInDependencies(project));
            }

            if (allFiles.isEmpty()) {
            	LOG.info(Util.message("No input files found", LOG_PREFIX));
            	return null;
            }

            /*
             * Check if any of the listed files changed. If no changes occurred,
             * simply return null, which indicates and of execution.
             */
            boolean noChange = true;
            for (final File f : allFiles) {
                if (buildContext.hasDelta(f)) {
                    LOG.debug(Util.message("buildContext %s indicates %s changed, forcing regeneration", LOG_PREFIX,
                            buildContext, f));
                    noChange = false;
                }
            }

            if (noChange) {
                LOG.info(Util.message("None of %s input files changed", LOG_PREFIX, allFiles.size()));
                return null;
            }

            final List<InputStream> yangsInProject = new ArrayList<>();
            for (final File f : yangFilesInProject) {
                // FIXME: This is hack - normal path should be reported.
                yangsInProject.add(new NamedFileInputStream(f, META_INF_YANG_STRING + File.separator + f.getName()));
            }

            List<InputStream> all = new ArrayList<>(yangsInProject);
            closeables.addAll(yangsInProject);
            Map<InputStream, Module> allYangModules;

            /**
             * Set contains all modules generated from input sources. Number of
             * modules may differ from number of sources due to submodules
             * (parsed submodule's data are added to its parent module). Set
             * cannot contains null values.
             */
            Set<Module> projectYangModules;
            try {
                if (inspectDependencies) {
                    YangsInZipsResult dependentYangResult = Util.findYangFilesInDependenciesAsStream(project);
                    Closeable dependentYangResult1 = dependentYangResult;
                    closeables.add(dependentYangResult1);
                    List<InputStream> yangStreams = toStreamsWithoutDuplicates(dependentYangResult.getYangStreams());
                    all.addAll(yangStreams);
                    closeables.addAll(yangStreams);
                }

                allYangModules = parser.parseYangModelsFromStreamsMapped(all);

                projectYangModules = new HashSet<>();
                for (InputStream inProject : yangsInProject) {
                    Module module = allYangModules.get(inProject);
                    if (module != null) {
                        projectYangModules.add(module);
                    }
                }

            } finally {
                for (AutoCloseable closeable : closeables) {
                    closeable.close();
                }
            }

            Set<Module> parsedAllYangModules = new HashSet<>(allYangModules.values());
            SchemaContext resolveSchemaContext = parser.resolveSchemaContext(parsedAllYangModules);
            LOG.info(Util.message("%s files parsed from %s", LOG_PREFIX, Util.YANG_SUFFIX.toUpperCase(),
                    yangsInProject));
            return new ContextHolder(resolveSchemaContext, projectYangModules);

            // MojoExecutionException is thrown since execution cannot continue
        } catch (Exception e) {
            String message = Util.message("Unable to parse %s files from %s", LOG_PREFIX, Util.YANG_SUFFIX,
                    yangFilesRootDir);
            LOG.error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    private List<InputStream> toStreamsWithoutDuplicates(List<YangSourceFromDependency> list) throws IOException {
        ConcurrentMap<String, YangSourceFromDependency> byContent = Maps.newConcurrentMap();

        for (YangSourceFromDependency yangFromDependency : list) {
            try (InputStream dataStream = yangFromDependency.openStream()) {
                String contents = IOUtils.toString(dataStream);
                byContent.putIfAbsent(contents, yangFromDependency);
            }

        }
        List<InputStream> inputs = new ArrayList<>(byContent.size());
        for (YangSourceFromDependency entry : byContent.values()) {
            inputs.add(entry.openStream());
        }
        return inputs;
    }

    static class YangProvider {
        private static final Logger LOG = LoggerFactory.getLogger(YangProvider.class);

        void addYangsToMetaInf(MavenProject project, File yangFilesRootDir, File[] excludedFiles)
                throws MojoFailureException {

            // copy project's src/main/yang/*.yang to target/generated-sources/yang/META-INF/yang/*.yang

            File generatedYangDir = new File(project.getBasedir(), CodeGeneratorArg.YANG_GENERATED_DIR);
            addYangsToMetaInf(project, yangFilesRootDir, excludedFiles, generatedYangDir);

            // Also copy to the actual build output dir if different than "target". When running in
            // Eclipse this can differ (eg "target-ide").

            File actualGeneratedYangDir = new File(project.getBuild().getDirectory(),
                    CodeGeneratorArg.YANG_GENERATED_DIR.replace("target" + File.separator, ""));
            if(!actualGeneratedYangDir.equals(generatedYangDir)) {
                addYangsToMetaInf(project, yangFilesRootDir, excludedFiles, actualGeneratedYangDir);
            }
        }

        private void addYangsToMetaInf(MavenProject project, File yangFilesRootDir,
                File[] excludedFiles, File generatedYangDir)
                throws MojoFailureException {

            File withMetaInf = new File(generatedYangDir, META_INF_YANG_STRING);
            withMetaInf.mkdirs();

            try {
                Collection<File> files = Util.listFiles(yangFilesRootDir, excludedFiles);
                for (File file : files) {
                    org.apache.commons.io.FileUtils.copyFile(file, new File(withMetaInf, file.getName()));
                }
            } catch (IOException e) {
                LOG.warn(String.format("Failed to generate files into root %s", yangFilesRootDir), e);
                throw new MojoFailureException("Unable to list yang files into resource folder", e);
            }

            setResource(generatedYangDir, project);

            LOG.debug(Util.message("Yang files from: %s marked as resources: %s", LOG_PREFIX, yangFilesRootDir,
                    META_INF_YANG_STRING_JAR));
        }

        private static void setResource(File targetYangDir, MavenProject project) {
            Resource res = new Resource();
            res.setDirectory(targetYangDir.getPath());
            project.addResource(res);
        }
    }

    /**
     * Call generate on every generator from plugin configuration
     */
    private void generateSources(ContextHolder context) throws MojoFailureException {
        if (codeGenerators.size() == 0) {
            LOG.warn(Util.message("No code generators provided", LOG_PREFIX));
            return;
        }

        Map<String, String> thrown = Maps.newHashMap();
        for (CodeGeneratorArg codeGenerator : codeGenerators) {
            try {
                generateSourcesWithOneGenerator(context, codeGenerator);
            } catch (Exception e) {
                // try other generators, exception will be thrown after
                LOG.error(Util.message("Unable to generate sources with %s generator", LOG_PREFIX, codeGenerator
                        .getCodeGeneratorClass()), e);
                thrown.put(codeGenerator.getCodeGeneratorClass(), e.getClass().getCanonicalName());
            }
        }

        if (!thrown.isEmpty()) {
            String message = Util.message(
                    "One or more code generators failed, including failed list(generatorClass=exception) %s",
                    LOG_PREFIX, thrown.toString());
            LOG.error(message);
            throw new MojoFailureException(message);
        }
    }

    /**
     * Instantiate generator from class and call required method
     */
    private void generateSourcesWithOneGenerator(ContextHolder context, CodeGeneratorArg codeGeneratorCfg)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

        codeGeneratorCfg.check();

        BasicCodeGenerator g = Util.getInstance(codeGeneratorCfg.getCodeGeneratorClass(), BasicCodeGenerator.class);
        LOG.info(Util.message("Code generator instantiated from %s", LOG_PREFIX,
                codeGeneratorCfg.getCodeGeneratorClass()));

        File outputDir = codeGeneratorCfg.getOutputBaseDir(project);

        if (outputDir != null) {
          project.addCompileSourceRoot(outputDir.getAbsolutePath());
        } else {
          throw new NullPointerException("outputBaseDir is null. Please provide a valid outputBaseDir value in the pom.xml");
        }

        LOG.info(Util.message("Sources will be generated to %s", LOG_PREFIX, outputDir));
        LOG.debug(Util.message("Project root dir is %s", LOG_PREFIX, project.getBasedir()));
        LOG.debug(Util.message("Additional configuration picked up for : %s: %s", LOG_PREFIX,
                codeGeneratorCfg.getCodeGeneratorClass(), codeGeneratorCfg.getAdditionalConfiguration()));

        if (g instanceof BuildContextAware) {
            ((BuildContextAware)g).setBuildContext(buildContext);
        }
        if (g instanceof MavenProjectAware) {
            ((MavenProjectAware)g).setMavenProject(project);
        }
        g.setAdditionalConfig(codeGeneratorCfg.getAdditionalConfiguration());
        File resourceBaseDir = codeGeneratorCfg.getResourceBaseDir(project);

        YangProvider.setResource(resourceBaseDir, project);
        g.setResourceBaseDir(resourceBaseDir);
        LOG.debug(Util.message("Folder: %s marked as resources for generator: %s", LOG_PREFIX, resourceBaseDir,
                codeGeneratorCfg.getCodeGeneratorClass()));

        Collection<File> generated = g.generateSources(context.getContext(), outputDir, context.getYangModules());

        LOG.info(Util.message("Sources generated by %s: %s", LOG_PREFIX, codeGeneratorCfg.getCodeGeneratorClass(),
                generated));
    }

}
