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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFluentFuture;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceListener;
import org.opendaylight.yangtools.yang.parser.repo.SharedSchemaRepository;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public class FilesystemSchemaSourceCacheIntegrationTest {
    @Test
    public void testWithCacheStartup() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        class CountingSchemaListener implements SchemaSourceListener {
            List<PotentialSchemaSource<?>> registeredSources = new ArrayList<>();

            @Override
            public void schemaSourceEncountered(final SchemaSourceRepresentation source) {
            }

            @Override
            public void schemaSourceRegistered(final Iterable<PotentialSchemaSource<?>> sources) {
                for (final PotentialSchemaSource<?> source : sources) {
                    registeredSources.add(source);
                }
            }

            @Override
            public void schemaSourceUnregistered(final PotentialSchemaSource<?> source) {
            }
        }

        final File tempDir = Files.createTempDir();

        final CountingSchemaListener listener = new CountingSchemaListener();
        sharedSchemaRepository.registerSchemaSourceListener(listener);

        final File test = new File(tempDir, "test.yang");
        Files.asCharSink(test, StandardCharsets.UTF_8).write("content-test");

        final File test2 = new File(tempDir, "test@2012-12-12.yang");
        Files.asCharSink(test2, StandardCharsets.UTF_8).write("content-test-2012");

        final File test3 = new File(tempDir, "test@2013-12-12.yang");
        Files.asCharSink(test3, StandardCharsets.UTF_8).write("content-test-2013");

        final File test4 = new File(tempDir, "module@2010-12-12.yang");
        Files.asCharSink(test4, StandardCharsets.UTF_8).write("content-module-2010");

        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache = new FilesystemSchemaSourceCache<>(
                sharedSchemaRepository, YangTextSchemaSource.class, tempDir);
        sharedSchemaRepository.registerSchemaSourceListener(cache);

        assertEquals(4, listener.registeredSources.size());

        assertThat(Lists.transform(listener.registeredSources, PotentialSchemaSource::getSourceIdentifier),
                both(hasItem(new SourceIdentifier("test")))
                        .and(hasItem(new SourceIdentifier("test", "2012-12-12")))
                        .and(hasItem(new SourceIdentifier("test", "2013-12-12")))
                        .and(hasItem(new SourceIdentifier("module", "2010-12-12")))
        );
    }

    @Test
    public void testWithCacheRunning() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final File storageDir = Files.createTempDir();

        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache = new FilesystemSchemaSourceCache<>(
                sharedSchemaRepository, YangTextSchemaSource.class, storageDir);
        sharedSchemaRepository.registerSchemaSourceListener(cache);

        final SourceIdentifier runningId = new SourceIdentifier("running", "2012-12-12");

        sharedSchemaRepository.registerSchemaSource(sourceIdentifier -> immediateFluentFuture(
            new YangTextSchemaSource(runningId) {
                @Override
                protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
                    return toStringHelper;
                }

                @Override
                public InputStream openStream() throws IOException {
                    return new ByteArrayInputStream("running".getBytes(StandardCharsets.UTF_8));
                }

                @Override
                public Optional<String> getSymbolicName() {
                    return Optional.empty();
                }
            }), PotentialSchemaSource.create(runningId, YangTextSchemaSource.class,
                PotentialSchemaSource.Costs.REMOTE_IO.getValue()));

        final TextToIRTransformer transformer = TextToIRTransformer.create(sharedSchemaRepository,
            sharedSchemaRepository);
        sharedSchemaRepository.registerSchemaSourceListener(transformer);

        // Request schema to make repository notify the cache
        final ListenableFuture<EffectiveModelContext> schemaFuture = sharedSchemaRepository
                .createEffectiveModelContextFactory()
                .createEffectiveModelContext(runningId);
        Futures.addCallback(schemaFuture, new FutureCallback<SchemaContext>() {
            @Override
            public void onSuccess(final SchemaContext result) {
                fail("Creation of schema context should fail from non-regular sources");
            }

            @Override
            public void onFailure(final Throwable cause) {
                // Creation of schema context fails, since we do not provide regular sources, but we just want
                // to check cache
                final List<File> cachedSchemas = Arrays.asList(storageDir.listFiles());
                assertEquals(1, cachedSchemas.size());
                assertEquals(Files.getNameWithoutExtension(cachedSchemas.get(0).getName()), "running@2012-12-12");
            }
        }, MoreExecutors.directExecutor());

        try {
            schemaFuture.get();
        } catch (final ExecutionException e) {
            assertNotNull(e.getCause());
            assertEquals(MissingSchemaSourceException.class, e.getCause().getClass());
            return;
        }

        fail("Creation of schema context should fail from non-regular sources");
    }
}
