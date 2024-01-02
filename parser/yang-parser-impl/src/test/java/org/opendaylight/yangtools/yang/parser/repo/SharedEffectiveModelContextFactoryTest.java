/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFailedFluentFuture;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFluentFuture;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.ir.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public class SharedEffectiveModelContextFactoryTest {
    private final SharedSchemaRepository repository = new SharedSchemaRepository("test");
    private final SchemaContextFactoryConfiguration config = SchemaContextFactoryConfiguration.getDefault();

    private SourceIdentifier s1;
    private SourceIdentifier s2;

    @BeforeEach
    public void setUp() {
        final var source1 = YangTextSource.forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        final var source2 = YangTextSource.forResource("/ietf/iana-timezones@2012-07-09.yang");
        s1 = new SourceIdentifier("ietf-inet-types", "2010-09-24");
        s2 = new SourceIdentifier("iana-timezones", "2012-07-09");

        final var transformer = TextToIRTransformer.create(repository, repository);
        repository.registerSchemaSourceListener(transformer);

        repository.registerSchemaSource(sourceIdentifier -> immediateFluentFuture(source1),
            PotentialSchemaSource.create(s1, YangTextSource.class, 1));

        repository.registerSchemaSource(sourceIdentifier -> immediateFluentFuture(source2),
            PotentialSchemaSource.create(s2, YangTextSource.class, 1));
    }

    @Test
    public void testCreateSchemaContextWithDuplicateRequiredSources() throws Exception {
        final var sharedSchemaContextFactory = new SharedEffectiveModelContextFactory(repository, config);
        final var schemaContext = sharedSchemaContextFactory.createEffectiveModelContext(s1, s1, s2);
        assertNotNull(schemaContext.get());
    }

    @Test
    public void testSourceRegisteredWithDifferentSI() throws Exception {
        final var source1 = YangTextSource.forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        final var source2 = YangTextSource.forResource("/ietf/iana-timezones@2012-07-09.yang");
        s1 = source1.sourceId();
        s2 = source2.sourceId();

        final var provider = SharedSchemaRepositoryTest.getImmediateYangSourceProviderFromResource(
            "/no-revision/imported@2012-12-12.yang");
        provider.setResult();
        provider.register(repository);

        // Register the same provider under source id without revision
        final var sIdWithoutRevision = new SourceIdentifier(provider.getId().name());
        repository.registerSchemaSource(provider, PotentialSchemaSource.create(sIdWithoutRevision,
            YangIRSchemaSource.class, PotentialSchemaSource.Costs.IMMEDIATE.getValue()));

        final var sharedSchemaContextFactory = new SharedEffectiveModelContextFactory(repository, config);
        final var schemaContext = sharedSchemaContextFactory.createEffectiveModelContext(
            sIdWithoutRevision, provider.getId());
        assertNotNull(schemaContext.get());
    }

    @Test
    public void testTransientFailureWhilreRetrievingSchemaSource() throws Exception {
        final SourceIdentifier s3 = new SourceIdentifier("network-topology", "2013-10-21");

        repository.registerSchemaSource(new TransientFailureProvider(
            YangTextSource.forResource("/ietf/network-topology@2013-10-21.yang")),
            PotentialSchemaSource.create(s3, YangTextSource.class, 1));

        final var sharedSchemaContextFactory = new SharedEffectiveModelContextFactory(repository, config);

        var schemaContext = sharedSchemaContextFactory.createEffectiveModelContext(s1, s3);

        final var exception = assertThrows(ExecutionException.class, schemaContext::get);
        assertInstanceOf(MissingSchemaSourceException.class, exception.getCause());

        // check if future is invalidated and resolution of source is retried after failure
        schemaContext = sharedSchemaContextFactory.createEffectiveModelContext(s1, s3);
        assertNotNull(schemaContext.get());
    }

    /**
     * Schema source provider that fails on first attempt of getSource() and succeeds on every subsequent call
     * to simulate transient failures of source retrieval.
     */
    private static final class TransientFailureProvider implements SchemaSourceProvider<YangTextSource> {
        private final YangTextSource schemaSource;

        private boolean shouldFail = true;

        private TransientFailureProvider(final YangTextSource schemaSource) {
            this.schemaSource = requireNonNull(schemaSource);
        }

        @Override
        public ListenableFuture<YangTextSource> getSource(final SourceIdentifier sourceIdentifier) {
            if (shouldFail) {
                shouldFail = false;
                return immediateFailedFluentFuture(new Exception("Transient test failure."));
            }

            return immediateFluentFuture(schemaSource);
        }
    }
}
