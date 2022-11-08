/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.repo.api.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

class YangToSourcesProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(YangToSourcesProcessor.class);
    private static final YangParserFactory DEFAULT_PARSER_FACTORY;

    static {
        final Iterator<YangParserFactory> it = ServiceLoader.load(YangParserFactory.class).iterator();
        checkState(it.hasNext(), "Failed to find a YangParserFactory implementation");
        DEFAULT_PARSER_FACTORY = it.next();
    }

    static final String LOG_PREFIX = "yang-to-sources:";
    private static final String META_INF_STR = "META-INF";
    private static final String YANG_STR = "yang";

    static final String META_INF_YANG_STRING = META_INF_STR + File.separator + YANG_STR;
    static final String META_INF_YANG_STRING_JAR = META_INF_STR + "/" + YANG_STR;
    static final String META_INF_YANG_SERVICES_STRING_JAR = META_INF_STR + "/" + "services";

    private final YangParserFactory parserFactory;
    private final File yangFilesRootDir;
    private final Set<File> excludedFiles;
    private final Map<String, FileGeneratorArg> fileGeneratorArgs;
    private final @NonNull MavenProject project;
    private final boolean inspectDependencies;
    private final BuildContext buildContext;
    private final YangProvider yangProvider;

    private YangToSourcesProcessor(final BuildContext buildContext, final File yangFilesRootDir,
            final Collection<File> excludedFiles, final List<FileGeneratorArg> fileGeneratorsArgs,
            final MavenProject project, final boolean inspectDependencies, final YangProvider yangProvider) {
        this.buildContext = requireNonNull(buildContext, "buildContext");
        this.yangFilesRootDir = requireNonNull(yangFilesRootDir, "yangFilesRootDir");
        this.excludedFiles = ImmutableSet.copyOf(excludedFiles);
        //FIXME multiple FileGeneratorArg entries of same identifier became one here
        fileGeneratorArgs = Maps.uniqueIndex(fileGeneratorsArgs, FileGeneratorArg::getIdentifier);
        this.project = requireNonNull(project);
        this.inspectDependencies = inspectDependencies;
        this.yangProvider = requireNonNull(yangProvider);
        parserFactory = DEFAULT_PARSER_FACTORY;
    }

    @VisibleForTesting
    YangToSourcesProcessor(final File yangFilesRootDir, final Collection<File> excludedFiles,
            final List<FileGeneratorArg> fileGenerators, final MavenProject project, final boolean inspectDependencies,
            final YangProvider yangProvider) {
        this(new DefaultBuildContext(), yangFilesRootDir, excludedFiles, ImmutableList.of(),
            project, inspectDependencies, yangProvider);
    }

    YangToSourcesProcessor(final BuildContext buildContext, final File yangFilesRootDir,
            final Collection<File> excludedFiles, final List<FileGeneratorArg> fileGenerators,
            final MavenProject project, final boolean inspectDependencies) {
        this(buildContext, yangFilesRootDir, excludedFiles, fileGenerators, project, inspectDependencies,
            YangProvider.getInstance());
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        conditionalExecute(false);
    }

    void conditionalExecute(final boolean skip) throws MojoExecutionException, MojoFailureException {

        final var rebuildContext = new RebuildContext(new File(project.getBuild().getDirectory()), buildContext);

        /*
         * Collect all files which affect YANG context. This includes all
         * files in current project and optionally any jars/files in the
         * dependencies.
         */
        final List<File> yangFilesInProject;
        try {
            yangFilesInProject = listFiles(yangFilesRootDir, excludedFiles);
        } catch (IOException e) {
            throw new MojoFailureException("Failed to list project files", e);
        }

        if (yangFilesInProject.isEmpty()) {
            // No files to process, skip.
            LOG.info("{} No input files found", LOG_PREFIX);
            return;
        }

        // We need to instantiate all code generators to determine required import resolution mode
        final List<GeneratorTaskFactory> codeGenerators = instantiateGenerators();
        if (codeGenerators.isEmpty()) {
            LOG.warn("{} No code generators provided", LOG_PREFIX);
            return;
        }

        final Set<YangParserConfiguration> parserConfigs = codeGenerators.stream()
            .map(GeneratorTaskFactory::parserConfig)
            .collect(Collectors.toUnmodifiableSet());

        LOG.info("{} Inspecting {}", LOG_PREFIX, yangFilesRootDir);

        final Collection<File> allFiles = new ArrayList<>(yangFilesInProject);
        final Collection<ScannedDependency> dependencies;
        if (inspectDependencies) {
            dependencies = new ArrayList<>();
            final Stopwatch watch = Stopwatch.createStarted();

            try {
                ScannedDependency.scanDependencies(project).forEach(dep -> {
                    allFiles.add(dep.file());
                    dependencies.add(dep);
                });
            } catch (IOException e) {
                LOG.error("{} Failed to scan dependencies", LOG_PREFIX, e);
                throw new MojoExecutionException(LOG_PREFIX + " Failed to scan dependencies ", e);
            }
            LOG.info("{} Found {} dependencies in {}", LOG_PREFIX, dependencies.size(), watch);
        } else {
            dependencies = ImmutableList.of();
        }

        // register input files
        rebuildContext.setInputFiles(allFiles);

        // register configurations
        rebuildContext.setConfigurations(fileGeneratorArgs);

        /*
         * Check if any of the listed files changed. If no changes occurred, simply return empty, which indicates
         * end of execution.
         */
        if (!rebuildContext.hasChanges()) {
            LOG.info("{} None of {} input files changed", LOG_PREFIX, allFiles.size());
            return;
        }

        LOG.debug("Found project files: {}", yangFilesInProject);
        LOG.info("{} Project model files found: {}", LOG_PREFIX, yangFilesInProject.size());

        final var outputFiles = ImmutableList.<ResourceState>builder();

        for (YangParserConfiguration parserConfig : parserConfigs) {
            final Optional<ProcessorModuleReactor> optReactor = createReactor(yangFilesInProject, parserConfig,
                    dependencies);
            if (optReactor.isPresent()) {
                final ProcessorModuleReactor reactor = optReactor.orElseThrow();

                if (!skip) {
                    final Stopwatch sw = Stopwatch.createStarted();
                    final ContextHolder holder;

                    try {
                        holder = reactor.toContext();
                    } catch (YangParserException e) {
                        throw new MojoFailureException("Failed to process reactor " + reactor, e);
                    } catch (IOException e) {
                        throw new MojoExecutionException("Failed to read reactor " + reactor, e);
                    }

                    LOG.info("{} {} YANG models processed in {}", LOG_PREFIX, holder.getContext().getModules().size(),
                        sw);
                    outputFiles.addAll(generateSources(holder, codeGenerators, parserConfig));
                } else {
                    LOG.info("{} Skipping YANG code generation because property yang.skip is true", LOG_PREFIX);
                }

                // FIXME: this is not right: we should be generating the models exactly once!
                // add META_INF/yang
                final Collection<YangTextSchemaSource> models = reactor.getModelsInProject();
                try {
                    yangProvider.addYangsToMetaInf(project, models);
                } catch (IOException e) {
                    throw new MojoExecutionException("Failed write model files for " + models, e);
                }
            }
        }

        // add META_INF/services
        File generatedServicesDir = new GeneratedDirectories(project).getYangServicesDir();
        YangProvider.setResource(generatedServicesDir, project);
        LOG.debug("{} Yang services files from: {} marked as resources: {}", LOG_PREFIX, generatedServicesDir,
            META_INF_YANG_SERVICES_STRING_JAR);

        final var uniqueOutputFiles = new LinkedHashMap<String, ResourceState>();
        for (var fileHash : outputFiles.build()) {
            final var prev = uniqueOutputFiles.putIfAbsent(fileHash.identifier(), fileHash);
            if (prev != null) {
                throw new MojoFailureException("Duplicate files " + prev + " and " + fileHash);
            }
        }

        // TODO: add yang files from meta-inf to outputFiles
        // register output
        rebuildContext.setOutputFileStates(ImmutableMap.copyOf(uniqueOutputFiles));
        // TODO: clear outputs remaining from prior build
        // persist resource states for next build
        rebuildContext.persistState();
    }

    private List<GeneratorTaskFactory> instantiateGenerators() throws MojoExecutionException {
        // Search for available FileGenerator implementations
        final Map<String, FileGeneratorFactory> factories = Maps.uniqueIndex(
            ServiceLoader.load(FileGeneratorFactory.class), FileGeneratorFactory::getIdentifier);

        // FIXME: iterate over fileGeneratorArg instances (configuration), not factories (environment)
        // Assign instantiate FileGenerators with appropriate configuration
        final List<GeneratorTaskFactory> generators = new ArrayList<>(factories.size());
        for (Entry<String, FileGeneratorFactory> entry : factories.entrySet()) {
            final String id = entry.getKey();
            FileGeneratorArg arg = fileGeneratorArgs.get(id);
            if (arg == null) {
                LOG.debug("{} No configuration for {}, using empty", LOG_PREFIX, id);
                arg = new FileGeneratorArg(id);
            }

            try {
                generators.add(GeneratorTaskFactory.of(entry.getValue(), arg));
            } catch (FileGeneratorException e) {
                throw new MojoExecutionException("File generator " + id + " failed", e);
            }
            LOG.info("{} Code generator {} instantiated", LOG_PREFIX, id);
        }

        // Notify if no factory found for defined identifiers
        fileGeneratorArgs.keySet().forEach(
            fileGenIdentifier -> {
                if (!factories.containsKey(fileGenIdentifier)) {
                    LOG.warn("{} No generator found for identifier {}", LOG_PREFIX, fileGenIdentifier);
                }
            }
        );

        return generators;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private Optional<ProcessorModuleReactor> createReactor(final List<File> yangFilesInProject,
            final YangParserConfiguration parserConfig, final Collection<ScannedDependency> dependencies)
            throws MojoExecutionException {

        try {
            final List<YangTextSchemaSource> sourcesInProject = new ArrayList<>(yangFilesInProject.size());
            final YangParser parser = parserFactory.createParser(parserConfig);

            for (final File yangFile : yangFilesInProject) {
                final YangTextSchemaSource textSource = YangTextSchemaSource.forPath(yangFile.toPath());
                final YangIRSchemaSource astSource = TextToIRTransformer.transformText(textSource);
                parser.addSource(astSource);

                if (!astSource.getIdentifier().equals(textSource.getIdentifier())) {
                    // AST indicates a different source identifier, make sure we use that
                    sourcesInProject.add(YangTextSchemaSource.delegateForByteSource(astSource.getIdentifier(),
                        textSource));
                } else {
                    sourcesInProject.add(textSource);
                }
            }

            final ProcessorModuleReactor reactor =
                    new ProcessorModuleReactor(parser, sourcesInProject, dependencies);
            LOG.debug("Initialized reactor {} with {}", reactor, yangFilesInProject);
            return Optional.of(reactor);
        } catch (IOException | YangSyntaxErrorException | RuntimeException e) {
            // MojoExecutionException is thrown since execution cannot continue
            LOG.error("{} Unable to parse YANG files from {}", LOG_PREFIX, yangFilesRootDir, e);
            Throwable rootCause = Throwables.getRootCause(e);
            throw new MojoExecutionException(LOG_PREFIX + " Unable to parse YANG files from " + yangFilesRootDir,
                rootCause);
        }
    }

    private static List<File> listFiles(final File root, final Collection<File> excludedFiles)
            throws IOException {
        if (!root.isDirectory()) {
            LOG.warn("{} YANG source directory {} not found. No code will be generated.", LOG_PREFIX, root);
            return ImmutableList.of();
        }

        return Files.walk(root.toPath()).map(Path::toFile).filter(File::isFile).filter(f -> {
            if (excludedFiles.contains(f)) {
                LOG.info("{} YANG file excluded {}", LOG_PREFIX, f);
                return false;
            }
            return true;
        }).filter(f -> f.getName().endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION)).collect(Collectors.toList());
    }

    /**
     * Call generate on every generator from plugin configuration.
     */
    private List<ResourceState> generateSources(final ContextHolder context,
            final Collection<GeneratorTaskFactory> generators, final YangParserConfiguration parserConfig)
                throws MojoFailureException {
        final var generatorToFiles = ImmutableList.<ResourceState>builder();
        for (GeneratorTaskFactory factory : generators) {
            if (!parserConfig.equals(factory.parserConfig())) {
                continue;
            }

            final Stopwatch sw = Stopwatch.createStarted();
            final GeneratorTask task = factory.createTask(project, context);
            LOG.debug("{} Task {} initialized in {}", LOG_PREFIX, task, sw);

            final List<ResourceState> files;
            try {
                files = task.execute(buildContext);
            } catch (FileGeneratorException | IOException e) {
                throw new MojoFailureException(LOG_PREFIX + " Generator " + factory + " failed", e);
            }

            final String generatorName = factory.generatorName();
            LOG.debug("{} Sources generated by {}: {}", LOG_PREFIX, generatorName, files);

            final int fileCount = files.size();
            generatorToFiles.addAll(files);

            LOG.info("{} Sources generated by {}: {} in {}", LOG_PREFIX, generatorName, fileCount, sw);
        }

        return generatorToFiles.build();
    }
}
