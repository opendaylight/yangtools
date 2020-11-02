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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;
import org.opendaylight.yangtools.yang2sources.plugin.ConfigArg.CodeGeneratorArg;
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
    private final List<CodeGeneratorArg> codeGeneratorArgs;
    private final Map<String, FileGeneratorArg> fileGeneratorArgs;
    private final MavenProject project;
    private final boolean inspectDependencies;
    private final BuildContext buildContext;
    private final YangProvider yangProvider;

    private YangToSourcesProcessor(final BuildContext buildContext, final File yangFilesRootDir,
            final Collection<File> excludedFiles, final List<CodeGeneratorArg> codeGeneratorArgs,
            final List<FileGeneratorArg> fileGeneratorsArgs,
            final MavenProject project, final boolean inspectDependencies, final YangProvider yangProvider) {
        this.buildContext = requireNonNull(buildContext, "buildContext");
        this.yangFilesRootDir = requireNonNull(yangFilesRootDir, "yangFilesRootDir");
        this.excludedFiles = ImmutableSet.copyOf(excludedFiles);
        this.codeGeneratorArgs = ImmutableList.copyOf(codeGeneratorArgs);
        this.fileGeneratorArgs = Maps.uniqueIndex(fileGeneratorsArgs, FileGeneratorArg::getIdentifier);
        this.project = requireNonNull(project);
        this.inspectDependencies = inspectDependencies;
        this.yangProvider = requireNonNull(yangProvider);
        this.parserFactory = DEFAULT_PARSER_FACTORY;
    }

    @VisibleForTesting
    YangToSourcesProcessor(final File yangFilesRootDir, final Collection<File> excludedFiles,
            final List<CodeGeneratorArg> codeGenerators, final MavenProject project, final boolean inspectDependencies,
            final YangProvider yangProvider) {
        this(new DefaultBuildContext(), yangFilesRootDir, excludedFiles, codeGenerators, ImmutableList.of(),
            project, inspectDependencies, yangProvider);
    }

    YangToSourcesProcessor(final BuildContext buildContext, final File yangFilesRootDir,
                final Collection<File> excludedFiles, final List<CodeGeneratorArg> codeGenerators,
                final List<FileGeneratorArg> fileGenerators, final MavenProject project,
                final boolean inspectDependencies) {
        this(buildContext, yangFilesRootDir, excludedFiles, codeGenerators, fileGenerators, project,
            inspectDependencies, YangProvider.getInstance());
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        conditionalExecute(false);
    }

    void conditionalExecute(final boolean skip) throws MojoExecutionException, MojoFailureException {
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

        final Set<StatementParserMode> parserModes = codeGenerators.stream()
            .map(GeneratorTaskFactory::parserMode)
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

        /*
         * Check if any of the listed files changed. If no changes occurred, simply return empty, which indicates
         * end of execution.
         */
        if (!allFiles.stream().anyMatch(buildContext::hasDelta)) {
            LOG.info("{} None of {} input files changed", LOG_PREFIX, allFiles.size());
            return;
        }

        final Stopwatch watch = Stopwatch.createStarted();
        final List<Entry<YangTextSchemaSource, IRSchemaSource>> parsed = yangFilesInProject.parallelStream()
            .map(file -> {
                final YangTextSchemaSource textSource = YangTextSchemaSource.forFile(file);
                try {
                    return Map.entry(textSource,TextToIRTransformer.transformText(textSource));
                } catch (YangSyntaxErrorException | IOException e) {
                    throw new IllegalArgumentException("Failed to parse " + file, e);
                }
            })
            .collect(Collectors.toList());
        LOG.debug("Found project files: {}", yangFilesInProject);
        LOG.info("{} Project model files found: {} in {}", LOG_PREFIX, yangFilesInProject.size(), watch);

        // FIXME: store these files into state, so that we can verify/clean up
        final Builder<File> files = ImmutableSet.builder();
        for (StatementParserMode parserMode : parserModes) {
            final Optional<ProcessorModuleReactor> optReactor = createReactor(yangFilesInProject,
                parserMode, dependencies, parsed);
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
                    files.addAll(generateSources(holder, codeGenerators, parserMode));
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
    }

    private List<GeneratorTaskFactory> instantiateGenerators() throws MojoExecutionException, MojoFailureException {
        final List<GeneratorTaskFactory> generators = new ArrayList<>(codeGeneratorArgs.size());
        for (CodeGeneratorArg arg : codeGeneratorArgs) {
            generators.add(CodeGeneratorTaskFactory.create(arg));
            LOG.info("{} Code generator instantiated from {}", LOG_PREFIX, arg.getCodeGeneratorClass());
        }

        // Search for available FileGenerator implementations
        final Map<String, FileGeneratorFactory> factories = Maps.uniqueIndex(
            ServiceLoader.load(FileGeneratorFactory.class), FileGeneratorFactory::getIdentifier);

        // Assign instantiate FileGenerators with appropriate configurate
        for (Entry<String, FileGeneratorFactory> entry : factories.entrySet()) {
            final String id = entry.getKey();
            FileGeneratorArg arg = fileGeneratorArgs.get(id);
            if (arg == null) {
                LOG.debug("{} No configuration for {}, using empty", LOG_PREFIX, id);
                arg = new FileGeneratorArg(id);
            }

            try {
                generators.add(FileGeneratorTaskFactory.of(entry.getValue(), arg));
            } catch (FileGeneratorException e) {
                throw new MojoExecutionException("File generator " + id + " failed", e);
            }
            LOG.info("{} Code generator {} instantiated", LOG_PREFIX, id);
        }

        return generators;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private Optional<ProcessorModuleReactor> createReactor(final List<File> yangFilesInProject,
            final StatementParserMode parserMode, final Collection<ScannedDependency> dependencies,
            final List<Entry<YangTextSchemaSource, IRSchemaSource>> parsed) throws MojoExecutionException {

        try {
            final List<YangTextSchemaSource> sourcesInProject = new ArrayList<>(yangFilesInProject.size());
            final YangParser parser = parserFactory.createParser(parserMode);
            for (final Entry<YangTextSchemaSource, IRSchemaSource> entry : parsed) {
                final YangTextSchemaSource textSource = entry.getKey();
                final IRSchemaSource astSource = entry.getValue();
                parser.addSource(astSource);

                if (!astSource.getIdentifier().equals(textSource.getIdentifier())) {
                    // AST indicates a different source identifier, make sure we use that
                    sourcesInProject.add(YangTextSchemaSource.delegateForByteSource(astSource.getIdentifier(),
                        textSource));
                } else {
                    sourcesInProject.add(textSource);
                }
            }

            final ProcessorModuleReactor reactor = new ProcessorModuleReactor(parser, sourcesInProject, dependencies);
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
    private Set<File> generateSources(final ContextHolder context, final Collection<GeneratorTaskFactory> generators,
            final StatementParserMode parserMode) throws MojoFailureException {
        final Builder<File> allFiles = ImmutableSet.builder();
        for (GeneratorTaskFactory factory : generators) {
            if (!parserMode.equals(factory.parserMode())) {
                continue;
            }

            final Stopwatch sw = Stopwatch.createStarted();
            final GeneratorTask<?> task = factory.createTask(project, context);
            LOG.debug("{} Task {} initialized in {}", LOG_PREFIX, task, sw);

            final Collection<File> files;
            try {
                files = task.execute(buildContext);
            } catch (FileGeneratorException | IOException e) {
                throw new MojoFailureException(LOG_PREFIX + " Generator " + factory + " failed", e);
            }

            LOG.debug("{} Sources generated by {}: {}", LOG_PREFIX, factory.generatorName(), files);
            LOG.info("{} Sources generated by {}: {} in {}", LOG_PREFIX, factory.generatorName(),
                files == null ? 0 : files.size(), sw);
            allFiles.addAll(files);
        }

        return allFiles.build();
    }
}
