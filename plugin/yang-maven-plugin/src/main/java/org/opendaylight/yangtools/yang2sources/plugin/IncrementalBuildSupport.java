/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Support for incremental builds. It provides interface between {@link YangToSourcesProcessor} execution, observed
 * project input state (like dependencies, project YANG files, plugins and their configuration, etc.) and the contents
 * of the project build directory.
 *
 * <p>
 * While the logic here could be integrated, we keep it separate so we can test it separately.
 */
// FIXME: rename to ExecutionSupport or something similar
final class IncrementalBuildSupport {
    private static final Logger LOG = LoggerFactory.getLogger(IncrementalBuildSupport.class);
    private static final List<String> TRANSIENT_DIRECTORIES = List.of("generated-resources", "generated-sources");

    private final @NonNull YangToSourcesState previousState;
    private final @NonNull ImmutableMap<String, FileGeneratorArg> fileGeneratorArgs;
    private final @NonNull FileStateSet projectYangs;
    private final @NonNull FileStateSet dependencyYangs;

    IncrementalBuildSupport(final YangToSourcesState previousState,
            final ImmutableMap<String, FileGeneratorArg> fileGeneratorArgs, final FileStateSet projectYangs,
            final FileStateSet dependencyYangs) {
        this.previousState = requireNonNull(previousState);
        this.fileGeneratorArgs = requireNonNull(fileGeneratorArgs);
        this.projectYangs = requireNonNull(projectYangs);
        this.dependencyYangs = requireNonNull(dependencyYangs);
    }

    /**
     * Compare previous state against current inputs and return {@core true} if any of the parameters changed.
     *
     * @return {@code true} if restored state is different from currently-observed state
     */
    boolean inputsChanged() {
        if (!fileGeneratorArgs.equals(previousState.fileGeneratorArgs())) {
            LOG.debug("{}: file generator arguments changed from {} to {}", YangToSourcesProcessor.LOG_PREFIX,
                fileGeneratorArgs, previousState.fileGeneratorArgs());
            return true;
        }
        if (!projectYangs.equals(previousState.projectYangs())) {
            LOG.debug("{}: project YANG files changed", YangToSourcesProcessor.LOG_PREFIX);
            return true;
        }
        if (!dependencyYangs.equals(previousState.dependencyYangs())) {
            LOG.debug("{}: dependency YANG files changed", YangToSourcesProcessor.LOG_PREFIX);
            return true;
        }
        return false;
    }

    /**
     * Compare previous state to the contents of the specified project build directory. This method detects any changes
     * to existence/contents of files recorded in previous state as well as any extra files created in per-generator
     * sub-directories of the project build directory.
     *
     * @param projectBuildDirectory Current project build directory
     * @return {@code true} if restored state and the build directory differ
     */
    boolean outputsChanged(final String projectBuildDirectory) throws IOException {
        // Compare explicit mentions first
        final var outputFiles = previousState.outputFiles().fileStates();
        if (outputsChanged(outputFiles.values())) {
            return true;
        }

        // Make sure all discovered files for codegen plugins are accounted for
        for (var pluginName : fileGeneratorArgs.keySet()) {
            for (var directory : TRANSIENT_DIRECTORIES) {
                final var dirPath = pluginSubdirectory(projectBuildDirectory, pluginName, directory);
                if (Files.isDirectory(dirPath)) {
                    final var mismatch = Files.walk(dirPath)
                        .filter(Files::isRegularFile)
                        .map(Path::toString)
                        .allMatch(path -> {
                            if (outputFiles.containsKey(path)) {
                                return true;
                            }
                            LOG.info("{}: unexpected output file {}", YangToSourcesProcessor.LOG_PREFIX, path);
                            return false;
                        });
                    if (!mismatch) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean outputsChanged(final Collection<FileState> outputFiles) throws IOException {
        for (var prev : outputFiles) {
            final var current = FileState.ofFile(Path.of(prev.path()));
            if (!prev.equals(current)) {
                LOG.debug("{}: output file changed from {} to {}", YangToSourcesProcessor.LOG_PREFIX, prev, current);
                LOG.info("{}: output file {} changed", YangToSourcesProcessor.LOG_PREFIX, prev.path());
                return true;
            }
        }
        return false;
    }

    @NonNull YangToSourcesState reconcileOutputFiles(final BuildContext buildContext,
            final String projectBuildDirectory, final Map<String, FileState> outputFiles) throws IOException {
        final var local = previousState;
        if (local != null) {
            // Delete any file mentioned in previous state and not output by us
            for (var file : local.outputFiles().fileStates().keySet()) {
                if (!outputFiles.containsKey(file)) {
                    final var path = Path.of(file);
                    if (Files.deleteIfExists(path)) {
                        buildContext.refresh(path.toFile());
                    }
                }
            }

            // Recursively delete all plugin directories configured
            for (var plugin : local.fileGeneratorArgs().keySet()) {
                if (!fileGeneratorArgs.containsKey(plugin)) {
                    for (var directory : TRANSIENT_DIRECTORIES) {
                        deleteRecursively(buildContext, pluginSubdirectory(projectBuildDirectory, plugin, directory));
                    }
                }
            }
        }

        // Now examine each plugin's output and remove any file not mentioned in outputs
        for (var plugin : fileGeneratorArgs.keySet()) {
            for (var directory : TRANSIENT_DIRECTORIES) {
                final var dirPath = pluginSubdirectory(projectBuildDirectory, plugin, directory);
                if (Files.isDirectory(dirPath)) {
                    Files.walk(dirPath)
                        .filter(Files::isRegularFile)
                        .filter(path -> !outputFiles.containsKey(path.toString()))
                        .map(Path::toFile)
                        .forEach(file -> {
                            if (file.delete()) {
                                // Notify BuildContext of the deleted file
                                buildContext.refresh(file);
                            }
                        });
                }
            }
        }

        return new YangToSourcesState(fileGeneratorArgs, projectYangs, dependencyYangs,
            new FileStateSet(ImmutableMap.copyOf(outputFiles)));
    }

    static Path pluginSubdirectory(final String projectBuildDirectory, final String pluginName, final String subdir) {
        return Path.of(projectBuildDirectory, subdir, pluginName);
    }

    private static void deleteRecursively(final BuildContext buildContext, final Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
                if (file.delete()) {
                    // Notify BuildContext of the deleted file
                    buildContext.refresh(file);
                }
            });
        }
    }
}
