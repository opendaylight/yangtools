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

import com.google.common.io.Resources;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class RebuildContextTest {

    private static final String CONFIG_ID1 = "config-id-1";
    private static final String CONFIG_ID2 = "config-id-2";
    private static final WritableObject CONFIG_ORIGINAL = out -> out.writeUTF("config-original");
    private static final WritableObject CONFIG_MODIFIED = out -> out.writeUTF("config1-modified");

    private static final File ORIGINAL_DIR = resourceToPath("/rebuild-context/original");
    private static final File WHITESPACES_DIR = resourceToPath("/rebuild-context/whitespaces");
    private static final File MODIFIED_DIR = resourceToPath("/rebuild-context/modified");
    private static final String MODULE = "foo.yang";
    private static final String SUBMODULE = "foo-submodule.yang";
    private static final String AUGMENTATION = "bar.yang";

    private static final String FIRST_RUN = "First run";


    @TempDir
    private static File workDir;
    private File persistenceFile;

    @BeforeEach
    void setup() {
        persistenceFile = new File(workDir, RebuildContext.PERSISTENCE_FILE_NAME);
    }

    @ParameterizedTest(name = "Update configuration: {0}")
    @MethodSource("configurationContextUpdateArgs")
    void configurationContextUpdate(final String testDesc, final Map<String, WritableObject> configMap,
            final Map<String, Boolean> isChangedMap) {

        if (FIRST_RUN.equals(testDesc)) {
            // ensure there is no persistence file on first run
            persistenceFile.delete();
        } else {
            assertTrue(persistenceFile.exists());
        }

        final RebuildContext rebuildContext = new RebuildContext(workDir);
        rebuildContext.setConfigurations(configMap);

        // validate expected state of context on per config basis
        isChangedMap.forEach((configId, isChanged) ->
                assertEquals(isChanged, rebuildContext.isConfigurationContextChanged(configId)));

        // persist context; ensure state data is saved
        rebuildContext.persist();
        assertTrue(persistenceFile.exists());

        // next build case: ensure context is not changed for same inputs
        final RebuildContext nextRebuildContext = new RebuildContext(workDir);
        nextRebuildContext.setConfigurations(configMap);
        isChangedMap.keySet().forEach(
                configId -> assertFalse(nextRebuildContext.isConfigurationContextChanged(configId)));

    }

    private static Stream<Arguments> configurationContextUpdateArgs() {
        // (test case description, input configurations map, expected states on per config id)
        return Stream.of(
                Arguments.of(FIRST_RUN,
                        Map.of(CONFIG_ID1, CONFIG_ORIGINAL),
                        Map.of(CONFIG_ID1, true)),
                Arguments.of("Config 1 modified",
                        Map.of(CONFIG_ID1, CONFIG_MODIFIED),
                        Map.of(CONFIG_ID1, true)),
                Arguments.of("Config 2 added",
                        Map.of(CONFIG_ID1, CONFIG_MODIFIED, CONFIG_ID2, CONFIG_ORIGINAL),
                        Map.of(CONFIG_ID1, false, CONFIG_ID2, true)),
                Arguments.of("Config 2 modified",
                        Map.of(CONFIG_ID1, CONFIG_MODIFIED, CONFIG_ID2, CONFIG_MODIFIED),
                        Map.of(CONFIG_ID1, false, CONFIG_ID2, true))
        );
    }

    @ParameterizedTest(name = "Update EffectiveModelContext: {0}")
    @MethodSource("effectiveModelContextUpdateArgs")
    void effectiveModelContextUpdate(final String testDesc, final boolean isChanged, final List<File> yangFiles)
            throws Exception {

        if (FIRST_RUN.equals(testDesc)) {
            // ensure there is no persistence file on first run
            persistenceFile.delete();
        } else {
            assertTrue(persistenceFile.exists());
        }

        final RebuildContext rebuildContext = new RebuildContext(workDir);

        // set parsed modules for hashcode comparison
        final List<Module> projectModules = getParsedProjectModules(yangFiles);
        rebuildContext.setModules(projectModules);

        // validate expected state of context
        assertEquals(isChanged, rebuildContext.isModuleContextChanged());

        // persist context; ensure state data is saved
        rebuildContext.persist();
        assertTrue(persistenceFile.exists());

        // next build case: ensure context is not changed
        final RebuildContext nextRebuildContext = new RebuildContext(workDir);
        nextRebuildContext.setModules(projectModules);
        assertFalse(nextRebuildContext.isModuleContextChanged());
    }

    private static Stream<Arguments> effectiveModelContextUpdateArgs() {
        // (test case descriptor, expected context change, list of yang files)
        return Stream.of(
                Arguments.of(FIRST_RUN, true,
                        List.of(new File(ORIGINAL_DIR, MODULE),
                                new File(ORIGINAL_DIR, SUBMODULE),
                                new File(ORIGINAL_DIR, AUGMENTATION))),
                Arguments.of("Module file whitespaces only update", false,
                        List.of(new File(WHITESPACES_DIR, MODULE),
                                new File(ORIGINAL_DIR, SUBMODULE),
                                new File(ORIGINAL_DIR, AUGMENTATION))),
                Arguments.of("Module file content update", true,
                        List.of(new File(MODIFIED_DIR, MODULE),
                                new File(ORIGINAL_DIR, SUBMODULE),
                                new File(ORIGINAL_DIR, AUGMENTATION))),
                Arguments.of("Submodule file whitespaces only update", false,
                        List.of(new File(MODIFIED_DIR, MODULE),
                                new File(WHITESPACES_DIR, SUBMODULE),
                                new File(ORIGINAL_DIR, AUGMENTATION))),
                Arguments.of("Submodule file content update", true,
                        List.of(new File(MODIFIED_DIR, MODULE),
                                new File(MODIFIED_DIR, SUBMODULE),
                                new File(ORIGINAL_DIR, AUGMENTATION))),
                Arguments.of("Augmentation module whitespaces only update", false,
                        List.of(new File(MODIFIED_DIR, MODULE),
                                new File(MODIFIED_DIR, SUBMODULE),
                                new File(WHITESPACES_DIR, AUGMENTATION))),
                Arguments.of("Augmentation module content update", true,
                        List.of(new File(MODIFIED_DIR, MODULE),
                                new File(MODIFIED_DIR, SUBMODULE),
                                new File(MODIFIED_DIR, AUGMENTATION)))
        );
    }

    private static List<Module> getParsedProjectModules(List<File> yangFiles) throws Exception {
        final List<YangTextSchemaSource> yangSources = toTextSchemaSources(yangFiles);
        final EffectiveModelContext modelContext = YangParserTestUtils.parseYangFiles(yangFiles);
        final Set<SourceIdentifier> projectIdentifiers =
                yangSources.stream().map(YangTextSchemaSource::getIdentifier).collect(Collectors.toSet());

        return modelContext.getModules().stream()
                .filter(m -> projectIdentifiers.contains(Util.moduleToIdentifier(m)))
                .collect(Collectors.toList());
    }

    private static List<YangTextSchemaSource> toTextSchemaSources(final Collection<File> files) throws Exception {
        final List<YangTextSchemaSource> result = new LinkedList<>();
        for (File file : files) {
            final YangTextSchemaSource source = YangTextSchemaSource.forPath(file.toPath());
            final YangTextSchemaSource delegate = YangTextSchemaSource.delegateForByteSource(
                    TextToIRTransformer.transformText(source).getIdentifier(), source);
            result.add(delegate);
        }
        return result;
    }

    private static File resourceToPath(String resourceName) {
        try {
            return new File(Resources.getResource(RebuildContextTest.class, resourceName).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

}
