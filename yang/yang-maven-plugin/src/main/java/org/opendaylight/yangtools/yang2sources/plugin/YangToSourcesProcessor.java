/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
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
    private final Set<File> excludedFiles;
    private final List<CodeGeneratorArg> codeGenerators;
    private final MavenProject project;
    private final boolean inspectDependencies;
    private final BuildContext buildContext;
    private final YangProvider yangProvider;
    private final YangTextSchemaContextResolver resolver;

    @VisibleForTesting
    YangToSourcesProcessor(final File yangFilesRootDir, final Collection<File> excludedFiles,
            final List<CodeGeneratorArg> codeGenerators, final MavenProject project, final boolean inspectDependencies,
            final YangProvider yangProvider) {
        this(new DefaultBuildContext(), yangFilesRootDir, excludedFiles, codeGenerators, project,
                inspectDependencies, yangProvider);
    }

    private YangToSourcesProcessor(final BuildContext buildContext, final File yangFilesRootDir,
            final Collection<File> excludedFiles, final List<CodeGeneratorArg> codeGenerators,
            final MavenProject project, final boolean inspectDependencies, final YangProvider yangProvider) {
        this.buildContext = Preconditions.checkNotNull(buildContext, "buildContext");
        this.yangFilesRootDir = Preconditions.checkNotNull(yangFilesRootDir, "yangFilesRootDir");
        this.excludedFiles = ImmutableSet.copyOf(excludedFiles);
        this.codeGenerators = ImmutableList.copyOf(codeGenerators);
        this.project = Preconditions.checkNotNull(project);
        this.inspectDependencies = inspectDependencies;
        this.yangProvider = yangProvider;
        this.resolver = YangTextSchemaContextResolver.create("maven-plugin");
    }

    YangToSourcesProcessor(final BuildContext buildContext, final File yangFilesRootDir,
                final Collection<File> excludedFiles, final List<CodeGeneratorArg> codeGenerators,
                final MavenProject project, final boolean inspectDependencies) {
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
            File generatedServicesDir = new GeneratedDirectories(project).getYangServicesDir();
            YangProvider.setResource(generatedServicesDir, project);
            LOG.debug("{} Yang services files from: {} marked as resources: {}", LOG_PREFIX, generatedServicesDir,
                    META_INF_YANG_SERVICES_STRING_JAR);


        } else {
            execute();
        }
    }

    @SuppressWarnings("checkstyle:illegalCatch")
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
            final Set<Module> projectYangModules = new HashSet<>();
            final Set<Module> projectYangFiles = new HashSet<>();
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
                for (Module module : parsedAllYangModules) {
                    if (containedInFiles(yangsInProject, module)) {
                        LOG.debug("Module {} belongs to current project", module);
                        projectYangModules.add(module);
                        projectYangFiles.add(module);

                        for (Module sub : module.getSubmodules()) {
                            if (containedInFiles(yangsInProject, sub)) {
                                LOG.debug("Submodule {} belongs to current project", sub);
                                projectYangFiles.add(sub);
                            } else {
                                LOG.warn("Submodule {} not found in input files", sub);
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
            LOG.debug("Project YANG files: {}", projectYangFiles);

            return new ContextHolder(resolveSchemaContext, projectYangModules, projectYangFiles);

            // MojoExecutionException is thrown since execution cannot continue
        } catch (Exception e) {
            LOG.error("{} Unable to parse {} files from {}", LOG_PREFIX, Util.YANG_SUFFIX, yangFilesRootDir, e);
            Throwable rootCause = Throwables.getRootCause(e);
            throw new MojoExecutionException(LOG_PREFIX + " Unable to parse " + Util.YANG_SUFFIX + " files from "
                    + yangFilesRootDir, rootCause);
        }
    }

    private static boolean containedInFiles(final List<NamedFileInputStream> files, final Module module) {
        final String path = module.getModuleSourcePath();
        if (path != null) {
            LOG.debug("Looking for source {}", path);
            for (NamedFileInputStream is : files) {
                LOG.debug("In project destination {}", is.getFileDestination());
                if (path.equals(is.getFileDestination())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static List<InputStream> toStreamsWithoutDuplicates(final List<YangSourceFromDependency> list)
            throws IOException {
        final Map<String, YangSourceFromDependency> byContent = new HashMap<>();

        for (YangSourceFromDependency yangFromDependency : list) {
            try (Reader reader = yangFromDependency.asCharSource(StandardCharsets.UTF_8).openStream()) {
                final String contents = CharStreams.toString(reader);
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

    /**
     * Call generate on every generator from plugin configuration.
     */
    @SuppressWarnings("checkstyle:illegalCatch")
    private void generateSources(final ContextHolder context) throws MojoFailureException {
        if (codeGenerators.size() == 0) {
            LOG.warn("{} No code generators provided", LOG_PREFIX);
            return;
        }

        final Map<String, String> thrown = new HashMap<>();
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
     * Instantiate generator from class and call required method.
     */
    private void generateSourcesWithOneGenerator(final ContextHolder context, final CodeGeneratorArg codeGeneratorCfg)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

        codeGeneratorCfg.check();

        final BasicCodeGenerator g = getInstance(codeGeneratorCfg.getCodeGeneratorClass(), BasicCodeGenerator.class);
        LOG.info("{} Code generator instantiated from {}", LOG_PREFIX, codeGeneratorCfg.getCodeGeneratorClass());

        final File outputDir = codeGeneratorCfg.getOutputBaseDir(project);

        if (outputDir == null) {
            throw new NullPointerException("outputBaseDir is null. Please provide a valid outputBaseDir value in the "
                    + "pom.xml");
        }

        project.addCompileSourceRoot(outputDir.getAbsolutePath());

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

        FileUtils.deleteDirectory(outputDir);
        LOG.info("{} Succesfully deleted output directory {}", LOG_PREFIX, outputDir);

        Collection<File> generated = g.generateSources(context.getContext(), outputDir, context.getYangModules(),
            context::moduleToResourcePath);

        LOG.info("{} Sources generated by {}: {}", LOG_PREFIX, codeGeneratorCfg.getCodeGeneratorClass(), generated);
    }

    /**
     * Instantiate object from fully qualified class name.
     */
    private static <T> T getInstance(final String codeGeneratorClass, final Class<T> baseType) throws
            ClassNotFoundException, InstantiationException, IllegalAccessException {
        final Class<?> clazz = Class.forName(codeGeneratorClass);

        Preconditions.checkArgument(baseType.isAssignableFrom(clazz), "Code generator %s has to implement %s", clazz,
            baseType);
        return baseType.cast(clazz.newInstance());
    }
}
