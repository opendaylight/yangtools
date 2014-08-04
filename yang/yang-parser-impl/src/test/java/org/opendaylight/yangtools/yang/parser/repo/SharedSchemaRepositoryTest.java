/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.repo;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.util.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.TextToASTTransformer;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;

public class SharedSchemaRepositoryTest {

    @Test
    public void testSimpleSchemaContext() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SettableSchemaProvider<ASTSchemaSource> remoteInetTypesYang = getImmediateYangSourceProviderFromResource("/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);
        final CheckedFuture<ASTSchemaSource, SchemaSourceException> registeredSourceFuture = sharedSchemaRepository.getSchemaSource(remoteInetTypesYang.getId(), ASTSchemaSource.class);
        assertFalse(registeredSourceFuture.isDone());

        final SchemaContextFactory fact = sharedSchemaRepository.createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);

        final CheckedFuture<SchemaContext, SchemaResolutionException> schemaContextFuture
                = fact.createSchemaContext(Lists.newArrayList(remoteInetTypesYang.getId()));

        assertFalse(schemaContextFuture.isDone());

        // Make source appear
        remoteInetTypesYang.setResult();
        assertEquals(remoteInetTypesYang.getSchemaSourceRepresentation(), registeredSourceFuture.get());

        // Verify schema created successfully
        assertTrue(schemaContextFuture.isDone());
        final SchemaContext firstSchemaContext = schemaContextFuture.checkedGet();
        assertSchemaContext(firstSchemaContext, 1);

        // Try same schema second time
        final CheckedFuture<SchemaContext, SchemaResolutionException> secondSchemaFuture =
                sharedSchemaRepository.createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT)
                        .createSchemaContext(Lists.newArrayList(remoteInetTypesYang.getId()));

        // Verify second schema created successfully immediately
        assertTrue(secondSchemaFuture.isDone());
        // Assert same context instance is returned from first and second attempt
        assertSame(firstSchemaContext, secondSchemaFuture.checkedGet());
    }

    @Test
    public void testTwoSchemaContextsSharingSource() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SettableSchemaProvider<ASTSchemaSource> remoteInetTypesYang = getImmediateYangSourceProviderFromResource("/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);
        remoteInetTypesYang.setResult();
        final SettableSchemaProvider<ASTSchemaSource> remoteTopologyYang = getImmediateYangSourceProviderFromResource("/ietf/network-topology@2013-10-21.yang");
        remoteTopologyYang.register(sharedSchemaRepository);
        remoteTopologyYang.setResult();
        final SettableSchemaProvider<ASTSchemaSource> remoteTest1Yang = getImmediateYangSourceProviderFromResource("/context-test/test1.yang");
        remoteTest1Yang.register(sharedSchemaRepository);

        final SchemaContextFactory fact = sharedSchemaRepository.createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);

        final CheckedFuture<SchemaContext, SchemaResolutionException> inetAndTopologySchemaContextFuture
                = fact.createSchemaContext(Lists.newArrayList(remoteInetTypesYang.getId(), remoteTopologyYang.getId()));
        assertTrue(inetAndTopologySchemaContextFuture.isDone());
        assertSchemaContext(inetAndTopologySchemaContextFuture.checkedGet(), 2);

        final CheckedFuture<SchemaContext, SchemaResolutionException> inetAndTest1SchemaContextFuture
                = fact.createSchemaContext(Lists.newArrayList(remoteInetTypesYang.getId(), remoteTest1Yang.getId()));
        assertFalse(inetAndTest1SchemaContextFuture.isDone());

        remoteTest1Yang.setResult();
        assertTrue(inetAndTest1SchemaContextFuture.isDone());
        assertSchemaContext(inetAndTest1SchemaContextFuture.checkedGet(), 2);
    }

    @Test
    public void testFailedSchemaContext() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SettableSchemaProvider<ASTSchemaSource> remoteInetTypesYang = getImmediateYangSourceProviderFromResource("/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);

        final SchemaContextFactory fact = sharedSchemaRepository.createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);

        // Make source appear
        final Throwable ex = new IllegalStateException("failed schema");
        remoteInetTypesYang.setException(ex);

        final CheckedFuture<SchemaContext, SchemaResolutionException> schemaContextFuture
                = fact.createSchemaContext(Lists.newArrayList(remoteInetTypesYang.getId()));

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

    // TODO
    @Ignore("Costs are not considered when getting sources")
    @Test
    public void testDifferentCosts() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final SettableSchemaProvider<ASTSchemaSource> immediateInetTypesYang = spy(getImmediateYangSourceProviderFromResource("/ietf/ietf-inet-types@2010-09-24.yang"));
        immediateInetTypesYang.register(sharedSchemaRepository);
        immediateInetTypesYang.setResult();

        final SettableSchemaProvider<ASTSchemaSource> remoteInetTypesYang = spy(getRemoteYangSourceProviderFromResource("/ietf/ietf-inet-types@2010-09-24.yang"));
        remoteInetTypesYang.register(sharedSchemaRepository);
        remoteInetTypesYang.setResult();

        final SchemaContextFactory fact = sharedSchemaRepository.createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);

        final CheckedFuture<SchemaContext, SchemaResolutionException> schemaContextFuture
                = fact.createSchemaContext(Lists.newArrayList(remoteInetTypesYang.getId()));

        assertSchemaContext(schemaContextFuture.checkedGet(), 1);

        final SourceIdentifier id = immediateInetTypesYang.getId();
        verify(remoteInetTypesYang, times(0)).getSource(id);
        verify(immediateInetTypesYang).getSource(id);
    }

    private void assertSchemaContext(final SchemaContext schemaContext, final int moduleSize) {
        assertNotNull(schemaContext);
        assertEquals(moduleSize, schemaContext.getModules().size());
    }

    private SettableSchemaProvider<ASTSchemaSource> getRemoteYangSourceProviderFromResource(final String resourceName) throws Exception {
        final ResourceYangSource yangSource = new ResourceYangSource(resourceName);
        final CheckedFuture<ASTSchemaSource, SchemaSourceException> aSTSchemaSource = TextToASTTransformer.TRANSFORMATION.apply(yangSource);
        return SettableSchemaProvider.createRemote(aSTSchemaSource.get(), ASTSchemaSource.class);
    }

    private SettableSchemaProvider<ASTSchemaSource> getImmediateYangSourceProviderFromResource(final String resourceName) throws Exception {
        final ResourceYangSource yangSource = new ResourceYangSource(resourceName);
        final CheckedFuture<ASTSchemaSource, SchemaSourceException> aSTSchemaSource = TextToASTTransformer.TRANSFORMATION.apply(yangSource);
        return SettableSchemaProvider.createImmediate(aSTSchemaSource.get(), ASTSchemaSource.class);
    }

}
