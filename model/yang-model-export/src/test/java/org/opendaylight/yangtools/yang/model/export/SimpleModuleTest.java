/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.dagger.yang.parser.vanilla.DaggerVanillaYangParserFactoryComponent;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.EffectiveModelContextFactory;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceListener;
import org.opendaylight.yangtools.yang.model.repo.spi.SourceInfoSchemaSourceTransformer;
import org.opendaylight.yangtools.yang.parser.repo.SharedSchemaRepository;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

class SimpleModuleTest {
    private SharedSchemaRepository schemaRegistry;
    private EffectiveModelContextFactory schemaContextFactory;
    private Set<SourceIdentifier> allTestSources;

    @BeforeEach
    void init() {
        schemaRegistry = new SharedSchemaRepository(
            DaggerVanillaYangParserFactoryComponent.create().parserFactory(), "test");
        final var astTransformer = SourceInfoSchemaSourceTransformer.ofYang(schemaRegistry, schemaRegistry,
            YangIRSourceModule.provideTextToIR());
        schemaRegistry.registerSchemaSourceListener(astTransformer);

        schemaContextFactory = schemaRegistry.createEffectiveModelContextFactory();
        allTestSources = new HashSet<>();
        try (var reg = schemaRegistry.registerSchemaSourceListener(
            new SchemaSourceListener() {
                @Override
                public void schemaSourceUnregistered(final PotentialSchemaSource<?> source) {
                    // NOOP
                }

                @Override
                public void schemaSourceRegistered(final Iterable<PotentialSchemaSource<?>> sources) {
                    for (var source : sources) {
                        allTestSources.add(source.getSourceIdentifier());
                    }
                }

                @Override
                public void schemaSourceEncountered(final SourceRepresentation source) {
                    // NOOP
                }
            })) {
            // Noop
        }
    }

    @Test
    void testGenerateAll() throws Exception {
        testSetOfModules(allTestSources);
    }

    private void testSetOfModules(final Set<SourceIdentifier> source) throws Exception {
        final var schemaContext = schemaContextFactory.createEffectiveModelContext(source).get();
        final var outDir = Path.of("target", "collection");
        Files.createDirectories(outDir);
        for (var module : schemaContext.getModules()) {
            exportModule(module, outDir);
        }
    }

    private static void exportModule(final Module module, final Path outDir) throws Exception {
        final var outFile = outDir.resolve(YinExportUtils.wellFormedYinName(module.getName(), module.getRevision()));
        try (var output = Files.newOutputStream(outFile)) {
            YinExportUtils.writeModuleAsYinText(module.asEffectiveStatement(), output);
        }
    }
}
