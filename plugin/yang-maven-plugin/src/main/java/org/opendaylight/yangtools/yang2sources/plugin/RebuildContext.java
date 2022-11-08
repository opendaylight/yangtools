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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Artifact serves recognition of modified resources and output files to support incremental builds.
 * The states are persisted in a requested directory.
 */
final class RebuildContext {
    private static final Logger LOG = LoggerFactory.getLogger(RebuildContext.class);

    static final String PERSISTENCE_FILE_NAME = "rebuild-context-cache";
    static final String PERSISTENCE_KEY = YangToSourcesProcessor.class.getName();

    private final BuildContext buildContext;
    private final YangToSourcesState previousState;
    private final Path persistenceFile;

    private ImmutableMap<String, FileState> inputFilesStates;
    private ImmutableMap<String, FileGeneratorArg> configurations;
    private ImmutableMap<String, FileState> outputFileStates;

    private ImmutableList<File> obsoleteFiles;

    RebuildContext(final @NonNull File dir, final @NonNull BuildContext buildContext) {
        requireNonNull(dir);
        this.buildContext = requireNonNull(buildContext);
        persistenceFile = dir.toPath().resolve(PERSISTENCE_FILE_NAME);
        previousState = loadPrevState();
    }

    private YangToSourcesState loadPrevState() {
        final YangToSourcesState state = (YangToSourcesState) buildContext.getValue(PERSISTENCE_KEY);
        if (state != null) {
            return state;
        }
        if (Files.isRegularFile(persistenceFile)) {
            try (var in = new DataInputStream(Files.newInputStream(persistenceFile))) {
                return YangToSourcesState.readFrom(in);
            } catch (final IOException e) {
                LOG.warn("Could not load from rebuild context file", e);
            }
        }
        return YangToSourcesState.empty();
    }

    /**
     * Persists collected state data.
     */
    void persistState() {
        final var stateData = new YangToSourcesState(FileStateSet.ofNullable(inputFilesStates),
            configurations == null ? ImmutableMap.of() : configurations, FileStateSet.ofNullable(outputFileStates));
        buildContext.setValue(PERSISTENCE_KEY, stateData);
        try (var out = new DataOutputStream(Files.newOutputStream(persistenceFile))) {
            stateData.writeTo(out);
        } catch (IOException e) {
            LOG.warn("Could not persist rebuild context file", e);
        }
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
     * @param configMap configuration id to object map
     */
    void setConfigurations(final @NonNull Map<String, FileGeneratorArg> configMap) {
        requireNonNull(configMap, "configMap should not be null");
        checkState(configurations == null, "configuration state map is already set");
        configurations = ImmutableMap.copyOf(configMap);
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
            if (!Objects.equals(state, previousState.outputFiles().get(path))) {
                buildContext.refresh(new File(path)); // notify output file added or modified
            }
        });
        obsoleteFiles = previousState.outputFiles().entrySet().stream()
                .filter(entry -> !outputFileStates.containsKey(entry.getKey()))
                .map(entry -> new File(entry.getKey())).collect(ImmutableList.toImmutableList());
    }

    /**
     * Detects rebuild context change.
     *
     * @return true if any of inputs or configuration or outputs is changed comparing to previous build, false otherwise
     */
    public boolean hasChanges() {
        return !previousState.inputFiles().equals(inputFilesStates)
            || !previousState.configurations().equals(configurations)
            || !isSameSetOfFiles(previousState.outputFiles().fileStates().values());
    }

    static FileState buildState(final File file) {
        try (var cis = new CapturingInputStream(new FileInputStream(file))) {
            cis.readAllBytes();
            return cis.toFileState(file.getPath());
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

    public int deleteObsoleteFiles() {
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
}
