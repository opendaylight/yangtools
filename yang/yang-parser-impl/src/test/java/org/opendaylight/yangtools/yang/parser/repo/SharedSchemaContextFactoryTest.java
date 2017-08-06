/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.Assert.assertNotNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.TextToASTTransformer;

public class SharedSchemaContextFactoryTest {

    private final SharedSchemaRepository repository = new SharedSchemaRepository("test");

    @Mock
    private SchemaSourceFilter filter;
    private SourceIdentifier s1;
    private SourceIdentifier s2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final YangTextSchemaSource source1 = YangTextSchemaSource.forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        final YangTextSchemaSource source2 = YangTextSchemaSource.forResource("/ietf/iana-timezones@2012-07-09.yang");
        s1 = RevisionSourceIdentifier.create("ietf-inet-types", "2010-09-24");
        s2 = RevisionSourceIdentifier.create("iana-timezones", "2012-07-09");

        final TextToASTTransformer transformer = TextToASTTransformer.create(repository, repository);
        repository.registerSchemaSourceListener(transformer);

        repository.registerSchemaSource(sourceIdentifier -> Futures.immediateCheckedFuture(source1),
            PotentialSchemaSource.create(s1, YangTextSchemaSource.class, 1));

        repository.registerSchemaSource(sourceIdentifier -> Futures.immediateCheckedFuture(source2),
            PotentialSchemaSource.create(s2, YangTextSchemaSource.class, 1));
    }

    @Test
    public void testCreateSchemaContextWithDuplicateRequiredSources() throws Exception {
        final SharedSchemaContextFactory sharedSchemaContextFactory = new SharedSchemaContextFactory(repository, filter);
        final ListenableFuture<SchemaContext> schemaContext =
                sharedSchemaContextFactory.createSchemaContext(Arrays.asList(s1, s1, s2));
        assertNotNull(schemaContext.get());
    }

    @Test
    public void testSourceRegisteredWithDifferentSI() throws Exception {
        final YangTextSchemaSource source1 = YangTextSchemaSource.forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        final YangTextSchemaSource source2 = YangTextSchemaSource.forResource("/ietf/iana-timezones@2012-07-09.yang");
        s1 = source1.getIdentifier();
        s2 = source2.getIdentifier();

        final SettableSchemaProvider<ASTSchemaSource> provider =
                SharedSchemaRepositoryTest.getImmediateYangSourceProviderFromResource(
                    "/no-revision/imported@2012-12-12.yang");
        provider.setResult();
        provider.register(repository);

        // Register the same provider under source id without revision
        final SourceIdentifier sIdWithoutRevision = RevisionSourceIdentifier.create(provider.getId().getName());
        repository.registerSchemaSource(provider, PotentialSchemaSource.create(
                sIdWithoutRevision, ASTSchemaSource.class, PotentialSchemaSource.Costs.IMMEDIATE.getValue()));

        final SharedSchemaContextFactory sharedSchemaContextFactory = new SharedSchemaContextFactory(repository, filter);
        final ListenableFuture<SchemaContext> schemaContext =
                sharedSchemaContextFactory.createSchemaContext(Arrays.asList(sIdWithoutRevision, provider.getId()));
        assertNotNull(schemaContext.get());
    }
}
