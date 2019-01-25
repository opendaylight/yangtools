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
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaListenerRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceListener;
import org.opendaylight.yangtools.yang.parser.repo.SharedSchemaRepository;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToASTTransformer;

public class SimpleModuleTest {
    private SharedSchemaRepository schemaRegistry;
    private SchemaContextFactory schemaContextFactory;
    private Set<SourceIdentifier> allTestSources;

    @Before
    public void init() {
        schemaRegistry = new SharedSchemaRepository("test");
        final TextToASTTransformer astTransformer = TextToASTTransformer.create(schemaRegistry, schemaRegistry);
        schemaRegistry.registerSchemaSourceListener(astTransformer);

        schemaContextFactory = schemaRegistry.createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);
        allTestSources = new HashSet<>();
        final SchemaListenerRegistration reg = schemaRegistry.registerSchemaSourceListener(new SchemaSourceListener() {

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
            public void schemaSourceEncountered(final SchemaSourceRepresentation source) {
                // NOOP
            }
        });
        reg.close();
    }

    @Test
    public void testGenerateAll() throws Exception {
        testSetOfModules(allTestSources);
    }

    private void testSetOfModules(final Collection<SourceIdentifier> source) throws Exception {
        final SchemaContext schemaContext = schemaContextFactory.createSchemaContext(source).get();
        final File outDir = new File("target/collection");
        outDir.mkdirs();
        for (final Module module : schemaContext.getModules()) {
            exportModule(module, outDir);
        }
    }

    private static File exportModule(final Module module, final File outDir)
            throws Exception {
        final File outFile = new File(outDir, YinExportUtils.wellFormedYinName(module.getName(), module.getRevision()));
        try (OutputStream output = new FileOutputStream(outFile)) {
            YinExportUtils.writeModuleAsYinText(module, output);
        }
        return outFile;
    }
}
