/*
 * Copyright (c) 2022 PANTHEON.tech and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    private static final String CONTENT1 = "content-1";
    private static final String CONTENT2 = "content-2";
    private static final Set<String> ALL_OUTPUTS = Set.of(OUT_FILE1, OUT_FILE2);

    @Mock
    private BuildContext buildContext;

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
        final ImmutableMap<String, ResourceState> outputStates = createOutputStates(outputDataMap);
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

    private List<File> createInputFiles(final Map<String, String> nameContentMap) throws IOException {
        final List<File> files = new LinkedList<>();
        for (var entry : nameContentMap.entrySet()) {
            Path filePath = workDir.toPath().resolve(entry.getKey());
            Files.write(filePath, entry.getValue().getBytes(StandardCharsets.UTF_8));
            files.add(filePath.toFile());
        }
        return files;
    }

    private ImmutableMap<String, ResourceState> createOutputStates(final Map<String, String> nameContentMap)
            throws IOException {
        final ImmutableMap.Builder<String, ResourceState> mapBuilder = new ImmutableMap.Builder<>();
        for (String filename : ALL_OUTPUTS) {
            final Path filePath = workDir.toPath().resolve(filename);
            final File file = filePath.toFile();
            if (nameContentMap.containsKey(filename)) {
                Files.write(filePath, nameContentMap.get(filename).getBytes(StandardCharsets.UTF_8));
                mapBuilder.put(file.getPath(), RebuildContextUtils.buildState(file));
            } else {
                // remove extra output files bc rebuild context checks for prior output file existence
                file.delete();
            }
        }
        return mapBuilder.build();
    }
}
