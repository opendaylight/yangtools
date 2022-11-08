/*
 * Copyright (c) 2022 PANTHEON.tech and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Artifact serves recognition of modified resources and output files to support incremental builds.
 * The states are persisted in a requested directory.
 */
final class RebuildContext {
    private final BuildContext buildContext;
    private final YangToSourcesState previousState;

    private ImmutableMap<String, FileState> inputFilesStates;
    private ImmutableMap<String, FileGeneratorArg> fileGeneratorArgs;
    private ImmutableMap<String, FileState> outputFileStates;

    private ImmutableList<File> obsoleteFiles;

    RebuildContext(final @NonNull BuildContext buildContext, final @Nullable YangToSourcesState previousState) {
        this.buildContext = requireNonNull(buildContext);
        this.previousState = previousState != null ? previousState : YangToSourcesState.empty();
    }

    @NonNull YangToSourcesState toState() {
        return new YangToSourcesState(fileGeneratorArgs == null ? ImmutableMap.of() : fileGeneratorArgs,
            FileStateSet.ofNullable(inputFilesStates), FileStateSet.ofNullable(outputFileStates));
    }

    /**
     * Sets input files.
     *
     * @param inputFiles list of files
     */
    void setInputFiles(final @NonNull Collection<File> inputFiles) {
        requireNonNull(inputFiles, "inputFiles should not be null");
        checkArgument(!inputFiles.isEmpty(), "inputFiles should not be empty");
        checkState(inputFilesStates == null, "input files state map is already set");
        inputFilesStates = inputFiles.stream()
            .map(RebuildContext::buildState)
            .collect(ImmutableMap.toImmutableMap(FileState::path, state -> state));
    }

    /**
     * Sets current configuration states.
     *
     * @param fileGeneratorArgs configuration id to object map
     */
    void setFileGeneratorArgs(final @NonNull ImmutableMap<String, FileGeneratorArg> fileGeneratorArgs) {
        requireNonNull(fileGeneratorArgs, "configMap should not be null");
        checkState(this.fileGeneratorArgs == null, "configuration state map is already set");
        this.fileGeneratorArgs = fileGeneratorArgs;
    }

    /**
     * Sets output file states.
     *
     * @param fileStateMap output file path to state map
     */
    void setOutputFileStates(final @NonNull ImmutableMap<String, FileState> fileStateMap) {
        requireNonNull(fileStateMap, "fileStateMap should not be null");
        checkState(outputFileStates == null, "output files state map is already set");
        outputFileStates = fileStateMap;
        processOutputs();
    }

    private void processOutputs() {
        outputFileStates.forEach((path, state) -> {
            if (!Objects.equals(state, previousState.outputFiles().fileStates().get(path))) {
                // notify output file added or modified
                buildContext.refresh(new File(path));
            }
        });
        obsoleteFiles = previousState.outputFiles().fileStates().entrySet().stream()
                .filter(entry -> !outputFileStates.containsKey(entry.getKey()))
                .map(entry -> new File(entry.getKey())).collect(ImmutableList.toImmutableList());
    }

    /**
     * Detects rebuild context change.
     *
     * @return true if any of inputs or configuration or outputs is changed comparing to previous build, false otherwise
     */
    public boolean hasChanges() {
        return !previousState.projectYangs().fileStates().equals(projectYangFileStates)
            || !previousState.dependencyYangs().fileStates().equals(dependencyYangFilesStates)
            || !previousState.fileGeneratorArgs().equals(fileGeneratorArgs)
            || !isSameSetOfFiles(previousState.outputFiles().fileStates().values());
    }

    int deleteObsoleteFiles() {
        if (obsoleteFiles == null) {
            return 0;
        }
        int count = 0;
        for (File file : obsoleteFiles) {
            count += file.delete() ? 1 : 0;
            buildContext.refresh(file);
        }
        return count;
    }

    private static FileState buildState(final File file) {
        try {
            return FileState.ofFile(file);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isSameSetOfFiles(final Collection<FileState> states) {
        for (FileState state : states) {
            if (!Files.isRegularFile(Path.of(state.path()))) {
                return false;
            }
        }
        for (FileState state : states) {
            if (!state.equals(buildState(new File(state.path())))) {
                return false;
            }
        }
        return true;
    }
}
