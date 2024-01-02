/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.EffectiveModelContextFactory;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceListener;
import org.opendaylight.yangtools.yang.parser.repo.SharedSchemaRepository;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public class SimpleModuleTest {
    private SharedSchemaRepository schemaRegistry;
    private EffectiveModelContextFactory schemaContextFactory;
    private Set<SourceIdentifier> allTestSources;

    @BeforeEach
    public void init() {
        schemaRegistry = new SharedSchemaRepository("test");
        final TextToIRTransformer astTransformer = TextToIRTransformer.create(schemaRegistry, schemaRegistry);
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
                    for (final PotentialSchemaSource<?> source : sources) {
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
    public void testGenerateAll() throws Exception {
        testSetOfModules(allTestSources);
    }

    private void testSetOfModules(final Set<SourceIdentifier> source) throws Exception {
        final var schemaContext = schemaContextFactory.createEffectiveModelContext(source).get();
        final var outDir = new File("target/collection");
        outDir.mkdirs();
        for (final Module module : schemaContext.getModules()) {
            exportModule(module, outDir);
        }
    }

    private static File exportModule(final Module module, final File outDir)
            throws Exception {
        final var outFile = new File(outDir, YinExportUtils.wellFormedYinName(module.getName(), module.getRevision()));
        try (var output = new FileOutputStream(outFile)) {
            YinExportUtils.writeModuleAsYinText(module.asEffectiveStatement(), output);
        }
        return outFile;
    }
}
