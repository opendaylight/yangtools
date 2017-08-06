/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.repo;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter.ALWAYS_ACCEPT;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.util.concurrent.CheckedFuture;
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
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceListener;
import org.opendaylight.yangtools.yang.model.repo.util.FilesystemSchemaSourceCache;
import org.opendaylight.yangtools.yang.parser.util.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.TextToASTTransformer;

public class SharedSchemaRepositoryTest {

    @Test
    public void testSourceWithAndWithoutRevision() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SourceIdentifier idNoRevision = loadAndRegisterSource(sharedSchemaRepository,
            "/no-revision/imported.yang");
        final SourceIdentifier id2 = loadAndRegisterSource(sharedSchemaRepository,
            "/no-revision/imported@2012-12-12.yang");

        ListenableFuture<ASTSchemaSource> source = sharedSchemaRepository.getSchemaSource(idNoRevision,
            ASTSchemaSource.class);
        assertEquals(idNoRevision, source.get().getIdentifier());
        source = sharedSchemaRepository.getSchemaSource(id2, ASTSchemaSource.class);
        assertEquals(id2, source.get().getIdentifier());
    }

    private static SourceIdentifier loadAndRegisterSource(final SharedSchemaRepository sharedSchemaRepository,
            final String resourceName) throws Exception {
        final SettableSchemaProvider<ASTSchemaSource> sourceProvider = getImmediateYangSourceProviderFromResource(
            resourceName);
        sourceProvider.setResult();
        final SourceIdentifier idNoRevision = sourceProvider.getId();
        sourceProvider.register(sharedSchemaRepository);
        return idNoRevision;
    }

    @Test
    public void testSimpleSchemaContext() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SettableSchemaProvider<ASTSchemaSource> remoteInetTypesYang = getImmediateYangSourceProviderFromResource(
            "/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);
        final ListenableFuture<ASTSchemaSource> registeredSourceFuture = sharedSchemaRepository.getSchemaSource(
            remoteInetTypesYang.getId(), ASTSchemaSource.class);
        assertFalse(registeredSourceFuture.isDone());

        final SchemaContextFactory fact = sharedSchemaRepository.createSchemaContextFactory(ALWAYS_ACCEPT);
        final ListenableFuture<SchemaContext> schemaContextFuture =
                fact.createSchemaContext(ImmutableList.of(remoteInetTypesYang.getId()));

        assertFalse(schemaContextFuture.isDone());

        // Make source appear
        remoteInetTypesYang.setResult();
        assertEquals(remoteInetTypesYang.getSchemaSourceRepresentation(), registeredSourceFuture.get());

        // Verify schema created successfully
        assertTrue(schemaContextFuture.isDone());
        final SchemaContext firstSchemaContext = schemaContextFuture.get();
        assertSchemaContext(firstSchemaContext, 1);

        // Try same schema second time
        final ListenableFuture<SchemaContext> secondSchemaFuture = sharedSchemaRepository
                .createSchemaContextFactory(ALWAYS_ACCEPT)
                .createSchemaContext(ImmutableList.of(remoteInetTypesYang.getId()));

        // Verify second schema created successfully immediately
        assertTrue(secondSchemaFuture.isDone());
        // Assert same context instance is returned from first and second attempt
        assertSame(firstSchemaContext, secondSchemaFuture.get());
    }

    @Test
    public void testTwoSchemaContextsSharingSource() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SettableSchemaProvider<ASTSchemaSource> remoteInetTypesYang = getImmediateYangSourceProviderFromResource(
            "/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);
        remoteInetTypesYang.setResult();
        final SettableSchemaProvider<ASTSchemaSource> remoteTopologyYang = getImmediateYangSourceProviderFromResource(
            "/ietf/network-topology@2013-10-21.yang");
        remoteTopologyYang.register(sharedSchemaRepository);
        remoteTopologyYang.setResult();
        final SettableSchemaProvider<ASTSchemaSource> remoteModuleNoRevYang = getImmediateYangSourceProviderFromResource(
            "/no-revision/module-without-revision.yang");
        remoteModuleNoRevYang.register(sharedSchemaRepository);

        final SchemaContextFactory fact = sharedSchemaRepository.createSchemaContextFactory(ALWAYS_ACCEPT);
        final ListenableFuture<SchemaContext> inetAndTopologySchemaContextFuture = fact
                .createSchemaContext(ImmutableList.of(remoteInetTypesYang.getId(), remoteTopologyYang.getId()));
        assertTrue(inetAndTopologySchemaContextFuture.isDone());
        assertSchemaContext(inetAndTopologySchemaContextFuture.get(), 2);

        final ListenableFuture<SchemaContext> inetAndNoRevSchemaContextFuture =
                fact.createSchemaContext(ImmutableList.of(remoteInetTypesYang.getId(), remoteModuleNoRevYang.getId()));
        assertFalse(inetAndNoRevSchemaContextFuture.isDone());

        remoteModuleNoRevYang.setResult();
        assertTrue(inetAndNoRevSchemaContextFuture.isDone());
        assertSchemaContext(inetAndNoRevSchemaContextFuture.get(), 2);
    }

    @Test
    public void testFailedSchemaContext() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SettableSchemaProvider<ASTSchemaSource> remoteInetTypesYang = getImmediateYangSourceProviderFromResource(
            "/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);

        final SchemaContextFactory fact = sharedSchemaRepository.createSchemaContextFactory(ALWAYS_ACCEPT);

        // Make source appear
        final Throwable ex = new IllegalStateException("failed schema");
        remoteInetTypesYang.setException(ex);

        final CheckedFuture<SchemaContext, SchemaResolutionException> schemaContextFuture =
                fact.createSchemaContext(ImmutableList.of(remoteInetTypesYang.getId()));

        try {
            schemaContextFuture.checkedGet();
        } catch (final SchemaResolutionException e) {
            assertNotNull(e.getCause());
            assertNotNull(e.getCause().getCause());
            assertSame(ex, e.getCause().getCause());
            return;
        }

        fail("Schema context creation should have failed");
    }

    @Test
    public void testDifferentCosts() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SettableSchemaProvider<ASTSchemaSource> immediateInetTypesYang = spy(
            getImmediateYangSourceProviderFromResource("/ietf/ietf-inet-types@2010-09-24.yang"));
        immediateInetTypesYang.register(sharedSchemaRepository);
        immediateInetTypesYang.setResult();

        final SettableSchemaProvider<ASTSchemaSource> remoteInetTypesYang = spy(
            getRemoteYangSourceProviderFromResource("/ietf/ietf-inet-types@2010-09-24.yang"));
        remoteInetTypesYang.register(sharedSchemaRepository);
        remoteInetTypesYang.setResult();

        final SchemaContextFactory fact = sharedSchemaRepository.createSchemaContextFactory(ALWAYS_ACCEPT);

        final ListenableFuture<SchemaContext> schemaContextFuture =
                fact.createSchemaContext(ImmutableList.of(remoteInetTypesYang.getId()));

        assertSchemaContext(schemaContextFuture.get(), 1);

        final SourceIdentifier id = immediateInetTypesYang.getId();
        verify(remoteInetTypesYang, times(0)).getSource(id);
        verify(immediateInetTypesYang).getSource(id);
    }

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

        final File storageDir = Files.createTempDir();

        final CountingSchemaListener listener = new CountingSchemaListener();
        sharedSchemaRepository.registerSchemaSourceListener(listener);

        final File test = new File(storageDir, "test.yang");
        Files.asCharSink(test, StandardCharsets.UTF_8).write("content-test");

        final File test2 = new File(storageDir, "test@2012-12-12.yang");
        Files.asCharSink(test2, StandardCharsets.UTF_8).write("content-test-2012");

        final File test3 = new File(storageDir, "test@2013-12-12.yang");
        Files.asCharSink(test3, StandardCharsets.UTF_8).write("content-test-2013");

        final File test4 = new File(storageDir, "module@2010-12-12.yang");
        Files.asCharSink(test4, StandardCharsets.UTF_8).write("content-module-2010");

        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache = new FilesystemSchemaSourceCache<>(
                sharedSchemaRepository, YangTextSchemaSource.class, storageDir);
        sharedSchemaRepository.registerSchemaSourceListener(cache);

        assertEquals(4, listener.registeredSources.size());

        final Function<PotentialSchemaSource<?>, SourceIdentifier> potSourceToSID =
                PotentialSchemaSource::getSourceIdentifier;
        assertThat(Collections2.transform(listener.registeredSources, potSourceToSID),
                both(hasItem(RevisionSourceIdentifier.create("test", Optional.absent())))
                        .and(hasItem(RevisionSourceIdentifier.create("test", Optional.of("2012-12-12"))))
                        .and(hasItem(RevisionSourceIdentifier.create("test", Optional.of("2013-12-12"))))
                        .and(hasItem(RevisionSourceIdentifier.create("module", Optional.of("2010-12-12"))))
        );
    }

    @Test
    public void testWithCacheRunning() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final File storageDir = Files.createTempDir();

        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache = new FilesystemSchemaSourceCache<>(
                sharedSchemaRepository, YangTextSchemaSource.class, storageDir);
        sharedSchemaRepository.registerSchemaSourceListener(cache);

        final SourceIdentifier runningId = RevisionSourceIdentifier.create("running", Optional.of("2012-12-12"));

        sharedSchemaRepository.registerSchemaSource(sourceIdentifier -> Futures.immediateCheckedFuture(
            new YangTextSchemaSource(runningId) {
                @Override
                protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
                    return toStringHelper;
                }

                @Override
                public InputStream openStream() throws IOException {
                    return new ByteArrayInputStream("running".getBytes(StandardCharsets.UTF_8));
                }
        }), PotentialSchemaSource.create(runningId, YangTextSchemaSource.class,
            PotentialSchemaSource.Costs.REMOTE_IO.getValue()));

        final TextToASTTransformer transformer = TextToASTTransformer.create(sharedSchemaRepository,
            sharedSchemaRepository);
        sharedSchemaRepository.registerSchemaSourceListener(transformer);

        // Request schema to make repository notify the cache
        final ListenableFuture<SchemaContext> schemaFuture = sharedSchemaRepository
                .createSchemaContextFactory(ALWAYS_ACCEPT).createSchemaContext(ImmutableList.of(runningId));
        Futures.addCallback(schemaFuture, new FutureCallback<SchemaContext>() {
            @Override
            public void onSuccess(final SchemaContext result) {
                fail("Creation of schema context should fail from non-regular sources");
            }

            @Override
            public void onFailure(@Nonnull final Throwable t) {
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

    private static void assertSchemaContext(final SchemaContext schemaContext, final int moduleSize) {
        assertNotNull(schemaContext);
        assertEquals(moduleSize, schemaContext.getModules().size());
    }

    static SettableSchemaProvider<ASTSchemaSource> getRemoteYangSourceProviderFromResource(final String resourceName)
            throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createRemote(TextToASTTransformer.transformText(yangSource),
            ASTSchemaSource.class);
    }

    static SettableSchemaProvider<ASTSchemaSource> getImmediateYangSourceProviderFromResource(final String resourceName)
            throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createImmediate(TextToASTTransformer.transformText(yangSource),
            ASTSchemaSource.class);
    }
}
