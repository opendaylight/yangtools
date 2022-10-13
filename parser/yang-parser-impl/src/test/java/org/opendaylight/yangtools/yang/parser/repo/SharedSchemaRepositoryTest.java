/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.EffectiveModelContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.IRSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public class SharedSchemaRepositoryTest {

    @Test
    public void testSourceWithAndWithoutRevision() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SourceIdentifier idNoRevision = loadAndRegisterSource(sharedSchemaRepository,
            "/no-revision/imported.yang");
        final SourceIdentifier id2 = loadAndRegisterSource(sharedSchemaRepository,
            "/no-revision/imported@2012-12-12.yang");

        ListenableFuture<IRSchemaSource> source = sharedSchemaRepository.getSchemaSource(idNoRevision,
            IRSchemaSource.class);
        assertEquals(idNoRevision, source.get().getIdentifier());
        source = sharedSchemaRepository.getSchemaSource(id2, IRSchemaSource.class);
        assertEquals(id2, source.get().getIdentifier());
    }

    private static SourceIdentifier loadAndRegisterSource(final SharedSchemaRepository sharedSchemaRepository,
            final String resourceName) throws Exception {
        final SettableSchemaProvider<IRSchemaSource> sourceProvider = getImmediateYangSourceProviderFromResource(
            resourceName);
        sourceProvider.setResult();
        final SourceIdentifier idNoRevision = sourceProvider.getId();
        sourceProvider.register(sharedSchemaRepository);
        return idNoRevision;
    }

    @Test
    public void testSimpleSchemaContext() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SettableSchemaProvider<IRSchemaSource> remoteInetTypesYang = getImmediateYangSourceProviderFromResource(
            "/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);
        final ListenableFuture<IRSchemaSource> registeredSourceFuture = sharedSchemaRepository.getSchemaSource(
            remoteInetTypesYang.getId(), IRSchemaSource.class);
        assertFalse(registeredSourceFuture.isDone());

        final EffectiveModelContextFactory fact = sharedSchemaRepository.createEffectiveModelContextFactory();
        final ListenableFuture<EffectiveModelContext> schemaContextFuture =
                fact.createEffectiveModelContext(remoteInetTypesYang.getId());

        assertFalse(schemaContextFuture.isDone());

        // Make source appear
        remoteInetTypesYang.setResult();
        assertEquals(remoteInetTypesYang.getSchemaSourceRepresentation(), registeredSourceFuture.get());

        // Verify schema created successfully
        assertTrue(schemaContextFuture.isDone());
        final SchemaContext firstSchemaContext = schemaContextFuture.get();
        assertSchemaContext(firstSchemaContext, 1);

        // Try same schema second time
        final ListenableFuture<EffectiveModelContext> secondSchemaFuture =
                sharedSchemaRepository.createEffectiveModelContextFactory().createEffectiveModelContext(
                    remoteInetTypesYang.getId());

        // Verify second schema created successfully immediately
        assertTrue(secondSchemaFuture.isDone());
        // Assert same context instance is returned from first and second attempt
        assertSame(firstSchemaContext, secondSchemaFuture.get());
    }

    @Test
    public void testTwoSchemaContextsSharingSource() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SettableSchemaProvider<IRSchemaSource> remoteInetTypesYang = getImmediateYangSourceProviderFromResource(
            "/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);
        remoteInetTypesYang.setResult();
        final SettableSchemaProvider<IRSchemaSource> remoteTopologyYang = getImmediateYangSourceProviderFromResource(
            "/ietf/network-topology@2013-10-21.yang");
        remoteTopologyYang.register(sharedSchemaRepository);
        remoteTopologyYang.setResult();
        final SettableSchemaProvider<IRSchemaSource> remoteModuleNoRevYang =
                getImmediateYangSourceProviderFromResource("/no-revision/module-without-revision.yang");
        remoteModuleNoRevYang.register(sharedSchemaRepository);

        final EffectiveModelContextFactory fact = sharedSchemaRepository.createEffectiveModelContextFactory();
        final ListenableFuture<EffectiveModelContext> inetAndTopologySchemaContextFuture = fact
                .createEffectiveModelContext(remoteInetTypesYang.getId(), remoteTopologyYang.getId());
        assertTrue(inetAndTopologySchemaContextFuture.isDone());
        assertSchemaContext(inetAndTopologySchemaContextFuture.get(), 2);

        final ListenableFuture<EffectiveModelContext> inetAndNoRevSchemaContextFuture =
                fact.createEffectiveModelContext(remoteInetTypesYang.getId(), remoteModuleNoRevYang.getId());
        assertFalse(inetAndNoRevSchemaContextFuture.isDone());

        remoteModuleNoRevYang.setResult();
        assertTrue(inetAndNoRevSchemaContextFuture.isDone());
        assertSchemaContext(inetAndNoRevSchemaContextFuture.get(), 2);
    }

    @Test
    public void testFailedSchemaContext() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SettableSchemaProvider<IRSchemaSource> remoteInetTypesYang = getImmediateYangSourceProviderFromResource(
            "/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);

        final EffectiveModelContextFactory fact = sharedSchemaRepository.createEffectiveModelContextFactory();

        // Make source appear
        final Throwable ex = new IllegalStateException("failed schema");
        remoteInetTypesYang.setException(ex);

        final ListenableFuture<EffectiveModelContext> schemaContextFuture = fact.createEffectiveModelContext(
            remoteInetTypesYang.getId());

        try {
            schemaContextFuture.get();
        } catch (final ExecutionException e) {
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

        final SettableSchemaProvider<IRSchemaSource> immediateInetTypesYang = spy(
            getImmediateYangSourceProviderFromResource("/ietf/ietf-inet-types@2010-09-24.yang"));
        immediateInetTypesYang.register(sharedSchemaRepository);
        immediateInetTypesYang.setResult();

        final SettableSchemaProvider<IRSchemaSource> remoteInetTypesYang = spy(
            getRemoteYangSourceProviderFromResource("/ietf/ietf-inet-types@2010-09-24.yang"));
        remoteInetTypesYang.register(sharedSchemaRepository);
        remoteInetTypesYang.setResult();

        final EffectiveModelContextFactory fact = sharedSchemaRepository.createEffectiveModelContextFactory();
        final ListenableFuture<EffectiveModelContext> schemaContextFuture =
                fact.createEffectiveModelContext(remoteInetTypesYang.getId());

        assertSchemaContext(schemaContextFuture.get(), 1);

        final SourceIdentifier id = immediateInetTypesYang.getId();
        verify(remoteInetTypesYang, times(0)).getSource(id);
        verify(immediateInetTypesYang).getSource(id);
    }

    private static void assertSchemaContext(final SchemaContext schemaContext, final int moduleSize) {
        assertNotNull(schemaContext);
        assertEquals(moduleSize, schemaContext.getModules().size());
    }

    static SettableSchemaProvider<IRSchemaSource> getRemoteYangSourceProviderFromResource(final String resourceName)
            throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createRemote(TextToIRTransformer.transformText(yangSource),
            IRSchemaSource.class);
    }

    static SettableSchemaProvider<IRSchemaSource> getImmediateYangSourceProviderFromResource(final String resourceName)
            throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createImmediate(TextToIRTransformer.transformText(yangSource),
            IRSchemaSource.class);
    }
}
