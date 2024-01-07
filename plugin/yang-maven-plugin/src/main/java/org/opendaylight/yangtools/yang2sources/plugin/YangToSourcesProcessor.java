/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.spi.source.DelegatedYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.FileYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

// FIXME: rename to Execution
// FIXME: final
class YangToSourcesProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(YangToSourcesProcessor.class);
    private static final YangParserFactory DEFAULT_PARSER_FACTORY;

    static {
        try {
            DEFAULT_PARSER_FACTORY = ServiceLoader.load(YangParserFactory.class).iterator().next();
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("Failed to find a YangParserFactory implementation", e);
        }
    }

    private static final String META_INF_STR = "META-INF";
    private static final String YANG_STR = "yang";

    static final String LOG_PREFIX = "yang-to-sources:";
    static final String META_INF_YANG_STRING = META_INF_STR + File.separator + YANG_STR;
    static final String META_INF_YANG_STRING_JAR = META_INF_STR + "/" + YANG_STR;
    static final String META_INF_YANG_SERVICES_STRING_JAR = META_INF_STR + "/" + "services";

    private static final YangProvider YANG_PROVIDER = (project, modelsInProject) -> {
        final var generatedYangDir =
            // FIXME: why are we generating these in "generated-sources"? At the end of the day YANG files are more
            //        resources (except we do not them to be subject to filtering)
            new File(new File(project.getBuild().getDirectory(), "generated-sources"), "yang");
        LOG.debug("Generated dir {}", generatedYangDir);

        // copy project's src/main/yang/*.yang to ${project.builddir}/generated-sources/yang/META-INF/yang/
        // This honors setups like a Eclipse-profile derived one
        final var withMetaInf = new File(generatedYangDir, YangToSourcesProcessor.META_INF_YANG_STRING);
        Files.createDirectories(withMetaInf.toPath());

        final var stateListBuilder = ImmutableList.<FileState>builderWithExpectedSize(modelsInProject.size());
        for (var source : modelsInProject) {
            final File file = new File(withMetaInf, source.sourceId().toYangFilename());
            stateListBuilder.add(FileState.ofWrittenFile(file,
                out -> source.asByteSource(StandardCharsets.UTF_8).copyTo(out)));
            LOG.debug("Created file {} for {}", file, source.sourceId());
        }

        ProjectFileAccess.addResourceDir(project, generatedYangDir);
        LOG.debug("{} YANG files marked as resources: {}", YangToSourcesProcessor.LOG_PREFIX, generatedYangDir);

        return stateListBuilder.build();
    };

    private final YangParserFactory parserFactory;
    private final File yangFilesRootDir;
    private final Set<File> excludedFiles;
    private final ImmutableMap<String, FileGeneratorArg> fileGeneratorArgs;
    private final @NonNull MavenProject project;
    private final boolean inspectDependencies;
    private final @NonNull BuildContext buildContext;
    private final YangProvider yangProvider;
    private final StateStorage stateStorage;
    private final String projectBuildDirectory;

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
        projectBuildDirectory = project.getBuild().getDirectory();
        stateStorage = StateStorage.of(buildContext, stateFilePath(projectBuildDirectory));
        parserFactory = DEFAULT_PARSER_FACTORY;
    }

    @VisibleForTesting
    YangToSourcesProcessor(final File yangFilesRootDir, final List<FileGeneratorArg> fileGenerators,
            final MavenProject project, final YangProvider yangProvider) {
        this(new DefaultBuildContext(), yangFilesRootDir, List.of(), List.of(), project, false, yangProvider);
    }

    YangToSourcesProcessor(final BuildContext buildContext, final File yangFilesRootDir,
            final Collection<File> excludedFiles, final List<FileGeneratorArg> fileGenerators,
            final MavenProject project, final boolean inspectDependencies) {
        this(buildContext, yangFilesRootDir, excludedFiles, fileGenerators, project, inspectDependencies,
            YANG_PROVIDER);
    }

    void execute() throws MojoExecutionException, MojoFailureException {
        YangToSourcesState prevState;
        try {
            prevState = stateStorage.loadState();
        } catch (IOException e) {
            throw new MojoFailureException("Failed to restore execution state", e);
        }
        if (prevState == null) {
            LOG.debug("{} no previous execution state present", LOG_PREFIX);
            prevState = new YangToSourcesState(ImmutableMap.of(),
                    FileStateSet.empty(), FileStateSet.empty(), FileStateSet.empty());
        }

        // Collect all files in the current project.
        final List<File> yangFilesInProject;
        try {
            yangFilesInProject = listFiles(yangFilesRootDir, excludedFiles);
        } catch (IOException e) {
            throw new MojoFailureException("Failed to list project files", e);
        }

        if (yangFilesInProject.isEmpty()) {
            // No files to process, skip.
            LOG.info("{} No input files found", LOG_PREFIX);
            wipeAllState(prevState);
            return;
        }

        // We need to instantiate all code generators to determine required import resolution mode
        final var codeGenerators = instantiateGenerators();
        if (codeGenerators.isEmpty()) {
            LOG.warn("{} No code generators provided", LOG_PREFIX);
            wipeAllState(prevState);
            return;
        }

        LOG.info("{} Inspecting {}", LOG_PREFIX, yangFilesRootDir);

        // All files which affect YANG context. This minimally includes all files in the current project, but optionally
        // may include any YANG files in the dependencies.
        final List<ScannedDependency> dependencies;
        if (inspectDependencies) {
            final Stopwatch watch = Stopwatch.createStarted();
            try {
                dependencies = ScannedDependency.scanDependencies(project);
            } catch (IOException e) {
                LOG.error("{} Failed to scan dependencies", LOG_PREFIX, e);
                throw new MojoExecutionException(LOG_PREFIX + " Failed to scan dependencies ", e);
            }
            LOG.info("{} Found {} dependencies in {}", LOG_PREFIX, dependencies.size(), watch);
        } else {
            dependencies = List.of();
        }

        // Determine hash/size of YANG input files and dependencies in parallel
        final var hashTimer = Stopwatch.createStarted();
        final var projectYangs = new FileStateSet(yangFilesInProject.parallelStream()
            .map(file -> {
                try {
                    return FileState.ofFile(file);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read " + file, e);
                }
            })
            .collect(ImmutableMap.toImmutableMap(FileState::path, Function.identity())));
        // TODO: this produces false positives for Jar files -- there we want to capture the contents of the YANG files,
        //       not the entire file
        final var dependencyYangs = new FileStateSet(dependencies.parallelStream()
            .map(ScannedDependency::file)
            .map(file -> {
                try {
                    return FileState.ofFile(file);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read " + file, e);
                }
            })
            .collect(ImmutableMap.toImmutableMap(FileState::path, Function.identity())));
        LOG.debug("{} Input state determined in {}", LOG_PREFIX, hashTimer);

        // We have collected our current inputs and previous state. Instantiate a support object which will guide us for
        // the rest of the way.
        final var buildSupport = new IncrementalBuildSupport(prevState,
            codeGenerators.stream()
                .collect(ImmutableMap.toImmutableMap(GeneratorTask::getIdentifier, GeneratorTask::arg)),
            projectYangs, dependencyYangs);

        // Check if any inputs changed, which is supposed to be fast. If they did not, we need to also validate our
        // our previous are also up-to-date.
        if (!buildSupport.inputsChanged()) {
            final boolean outputsChanged;
            try {
                outputsChanged = buildSupport.outputsChanged(projectBuildDirectory);
            } catch (IOException e) {
                throw new MojoFailureException("Failed to reconcile generation outputs", e);
            }

            if (!outputsChanged) {
                // FIXME: YANGTOOLS-745: still need to add all resources/directories to maven project
                LOG.info("{}: Everything is up to date, nothing to do", LOG_PREFIX);
                return;
            }
        }

        final Stopwatch watch = Stopwatch.createStarted();

        final var parsed = yangFilesInProject.parallelStream()
            .map(file -> {
                final var textSource = new FileYangTextSource(file.toPath());
                try {
                    return Map.entry(textSource, TextToIRTransformer.transformText(textSource));
                } catch (YangSyntaxErrorException | IOException e) {
                    throw new IllegalArgumentException("Failed to parse " + file, e);
                }
            })
            .collect(Collectors.toList());
        LOG.debug("Found project files: {}", yangFilesInProject);
        LOG.info("{} Project model files found: {} in {}", LOG_PREFIX, yangFilesInProject.size(), watch);

        final var outputFiles = ImmutableList.<FileState>builder();
        Collection<YangTextSource> modelsInProject = null;
        for (var parserConfig : codeGenerators.stream().map(GeneratorTask::parserConfig).collect(Collectors.toSet())) {
            final var moduleReactor = createReactor(yangFilesInProject, parserConfig, dependencies, parsed);
            final var yangSw = Stopwatch.createStarted();

            final ContextHolder holder;
            try {
                holder = moduleReactor.toContext();
            } catch (YangParserException e) {
                throw new MojoFailureException("Failed to process reactor " + moduleReactor, e);
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to read reactor " + moduleReactor, e);
            }
            LOG.info("{} {} YANG models processed in {}", LOG_PREFIX, holder.getContext().getModules().size(), yangSw);

            for (var factory : codeGenerators) {
                if (!parserConfig.equals(factory.parserConfig())) {
                    continue;
                }

                final var genSw = Stopwatch.createStarted();
                final List<FileState> files;
                try {
                    files = factory.execute(project, buildContext, holder);
                } catch (FileGeneratorException e) {
                    throw new MojoFailureException(LOG_PREFIX + " Generator " + factory + " failed", e);
                }

                outputFiles.addAll(files);
                LOG.info("{} Sources generated by {}: {} in {}", LOG_PREFIX, factory.generatorName(), files.size(),
                    genSw);
            }

            if (modelsInProject == null) {
                // FIXME: this is an invariant, we should prepare these separately
                modelsInProject = moduleReactor.getModelsInProject();
            }
        }

        // add META_INF/yang once
        try {
            outputFiles.addAll(yangProvider.addYangsToMetaInf(project, modelsInProject));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed write model files for " + modelsInProject, e);
        }

        // add META_INF/services
        File generatedServicesDir = new File(new File(projectBuildDirectory, "generated-sources"), "spi");
        ProjectFileAccess.addResourceDir(project, generatedServicesDir);
        LOG.debug("{} Yang services files from: {} marked as resources: {}", LOG_PREFIX, generatedServicesDir,
            META_INF_YANG_SERVICES_STRING_JAR);

        final var uniqueOutputFiles = new LinkedHashMap<String, FileState>();
        for (var fileHash : outputFiles.build()) {
            final var prev = uniqueOutputFiles.putIfAbsent(fileHash.path(), fileHash);
            if (prev != null) {
                throw new MojoFailureException("Duplicate files " + prev + " and " + fileHash);
            }
        }

        // Reconcile push output files into project directory and acquire the execution state
        final YangToSourcesState outputState;
        try {
            outputState = buildSupport.reconcileOutputFiles(buildContext, projectBuildDirectory, uniqueOutputFiles);
        } catch (IOException e) {
            throw new MojoFailureException("Failed to reconcile output files", e);
        }

        // Store execution state
        try {
            stateStorage.storeState(outputState);
        } catch (IOException e) {
            throw new MojoFailureException("Failed to store execution state", e);
        }
    }

    private void wipeAllState(final YangToSourcesState prevState) throws MojoExecutionException {
        try {
            prevState.deleteOutputFiles();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to delete output files", e);
        }
        try {
            stateStorage.deleteState();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to remove execution state", e);
        }
    }

    private ImmutableList<@NonNull GeneratorTask> instantiateGenerators() throws MojoExecutionException {
        // Search for available FileGenerator implementations
        final var factories = Maps.uniqueIndex(
            ServiceLoader.load(FileGeneratorFactory.class), FileGeneratorFactory::getIdentifier);

        // FIXME: iterate over fileGeneratorArg instances (configuration), not factories (environment)
        // Assign instantiate FileGenerators with appropriate configuration
        final var builder = ImmutableList.<@NonNull GeneratorTask>builderWithExpectedSize(factories.size());
        for (var entry : factories.entrySet()) {
            final var id = entry.getKey();
            var arg = fileGeneratorArgs.get(id);
            if (arg == null) {
                LOG.debug("{} No configuration for {}, using empty", LOG_PREFIX, id);
                arg = new FileGeneratorArg(id);
            }

            final GeneratorTask task;
            try {
                task = new GeneratorTask(entry.getValue(), arg);
            } catch (FileGeneratorException e) {
                throw new MojoExecutionException("File generator " + id + " failed", e);
            }
            builder.add(task);
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

        return builder.build();
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private @NonNull ProcessorModuleReactor createReactor(final List<File> yangFilesInProject,
            final YangParserConfiguration parserConfig, final Collection<ScannedDependency> dependencies,
            final List<Entry<FileYangTextSource, YangIRSchemaSource>> parsed) throws MojoExecutionException {

        try {
            final var sourcesInProject = new ArrayList<YangTextSource>(yangFilesInProject.size());
            final var parser = parserFactory.createParser(parserConfig);
            for (var entry : parsed) {
                final var textSource = entry.getKey();
                final var astSource = entry.getValue();
                parser.addSource(astSource);

                if (!astSource.sourceId().equals(textSource.sourceId())) {
                    // AST indicates a different source identifier, make sure we use that
                    sourcesInProject.add(new DelegatedYangTextSource(astSource.sourceId(), textSource));
                } else {
                    sourcesInProject.add(textSource);
                }
            }

            final var moduleReactor = new ProcessorModuleReactor(parser, sourcesInProject, dependencies);
            LOG.debug("Initialized reactor {} with {}", moduleReactor, yangFilesInProject);
            return moduleReactor;
        } catch (IOException | YangSyntaxErrorException | RuntimeException e) {
            // MojoExecutionException is thrown since execution cannot continue
            LOG.error("{} Unable to parse YANG files from {}", LOG_PREFIX, yangFilesRootDir, e);
            throw new MojoExecutionException(LOG_PREFIX + " Unable to parse YANG files from " + yangFilesRootDir,
                Throwables.getRootCause(e));
        }
    }

    private static ImmutableList<File> listFiles(final File root, final Collection<File> excludedFiles)
            throws IOException {
        if (!root.isDirectory()) {
            LOG.warn("{} YANG source directory {} not found. No code will be generated.", LOG_PREFIX, root);
            return ImmutableList.of();
        }

        return Files.walk(root.toPath())
            .map(Path::toFile)
            .filter(File::isFile)
            .filter(f -> {
                if (excludedFiles.contains(f)) {
                    LOG.info("{} YANG file excluded {}", LOG_PREFIX, f);
                    return false;
                }
                return true;
            })
            .filter(f -> f.getName().endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION))
            .collect(ImmutableList.toImmutableList());
    }

    @VisibleForTesting
    static @NonNull Path stateFilePath(final String projectBuildDirectory) {
        // ${project.build.directory}/maven-status/yang-maven-plugin/execution.state
        return Path.of(projectBuildDirectory, "maven-status", "yang-maven-plugin", "execution.state");
    }
}
