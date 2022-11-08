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
import static org.opendaylight.yangtools.yang2sources.plugin.RebuildContextUtils.buildState;
import static org.opendaylight.yangtools.yang2sources.plugin.RebuildContextUtils.isSame;
import static org.opendaylight.yangtools.yang2sources.plugin.RebuildContextUtils.isSameSetOfFiles;

import com.google.common.collect.ImmutableMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObject;
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

    private ImmutableMap<String, ResourceState> inputFilesStates;
    private ImmutableMap<String, ResourceState> configStates;
    private ImmutableMap<String, ResourceState> outputFileStates;

    RebuildContext(final @NonNull File dir, final @NonNull BuildContext buildContext) {
        requireNonNull(dir);
        this.buildContext = requireNonNull(buildContext);
        this.persistenceFile = dir.toPath().resolve(PERSISTENCE_FILE_NAME);
        this.previousState = loadPrevState();
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
        final var stateData = new YangToSourcesState(inputFilesStates == null ? ImmutableMap.of() : inputFilesStates,
                configStates == null ? ImmutableMap.of() : configStates,
                outputFileStates == null ? ImmutableMap.of() : outputFileStates);
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
        inputFilesStates = inputFiles.stream().map(RebuildContextUtils::buildState)
                .collect(ImmutableMap.toImmutableMap(ResourceState::identifier, state -> state));
    }

    /**
     * Sets current configuration states.
     *
     * @param configMap configuration id to object map
     */
    void setConfigurations(final @NonNull Map<String, ? extends WritableObject> configMap) {
        requireNonNull(configMap, "configMap should not be null");
        checkState(configStates == null, "configuration state map is already set");
        configStates = configMap.entrySet().stream().map(entry -> buildState(entry.getKey(), entry.getValue()))
                .collect(ImmutableMap.toImmutableMap(ResourceState::identifier, state -> state));
    }

    /**
     * Sets output file states.
     *
     * @param fileStateMap output file path to state map
     */

    void setOutputFileStates(final @NonNull ImmutableMap<String, ResourceState> fileStateMap) {
        requireNonNull(fileStateMap, "fileStateMap should not be null");
        checkState(outputFileStates == null, "output files state map is already set");
        outputFileStates = fileStateMap;
    }

    /**
     * Detects rebuild context change.
     *
     * @return true if any of inputs or configuration or outputs is changed comparing to previous build, false otherwise
     */
    public boolean hasChanges() {
        return !isSame(previousState.inputFiles(), inputFilesStates)
                || !isSame(previousState.configurations(), configStates)
                || !isSameSetOfFiles(previousState.outputFiles().values());
    }
}
