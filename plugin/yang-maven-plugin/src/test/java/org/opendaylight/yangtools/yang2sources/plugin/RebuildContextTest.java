/*
 * Copyright (c) 2022 PANTHEON.tech and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.io.Resources;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RebuildContextTest {

    private static final File ORIGINAL_DIR = resourceToPath("/rebuild-context/original");
    private static final File MODIFIED_DIR = resourceToPath("/rebuild-context/modified");
    private static final String MODULE = "foo.yang";
    private static final String SUBMODULE = "foo-submodule.yang";
    private static final String AUGMENTATION = "bar.yang";
    private static final String FIRST_RUN = "First run";
    private static final QNameModule MODULE_QNAME =
            QNameModule.create(XMLNamespace.of("urn:opendaylight:test:foo"), Revision.of("2022-02-22"));

    @TempDir
    private static File workDir;

    private RebuildContext rebuildContext;

    @BeforeEach
    void setup() {
        rebuildContext = new RebuildContext(workDir);
    }

    @ParameterizedTest(name = "Rebuild Context test: {0}")
    @MethodSource("rebuildContextArgs")
    void rebuildContextIteration(final String testDesc, final List<File> yangFiles) throws Exception {

        // check if persistence file exists before sources are checked
        final File persistenceFile = rebuildContext.getPersistenceFile();
        assertNotNull(persistenceFile);
        if (FIRST_RUN.equals(testDesc)) {
            assertFalse(persistenceFile.exists());
        } else {
            assertTrue(persistenceFile.exists());
        }

        // updating resource states: comparing persisted hashes with newly calculated
        final List<YangTextSchemaSource> yangSources = toTextSchemaSources(yangFiles);
        rebuildContext.updateSourceStates(yangSources);

        // updating cross-file dependencies using effective model context data
        final List<Module> projectModules = getProjectModules(yangFiles, yangSources);
        rebuildContext.updateDependencies(projectModules);

        // ensure primary module is marked as modified (eligible for processing)
        final Module module = projectModules.stream()
                .filter(m -> MODULE_QNAME.equals(m.getQNameModule())).findFirst().orElseThrow();
        assertTrue(rebuildContext.isModified(module));

        // persist context; ensure state data is saved
        rebuildContext.persist();
        assertTrue(persistenceFile.exists());

        // next build case: ensure module is no longer marked as modified
        final RebuildContext nextRebuildContext = new RebuildContext(workDir);
        nextRebuildContext.updateSourceStates(yangSources);
        nextRebuildContext.updateDependencies(projectModules);
        assertFalse(nextRebuildContext.isModified(module));
    }

    private static List<Module> getProjectModules(List<File> yangFiles, List<YangTextSchemaSource> yangSources) {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangFiles(yangFiles);
        final Set<SourceIdentifier> projectIdentifiers =
                yangSources.stream().map(YangTextSchemaSource::getIdentifier).collect(Collectors.toSet());
        return schemaContext.getModules().stream()
                .filter(m -> projectIdentifiers.contains(Util.moduleToIdentifier(m)))
                .collect(Collectors.toList());
    }

    private static Stream<Arguments> rebuildContextArgs() {
        // build iteration descriptor, list of yang files
        return Stream.of(
                Arguments.of(
                        FIRST_RUN, List.of(
                                new File(ORIGINAL_DIR, MODULE),
                                new File(ORIGINAL_DIR, SUBMODULE),
                                new File(ORIGINAL_DIR, AUGMENTATION))),
                Arguments.of(
                        "Module file update", List.of(
                                new File(MODIFIED_DIR, MODULE),
                                new File(ORIGINAL_DIR, SUBMODULE),
                                new File(ORIGINAL_DIR, AUGMENTATION))),
                Arguments.of(
                        "Submodule file update", List.of(
                                new File(MODIFIED_DIR, MODULE),
                                new File(MODIFIED_DIR, SUBMODULE),
                                new File(ORIGINAL_DIR, AUGMENTATION))),
                Arguments.of(
                        "Augmentation module update", List.of(
                                new File(MODIFIED_DIR, MODULE),
                                new File(MODIFIED_DIR, SUBMODULE),
                                new File(MODIFIED_DIR, AUGMENTATION)))
        );
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
