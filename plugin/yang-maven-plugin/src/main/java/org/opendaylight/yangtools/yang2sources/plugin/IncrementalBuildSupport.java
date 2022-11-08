/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for incremental builds. It provides interface between {@link YangToSourcesProcessor} execution, observed
 * project input state (like dependencies, project YANG files, plugins and their configuration, etc.) and the contents
 * of the project build directory.
 *
 * <p>
 * While the logic here could be integrated, we keep it separate so we can test it separately.
 */
final class IncrementalBuildSupport {
    private static final Logger LOG = LoggerFactory.getLogger(IncrementalBuildSupport.class);

    private final @NonNull ImmutableMap<String, FileGeneratorArg> fileGeneratorArgs;
    private final @NonNull FileStateSet projectYangs;
    private final @NonNull FileStateSet dependencyYangs;

    private final @Nullable YangToSourcesState previousState;

    IncrementalBuildSupport(final YangToSourcesState previousState,
            final ImmutableMap<String, FileGeneratorArg> fileGeneratorArgs, final FileStateSet projectYangs,
            final FileStateSet dependencyYangs) {
        this.previousState = previousState;
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
        // Local variable to aid null analysis
        final var local = previousState;
        return local == null
            || !fileGeneratorArgs.equals(local.fileGeneratorArgs())
            || !projectYangs.equals(local.projectYangs())
            || !dependencyYangs.equals(local.dependencyYangs());
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
        // Local variable to aid null analysis
        final var local = previousState;
        final var outputFiles = local == null ? ImmutableMap.<String, FileState>of() : local.outputFiles().fileStates();

        // Compare explicit mentions first
        if (outputsChanged(outputFiles.values())) {
            return true;
        }

        // Make sure all discovered files for codegen plugins are accounted for
        for (var pluginName : fileGeneratorArgs.keySet()) {
            final var mismatch = Files.walk(Path.of(projectBuildDirectory, pluginName))
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .allMatch(path -> {
                    if (outputFiles.containsKey(path)) {
                        return true;
                    }
                    LOG.debug("{}: unexpected output file {}", YangToSourcesProcessor.LOG_PREFIX, path);
                    return false;
                });
            if (mismatch) {
                return true;
            }
        }

        return false;
    }

    private static boolean outputsChanged(final ImmutableCollection<FileState> outputFiles) throws IOException {
        for (var prev : outputFiles) {
            final var current = FileState.ofFile(Path.of(prev.path()));
            if (!prev.equals(current)) {
                LOG.debug("{}: output file changed from {} to {}", YangToSourcesProcessor.LOG_PREFIX, prev, current);
                return true;
            }
        }
        return false;
    }

    @NonNull YangToSourcesState reconcileOutputFiles(final Map<String, FileState> uniqueOutputFiles) {
        // TODO Auto-generated method stub
        return null;
    }
}
