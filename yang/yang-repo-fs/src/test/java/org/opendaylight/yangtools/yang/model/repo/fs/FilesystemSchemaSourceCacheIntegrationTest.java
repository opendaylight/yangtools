/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.fs;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.util.concurrent.Futures;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.dagger.yang.parser.vanilla.DaggerVanillaYangParserComponent;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceListener;
import org.opendaylight.yangtools.yang.model.repo.spi.SourceInfoSchemaSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.StringYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.repo.SharedSchemaRepository;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

class FilesystemSchemaSourceCacheIntegrationTest {
    private static final YangParserFactory PARSER_FACTORY = DaggerVanillaYangParserComponent.create().parserFactory();

    @Test
    void testWithCacheStartup() throws IOException {
        final var sharedSchemaRepository = new SharedSchemaRepository(PARSER_FACTORY, "netconf-mounts");

        class CountingSchemaListener implements SchemaSourceListener {
            final ArrayList<PotentialSchemaSource<?>> registeredSources = new ArrayList<>();

            @Override
            public void schemaSourceEncountered(final SourceRepresentation source) {
                // no-op
            }

            @Override
            public void schemaSourceRegistered(final Iterable<PotentialSchemaSource<?>> sources) {
                sources.forEach(registeredSources::add);
            }

            @Override
            public void schemaSourceUnregistered(final PotentialSchemaSource<?> source) {
                // no-op
            }
        }

        final var tempDir = Files.createTempDirectory("with-cache-startup");

        final var listener = new CountingSchemaListener();
        sharedSchemaRepository.registerSchemaSourceListener(listener);

        Files.writeString(tempDir.resolve("test.yang"), "content-test");
        Files.writeString(tempDir.resolve("test@2012-12-12.yang"), "content-test-2012");
        Files.writeString(tempDir.resolve("test@2013-12-12.yang"), "content-test-2013");
        Files.writeString(tempDir.resolve("module@2010-12-12.yang"), "content-module-2010");

        final var cache = new FilesystemSchemaSourceCache<>(sharedSchemaRepository, YangTextSource.class, tempDir);
        sharedSchemaRepository.registerSchemaSourceListener(cache);

        assertEquals(4, listener.registeredSources.size());

        assertThat(listener.registeredSources.stream().map(PotentialSchemaSource::getSourceIdentifier).toList(),
            both(hasItem(new SourceIdentifier("test")))
                .and(hasItem(new SourceIdentifier("test", "2012-12-12")))
                .and(hasItem(new SourceIdentifier("test", "2013-12-12")))
                .and(hasItem(new SourceIdentifier("module", "2010-12-12"))));
    }

    @Test
    void testWithCacheRunning() throws Exception {
        final var sharedSchemaRepository = new SharedSchemaRepository(PARSER_FACTORY, "netconf-mounts");

        final var storageDir = Files.createTempDirectory("with-cache-running");

        final var cache = new FilesystemSchemaSourceCache<>(sharedSchemaRepository, YangTextSource.class, storageDir);
        sharedSchemaRepository.registerSchemaSourceListener(cache);

        final var runningId = new SourceIdentifier("running", "2012-12-12");

        sharedSchemaRepository.registerSchemaSource(sourceIdentifier -> FluentFutures.immediateFluentFuture(
            new StringYangTextSource(runningId, "running", null)),
            PotentialSchemaSource.create(runningId, YangTextSource.class,
                PotentialSchemaSource.Costs.REMOTE_IO.getValue()));

        final var transformer = SourceInfoSchemaSourceTransformer.ofYang(sharedSchemaRepository, sharedSchemaRepository,
            YangIRSourceModule.provideTextToIR());
        sharedSchemaRepository.registerSchemaSourceListener(transformer);

        // Request schema to make repository notify the cache
        final var schemaFuture = sharedSchemaRepository
                .createEffectiveModelContextFactory()
                .createEffectiveModelContext(runningId);

        final var cause = assertThrows(ExecutionException.class, () -> Futures.getDone(schemaFuture)).getCause();
        assertInstanceOf(MissingSchemaSourceException.class, cause);

        // Creation of schema context fails, since we do not provide regular sources, but we just want
        // to check cache
        final var cachedSchemas = Arrays.asList(storageDir.toFile().listFiles());
        assertEquals(1, cachedSchemas.size());
        assertEquals("running@2012-12-12.yang", cachedSchemas.getFirst().getName());
    }
}
