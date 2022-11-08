/*
 * Copyright (c) 2022 PANTHEON.tech and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.sonatype.plexus.build.incremental.BuildContext;

@ExtendWith(MockitoExtension.class)
class RebuildContextTest {
    private static final String CONFIG_ID1 = "config-id-1";
    private static final String CONFIG_ID2 = "config-id-2";
    private static final WritableObject CONFIG_ORIGINAL = out -> out.writeUTF("config-original");
    private static final WritableObject CONFIG_MODIFIED = out -> out.writeUTF("config1-modified");
    private static final String FIRST_RUN = "First run";
    private static final String IN_FILE1 = "in-file-1";
    private static final String IN_FILE2 = "in-file-2";
    private static final String OUT_FILE1 = "out-file-1";
    private static final String OUT_FILE2 = "out-file-2";
    private static final String OUT_FILE3 = "out-file-3";
    private static final String OUT_FILE4 = "out-file-4";
    private static final String CONTENT1 = "content-1";
    private static final String CONTENT2 = "content-2";
    private static final Set<String> ALL_OUTPUTS = Set.of(OUT_FILE1, OUT_FILE2, OUT_FILE3, OUT_FILE4);

    @Mock
    private BuildContext buildContext;
    @Captor
    private ArgumentCaptor<File> fileCaptor1;
    @Captor
    private ArgumentCaptor<File> fileCaptor2;

    @TempDir
    private static File workDir;
    private File persistenceFile;

    @BeforeEach
    void setup() {
        persistenceFile = new File(workDir, RebuildContext.PERSISTENCE_FILE_NAME);
        when(buildContext.getValue(anyString())).thenReturn(null);
    }

    @ParameterizedTest(name = "Context change : {0}")
    @MethodSource("hasChangedArgs")
    void hasChanged(final String testDesc, final Map<String, WritableObject> configMap,
            final Map<String, String> inputDataMap, final Map<String, String> outputDataMap)
            throws Exception {
        if (FIRST_RUN.equals(testDesc)) {
            // ensure there is no persistence file on first run
            persistenceFile.delete();
        } else {
            assertTrue(persistenceFile.exists());
        }
        final List<File> inputFiles = createInputFiles(inputDataMap);
        final ImmutableMap<String, ResourceState> outputStates = createOutputStates(outputDataMap, true);
        final RebuildContext rebuildContext = new RebuildContext(workDir, buildContext);
        rebuildContext.setInputFiles(inputFiles);
        rebuildContext.setConfigurations(configMap);
        // validate expected state
        assertTrue(rebuildContext.hasChanges());
        // persist context; ensure state data is saved
        rebuildContext.setOutputFileStates(outputStates);
        rebuildContext.persistState();
        assertTrue(persistenceFile.exists());
        // next build case: ensure context is not changed for same inputs
        final RebuildContext nextRebuildContext = new RebuildContext(workDir, buildContext);
        nextRebuildContext.setInputFiles(inputFiles);
        nextRebuildContext.setConfigurations(configMap);
        assertFalse(nextRebuildContext.hasChanges());
    }

    private static Stream<Arguments> hasChangedArgs() {
        // test case description, configurations, input files name/content, output files name/content
        return Stream.of(
                Arguments.of(FIRST_RUN, Map.of(CONFIG_ID1, CONFIG_ORIGINAL),
                        Map.of(IN_FILE1, CONTENT1), Map.of(OUT_FILE1, CONTENT1)),
                Arguments.of("Config 1 modified", Map.of(CONFIG_ID1, CONFIG_MODIFIED),
                        Map.of(IN_FILE1, CONTENT1), Map.of(OUT_FILE1, CONTENT1)),
                Arguments.of("Config 2 added", Map.of(CONFIG_ID1, CONFIG_MODIFIED, CONFIG_ID2, CONFIG_ORIGINAL),
                        Map.of(IN_FILE1, CONTENT1), Map.of(OUT_FILE1, CONTENT1)),
                Arguments.of("Config 2 modified",
                        Map.of(CONFIG_ID1, CONFIG_MODIFIED, CONFIG_ID2, CONFIG_MODIFIED),
                        Map.of(IN_FILE1, CONTENT1), Map.of(OUT_FILE1, CONTENT1)),
                Arguments.of("Config 1 removed", Map.of(CONFIG_ID2, CONFIG_MODIFIED),
                        Map.of(IN_FILE1, CONTENT1), Map.of(OUT_FILE1, CONTENT1)),
                Arguments.of("Input 1 modified", Map.of(CONFIG_ID2, CONFIG_MODIFIED),
                        Map.of(IN_FILE1, CONTENT2), Map.of(OUT_FILE1, CONTENT1)),
                Arguments.of("Input 2 added", Map.of(CONFIG_ID2, CONFIG_MODIFIED),
                        Map.of(IN_FILE1, CONTENT2, IN_FILE2, CONTENT2), Map.of(OUT_FILE1, CONTENT1)),
                Arguments.of("Output modified", Map.of(CONFIG_ID2, CONFIG_MODIFIED),
                        Map.of(IN_FILE2, CONTENT2), Map.of(OUT_FILE1, CONTENT2)),
                Arguments.of("Output removed", Map.of(CONFIG_ID2, CONFIG_MODIFIED),
                        Map.of(IN_FILE2, CONTENT2), Map.of()));
    }

    @Test
    @DisplayName("Processing outputs")
    void outputProcessing() throws Exception {
        // inputs
        final List<File> inputFiles = createInputFiles(Map.of(IN_FILE1, CONTENT1));
        final Map<String, WritableObject> configMap = Map.of(CONFIG_ID1, CONFIG_ORIGINAL);
        // output files
        final File file1 = new File(workDir, OUT_FILE1);
        final File file2 = new File(workDir, OUT_FILE2);
        final File file3 = new File(workDir, OUT_FILE3);
        final File file4 = new File(workDir, OUT_FILE4);

        // first run: created file 1, file2, file3
        final ImmutableMap<String, ResourceState> outputStatesBefore = createOutputStates(
                Map.of(OUT_FILE1, CONTENT1, OUT_FILE2, CONTENT1, OUT_FILE3, CONTENT1), true);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());
        // execution circle
        final RebuildContext rebuildContext = new RebuildContext(workDir, buildContext);
        rebuildContext.setInputFiles(inputFiles);
        rebuildContext.setConfigurations(configMap);
        rebuildContext.setOutputFileStates(outputStatesBefore);
        rebuildContext.deleteObsoleteFiles();
        rebuildContext.persistState();
        // ensure output files exist
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());
        // verify buildContext refreshed on created files
        verify(buildContext, times(3)).refresh(fileCaptor1.capture());
        assertEquals(Set.of(file1.getPath(), file2.getPath(), file3.getPath()),
                fileCaptor1.getAllValues().stream().map(File::getPath).collect(Collectors.toSet()));

        // second run: file1 is obsolete, file2 remain same, file3 updated, file4 added
        final ImmutableMap<String, ResourceState> outputStatesAfter = createOutputStates(
                Map.of(OUT_FILE2, CONTENT1, OUT_FILE3, CONTENT2, OUT_FILE4, CONTENT2), false);
        assertTrue(file1.exists()); // remain from previous run
        assertTrue(file2.exists());
        assertTrue(file3.exists());
        assertTrue(file4.exists());
        // execution circle
        reset(buildContext);
        final RebuildContext nextRebuildContext = new RebuildContext(workDir, buildContext);
        nextRebuildContext.setInputFiles(inputFiles);
        nextRebuildContext.setConfigurations(configMap);
        nextRebuildContext.setOutputFileStates(outputStatesAfter);
        final int deleted = nextRebuildContext.deleteObsoleteFiles();
        nextRebuildContext.persistState();
        // ensure output files exist, obsolete file deleted
        assertEquals(1, deleted);
        assertFalse(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());
        assertTrue(file4.exists());
        // verify buildContext refreshed for deleted, updated and added files, not for one  remaining same
        verify(buildContext, times(3)).refresh(fileCaptor2.capture());
        assertEquals(Set.of(file1.getPath(), file3.getPath(), file4.getPath()),
                fileCaptor2.getAllValues().stream().map(File::getPath).collect(Collectors.toSet()));
    }

    private List<File> createInputFiles(final Map<String, String> nameContentMap) throws IOException {
        final List<File> files = new LinkedList<>();
        for (var entry : nameContentMap.entrySet()) {
            File file = new File(workDir, entry.getKey());
            Files.write(file.toPath(), entry.getValue().getBytes(StandardCharsets.UTF_8));
            files.add(file);
        }
        return files;
    }

    private ImmutableMap<String, ResourceState> createOutputStates(final Map<String, String> nameContentMap,
            final boolean removeObsolete) throws IOException {
        final ImmutableMap.Builder<String, ResourceState> mapBuilder = new ImmutableMap.Builder<>();
        for (String filename : ALL_OUTPUTS) {
            final File file = new File(workDir, filename);
            if (nameContentMap.containsKey(filename)) {
                // create files if requested
                Files.write(file.toPath(), nameContentMap.get(filename).getBytes(StandardCharsets.UTF_8));
                mapBuilder.put(file.getPath(), RebuildContextUtils.buildState(file));
            } else if (removeObsolete) {
                // remove extra output files
                file.delete();
            }
        }
        return mapBuilder.build();
    }
}
