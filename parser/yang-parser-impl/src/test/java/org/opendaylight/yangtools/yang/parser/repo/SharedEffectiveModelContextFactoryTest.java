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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.spi.source.ResourceYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

class SharedEffectiveModelContextFactoryTest extends AbstractSchemaRepositoryTest {
    private static final ResourceYangTextSource SOURCE1 =
        assertYangTextResource("/ietf/ietf-inet-types@2010-09-24.yang");
    private static final ResourceYangTextSource SOURCE2 =
        assertYangTextResource("/ietf/iana-timezones@2012-07-09.yang");
    private static final SchemaContextFactoryConfiguration CONFIG = SchemaContextFactoryConfiguration.getDefault();

    private final SharedSchemaRepository repository = new SharedSchemaRepository("test");

    @BeforeEach
    public void setUp() {
        final var transformer = TextToIRTransformer.create(repository, repository);
        repository.registerSchemaSourceListener(transformer);

        repository.registerSchemaSource(sourceIdentifier -> immediateFluentFuture(SOURCE1),
            PotentialSchemaSource.create(SOURCE1.sourceId(), YangTextSource.class, 1));

        repository.registerSchemaSource(sourceIdentifier -> immediateFluentFuture(SOURCE2),
            PotentialSchemaSource.create(SOURCE2.sourceId(), YangTextSource.class, 1));
    }

    @Test
    public void testCreateSchemaContextWithDuplicateRequiredSources() throws Exception {
        assertNotNull(Futures.getDone(new SharedEffectiveModelContextFactory(repository, CONFIG)
            .createEffectiveModelContext(SOURCE1.sourceId(), SOURCE1.sourceId(), SOURCE2.sourceId())));
    }

    @Test
    public void testSourceRegisteredWithDifferentSI() throws Exception {
        final var provider = SharedSchemaRepositoryTest.immediateProvider("/no-revision/imported@2012-12-12.yang");
        provider.setResult();
        provider.register(repository);

        // Register the same provider under source id without revision
        final var sIdWithoutRevision = new SourceIdentifier(provider.getId().name());
        repository.registerSchemaSource(provider, PotentialSchemaSource.create(sIdWithoutRevision,
            YangIRSchemaSource.class, PotentialSchemaSource.Costs.IMMEDIATE.getValue()));

        assertNotNull(Futures.getDone(new SharedEffectiveModelContextFactory(repository, CONFIG)
            .createEffectiveModelContext(sIdWithoutRevision, provider.getId())));
    }

    @Test
    public void testTransientFailureWhilreRetrievingSchemaSource() throws Exception {
        final var source3 = assertYangTextResource("/ietf/network-topology@2013-10-21.yang");

        repository.registerSchemaSource(new TransientFailureProvider(source3),
            PotentialSchemaSource.create(source3.sourceId(), YangTextSource.class, 1));

        final var sharedSchemaContextFactory = new SharedEffectiveModelContextFactory(repository, CONFIG);

        var future = sharedSchemaContextFactory.createEffectiveModelContext(SOURCE1.sourceId(),
            source3.sourceId());

        final var exception = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        assertInstanceOf(MissingSchemaSourceException.class, exception.getCause());

        // check if future is invalidated and resolution of source is retried after failure
        assertNotNull(Futures.getDone(sharedSchemaContextFactory.createEffectiveModelContext(SOURCE1.sourceId(),
            source3.sourceId())));
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
