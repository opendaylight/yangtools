/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
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
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
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
    static final String META_INF_YANG_SERVICES_STRING_JAR = "META-INF" + "/" + "services";

    private final File yangFilesRootDir;
    private final File[] excludedFiles;
    private final List<CodeGeneratorArg> codeGenerators;
    private final MavenProject project;
    private final boolean inspectDependencies;
    private final BuildContext buildContext;
    private final YangProvider yangProvider;
    private final YangTextSchemaContextResolver resolver;

    @VisibleForTesting
    YangToSourcesProcessor(final File yangFilesRootDir, final File[] excludedFiles, final List<CodeGeneratorArg> codeGenerators,
            final MavenProject project, final boolean inspectDependencies, final YangProvider yangProvider) {
        this(new DefaultBuildContext(), yangFilesRootDir, excludedFiles, codeGenerators, project,
                inspectDependencies, yangProvider);
    }

    private YangToSourcesProcessor(final BuildContext buildContext, final File yangFilesRootDir, final File[] excludedFiles,
            final List<CodeGeneratorArg> codeGenerators, final MavenProject project, final boolean inspectDependencies, final YangProvider
                                           yangProvider) {
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
        this.resolver = YangTextSchemaContextResolver.create("maven-plugin");
    }

    YangToSourcesProcessor(final BuildContext buildContext, final File yangFilesRootDir, final File[] excludedFiles,
                           final List<CodeGeneratorArg> codeGenerators, final MavenProject project, final boolean inspectDependencies) {
        this(yangFilesRootDir, excludedFiles, codeGenerators, project, inspectDependencies, new YangProvider());
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        ContextHolder context = processYang();
        if (context != null) {
            generateSources(context);
            yangProvider.addYangsToMetaInf(project, yangFilesRootDir, excludedFiles);
        }
    }

    void conditionalExecute(final boolean skip) throws MojoExecutionException, MojoFailureException {
        if (skip) {
            LOG.info("Skipping YANG code generation because property yang.skip is true");

            // But manually add resources
            // add META_INF/yang
            yangProvider.addYangsToMetaInf(project, yangFilesRootDir, excludedFiles);

            // add META_INF/services
            File generatedServicesDir = new File(project.getBasedir(), CodeGeneratorArg.YANG_SERVICES_GENERATED_DIR);
            YangProvider.setResource(generatedServicesDir, project);
            LOG.debug("{} Yang services files from: {} marked as resources: {}", LOG_PREFIX, generatedServicesDir,
                    META_INF_YANG_SERVICES_STRING_JAR);


        } else {
            execute();
        }
    }

    private ContextHolder processYang() throws MojoExecutionException {
        SchemaContext resolveSchemaContext;
        List<Closeable> closeables = new ArrayList<>();
        LOG.info("{} Inspecting {}", LOG_PREFIX, yangFilesRootDir);
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
                LOG.info("{} No input files found", LOG_PREFIX);
                return null;
            }

            /*
             * Check if any of the listed files changed. If no changes occurred,
             * simply return null, which indicates and of execution.
             */
            boolean noChange = true;
            for (final File f : allFiles) {
                if (buildContext.hasDelta(f)) {
                    LOG.debug("{} buildContext {} indicates {} changed, forcing regeneration", LOG_PREFIX,
                            buildContext, f);
                    noChange = false;
                }
            }

            if (noChange) {
                LOG.info("{} None of {} input files changed", LOG_PREFIX, allFiles.size());
                return null;
            }

            final List<NamedFileInputStream> yangsInProject = new ArrayList<>();
            for (final File f : yangFilesInProject) {
                // FIXME: This is hack - normal path should be reported.
                yangsInProject.add(new NamedFileInputStream(f, META_INF_YANG_STRING + File.separator + f.getName()));
            }

            List<InputStream> all = new ArrayList<>();
            all.addAll(yangsInProject);
            closeables.addAll(yangsInProject);

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

                resolveSchemaContext = YangParserTestUtils.parseYangStreams(all);

                Set<Module> parsedAllYangModules = resolveSchemaContext.getModules();
                projectYangModules = new HashSet<>();
                for (Module module : parsedAllYangModules) {
                    final String path = module.getModuleSourcePath();
                    if (path != null) {
                        LOG.debug("Looking for source {}", path);
                        for (NamedFileInputStream is : yangsInProject) {
                            LOG.debug("In project destination {}", is.getFileDestination());
                            if (path.equals(is.getFileDestination())) {
                                LOG.debug("Module {} belongs to current project", module);
                                projectYangModules.add(module);
                                break;
                            }
                        }
                    }
                }
            } finally {
                for (AutoCloseable closeable : closeables) {
                    closeable.close();
                }
            }

            LOG.info("{} {} files parsed from {}", LOG_PREFIX, Util.YANG_SUFFIX.toUpperCase(), yangsInProject);
            return new ContextHolder(resolveSchemaContext, projectYangModules);

            // MojoExecutionException is thrown since execution cannot continue
        } catch (Exception e) {
            LOG.error("{} Unable to parse {} files from {}", LOG_PREFIX, Util.YANG_SUFFIX, yangFilesRootDir, e);
            Throwable rootCause = Throwables.getRootCause(e);
            throw new MojoExecutionException(LOG_PREFIX + " Unable to parse " + Util.YANG_SUFFIX + " files from " +
                    yangFilesRootDir, rootCause);
        }
    }

    private static List<InputStream> toStreamsWithoutDuplicates(final List<YangSourceFromDependency> list) throws IOException {
        ConcurrentMap<String, YangSourceFromDependency> byContent = Maps.newConcurrentMap();

        for (YangSourceFromDependency yangFromDependency : list) {
            try (InputStream dataStream = yangFromDependency.openStream()) {
                String contents = IOUtils.toString(dataStream);
                byContent.putIfAbsent(contents, yangFromDependency);
            } catch (IOException e) {
                throw new IOException("Exception when reading from: " + yangFromDependency.getDescription(), e);
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

        void addYangsToMetaInf(final MavenProject project, final File yangFilesRootDir, final File[] excludedFiles)
                throws MojoFailureException {

            // copy project's src/main/yang/*.yang to target/generated-sources/yang/META-INF/yang/*.yang

            File generatedYangDir = new File(project.getBasedir(), CodeGeneratorArg.YANG_GENERATED_DIR);
            addYangsToMetaInf(project, yangFilesRootDir, excludedFiles, generatedYangDir);

            // Also copy to the actual build output dir if different than "target". When running in
            // Eclipse this can differ (eg "target-ide").

            File actualGeneratedYangDir = new File(project.getBuild().getDirectory(),
                    CodeGeneratorArg.YANG_GENERATED_DIR.replace("target" + File.separator, ""));
            if (!actualGeneratedYangDir.equals(generatedYangDir)) {
                addYangsToMetaInf(project, yangFilesRootDir, excludedFiles, actualGeneratedYangDir);
            }
        }

        private static void addYangsToMetaInf(final MavenProject project, final File yangFilesRootDir,
                final File[] excludedFiles, final File generatedYangDir) throws MojoFailureException {

            File withMetaInf = new File(generatedYangDir, META_INF_YANG_STRING);
            withMetaInf.mkdirs();

            try {
                Collection<File> files = Util.listFiles(yangFilesRootDir, excludedFiles);
                for (File file : files) {
                    org.apache.commons.io.FileUtils.copyFile(file, new File(withMetaInf, file.getName()));
                }
            } catch (IOException e) {
                LOG.warn("Failed to generate files into root {}", yangFilesRootDir, e);
                throw new MojoFailureException("Unable to list yang files into resource folder", e);
            }

            setResource(generatedYangDir, project);

            LOG.debug("{} Yang files from: {} marked as resources: {}", LOG_PREFIX, yangFilesRootDir,
                    META_INF_YANG_STRING_JAR);
        }

        private static void setResource(final File targetYangDir, final MavenProject project) {
            Resource res = new Resource();
            res.setDirectory(targetYangDir.getPath());
            project.addResource(res);
        }
    }

    /**
     * Call generate on every generator from plugin configuration
     */
    private void generateSources(final ContextHolder context) throws MojoFailureException {
        if (codeGenerators.size() == 0) {
            LOG.warn("{} No code generators provided", LOG_PREFIX);
            return;
        }

        Map<String, String> thrown = Maps.newHashMap();
        for (CodeGeneratorArg codeGenerator : codeGenerators) {
            try {
                generateSourcesWithOneGenerator(context, codeGenerator);
            } catch (Exception e) {
                // try other generators, exception will be thrown after
                LOG.error("{} Unable to generate sources with {} generator", LOG_PREFIX, codeGenerator
                        .getCodeGeneratorClass(), e);
                thrown.put(codeGenerator.getCodeGeneratorClass(), e.getClass().getCanonicalName());
            }
        }

        if (!thrown.isEmpty()) {
            String message = " One or more code generators failed, including failed list(generatorClass=exception) ";
            LOG.error("{}" + message + "{}", LOG_PREFIX, thrown.toString());
            throw new MojoFailureException(LOG_PREFIX + message + thrown.toString());
        }
    }

    /**
     * Instantiate generator from class and call required method
     */
    private void generateSourcesWithOneGenerator(final ContextHolder context, final CodeGeneratorArg codeGeneratorCfg)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

        codeGeneratorCfg.check();

        BasicCodeGenerator g = Util.getInstance(codeGeneratorCfg.getCodeGeneratorClass(), BasicCodeGenerator.class);
        LOG.info("{} Code generator instantiated from {}", LOG_PREFIX, codeGeneratorCfg.getCodeGeneratorClass());

        File outputDir = codeGeneratorCfg.getOutputBaseDir(project);

        if (outputDir != null) {
          project.addCompileSourceRoot(outputDir.getAbsolutePath());
        } else {
          throw new NullPointerException("outputBaseDir is null. Please provide a valid outputBaseDir value in the " +
                  "pom.xml");
        }

        LOG.info("{} Sources will be generated to {}", LOG_PREFIX, outputDir);
        LOG.debug("{} Project root dir is {}", LOG_PREFIX, project.getBasedir());
        LOG.debug("{} Additional configuration picked up for : {}: {}", LOG_PREFIX, codeGeneratorCfg
                        .getCodeGeneratorClass(), codeGeneratorCfg.getAdditionalConfiguration());

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
        LOG.debug("{} Folder: {} marked as resources for generator: {}", LOG_PREFIX, resourceBaseDir,
                codeGeneratorCfg.getCodeGeneratorClass());

        Collection<File> generated = g.generateSources(context.getContext(), outputDir, context.getYangModules());

        LOG.info("{} Sources generated by {}: {}", LOG_PREFIX, codeGeneratorCfg.getCodeGeneratorClass(), generated);
    }

}
